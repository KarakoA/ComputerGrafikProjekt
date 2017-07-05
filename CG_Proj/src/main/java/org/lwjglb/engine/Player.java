package org.lwjglb.engine;

import org.joml.Vector3f;
import org.lwjglb.engine.graph.Camera;
import org.lwjglb.engine.services.Audio;
import org.omg.CORBA.FloatHolder;

import java.util.LinkedList;

/**
 * @author Anton K.
 */
public class Player {
    private Camera camera;
    private float playerHeight;
    private float lastTerrainHeight;
    private final float MAX_DIFFERENCE = 0.09f;

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
        stepSound = Audio.getInstance().createPlayable("/audio/long_steps.ogg");
        stepSound.setGain(0.1f);
        lastTerrainHeight = 0;
        playerHeight = 2f;
        audio=Audio.getInstance();
    }

    public void move(float x, float z, float terrainHeightAtNewPosition) {
        boolean moved = false;
        //affects only climibng
        if ((terrainHeightAtNewPosition - lastTerrainHeight) > MAX_DIFFERENCE && lastTerrainHeight != 0)
            System.out.println("w");
        else {

            float y = terrainHeightAtNewPosition + playerHeight;
            moved = true;
            lastTerrainHeight = terrainHeightAtNewPosition;
            camera.movePosition(x, 0, z);

            camera.getPosition().y = y;
            audio.setListenerPosition(camera.getPosition());
            System.out.println(camera.getPosition());
        }
//TODO check if its better with play and resume
        if (i > 10) {
            stepSound.stop();
        }
        System.out.println(i);
        if ((Utils.isZero(z, 0.0001f) && Utils.isZero(x, 0.0001f) )|| !moved) {
            i++;
            return;
        }
        i = 0;
        stepSound.playIfNotAlreadyPlaying();


    }

    private int i = 0;
}