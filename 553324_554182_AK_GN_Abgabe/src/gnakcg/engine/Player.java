package gnakcg.engine;

import gnakcg.engine.graph.Camera;
import gnakcg.utils.Audio;
import gnakcg.utils.Utils;

/**
 * Encapsulates the player.
 *
 * @author Anton K.
 * @author Gires N.
 */
public class Player {
    private final float STEP_THRESHOLD = 0.09f;
    private final int MAX_STAMINA = 100;


    private Camera camera;
    private float playerHeight;
    private float lastTerrainHeight;
    private int stamina;
    private int stopCounter;
    private boolean running;

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
        boolean canMove = running || !((terrainHeightAtNewPosition - lastTerrainHeight) > STEP_THRESHOLD && lastTerrainHeight != 0);

        //update player position when a step is possible
        if (canMove) {
            float y = terrainHeightAtNewPosition + playerHeight;
            lastTerrainHeight = terrainHeightAtNewPosition;
            camera.movePosition(x, 0, z);
            camera.getPosition().y = y;
        }
        updateAudio(x, z, canMove);
        addStamina();
        running = false;
    }

    private void addStamina() {
        stamina += 1;
        stamina = Math.min(MAX_STAMINA, stamina);
    }

    public void depleteStamina() {
        if (stamina >= 0) {
            stamina = stamina - 2;
            running = true;
        }
    }

    public boolean canRun() {
        return stamina > 10;
    }

    private void updateAudio(float xStep, float zStep, boolean canMove) {
        audio.setListenerPosition(camera.getPosition());
        audio.setListenerOrientation(camera.getRotation(), camera.getPosition());

        //stop playing if the player was still for 10 ticks
        if (stopCounter > 10)
            stepSound.stop();
        //tick if there was no change in neither x nor z direction
        if ((Utils.isZero(zStep, 0.0001f) && Utils.isZero(xStep, 0.0001f)) || !canMove) {
            stopCounter++;
        } else {
            stopCounter = 0;
            stepSound.playIfNotAlreadyPlaying();
        }
    }
}