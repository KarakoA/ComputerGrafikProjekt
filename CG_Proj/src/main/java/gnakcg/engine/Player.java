package gnakcg.engine;

import gnakcg.engine.graph.Camera;
import gnakcg.engine.services.Audio;
import gnakcg.utils.Utils;

/**
 * Encapsulates the player.
 *
 * @author Anton K.
 * @author Gires N.
 */
public class Player {
    private final float STEP_THRESHOLD = 0.09f;
    private final int MAX_STAMINA = 200;


    private Camera camera;
    private float playerHeight;
    private float lastTerrainHeight;
    private int stamina;
    private int stopCounter;

    private Audio.Playable stepSound;
    private Audio audio;

    public void setPlayerHeight(float playerHeight) {
        this.playerHeight = playerHeight;
    }

    public Camera getCamera() {
        return camera;
    }

    public Player(Camera camera) {
        this.camera = camera;
        audio = Audio.getInstance();
        stepSound = audio.createPlayable("/audio/long_steps.ogg", true);
        stepSound.setGain(0.15f);
        lastTerrainHeight = 0;
        stamina = MAX_STAMINA;

    }

    public void updatePosition(float x, float z, float terrainHeightAtNewPosition) {
        //See if the player can make the step. Affects only climbing
        boolean cantMove = ((terrainHeightAtNewPosition - lastTerrainHeight) > STEP_THRESHOLD && lastTerrainHeight != 0);
        //update player position when a step is possible
        if (!cantMove) {
            float y = terrainHeightAtNewPosition + playerHeight;
            lastTerrainHeight = terrainHeightAtNewPosition;
            camera.movePosition(x, 0, z);
            camera.getPosition().y = y;
        }
        updateAudio(x, z, cantMove);
        addStamina();
    }

    private void addStamina() {
        stamina+=1;
        stamina = Math.min(MAX_STAMINA, stamina);
    }

    public void depleteStamina() {
        stamina = stamina - 2;
        stamina = Math.max(0, stamina);
    }

    public boolean canRun() {
        return stamina > 10;
    }

    private void updateAudio(float xStep, float zStep, boolean cantMove) {
        audio.setListenerPosition(camera.getPosition());
        audio.setListenerOrientation(camera.getRotation(), camera.getPosition());

        //stop playing if the player was still for 10 ticks
        if (stopCounter > 10)
            stepSound.stop();
        //tick if there was no change in neither x nor z direction
        if ((Utils.isZero(zStep, 0.0001f) && Utils.isZero(xStep, 0.0001f)) || cantMove) {
            stopCounter++;
        } else {
            stopCounter = 0;
            stepSound.playIfNotAlreadyPlaying();
        }
    }
}