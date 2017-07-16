package org.lwjglb.game;

import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjglb.engine.GameEngine.TARGET_UPS;

import org.lwjglb.engine.*;
import org.lwjglb.engine.graph.*;
import org.lwjglb.engine.graph.lights.DirectionalLight;

import org.lwjglb.engine.items.GameItem;
import org.lwjglb.engine.items.SkyBox;
import org.lwjglb.engine.items.Terrain;
import org.lwjglb.engine.services.Audio;

public class DummyGame implements IGameLogic {

    public static final int TERRAIN_SCALE = 20;
    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.04f;
    private final float MINIMUM_WIN_DISTANCE = 1f;

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private Player player;
    private Scene scene;
    private Hud hud;
    private Terrain terrain;

    //sounds
    private Audio.Playable backgroundMusic;
    private Vector3f musicBoxPosition;
    private Audio.Playable loseSound;
    private Audio.Playable winSound;
    //time
    private final long start;
    //light
    private float lightAngle;
    private float angleUpdatePerCycle;
    private final int START_LIGHT_ANGLE = -100;
    private final int MAX_LIGHT_ANGLE = 100;

    //state variables

    private boolean won;
    private boolean controlsDisabled;
    private boolean DebugHardcodedTestEndBoolean;

    public DummyGame() {
        renderer = new Renderer();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        start = System.currentTimeMillis();
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        float skyBoxScale = 50f;
        float minY = 0;
        float maxY = 0.3f;
        float playerHeight = 0.80f;//0.001f;
        int textInc = 40;
        terrain = new Terrain(TERRAIN_SCALE, minY, maxY, "/textures/terrain.png", textInc);
        //terrain = new Terrain(TERRAIN_SCALE, minY, maxY, "/textures/terrain-big.png", textInc);

        //background music
        Audio audio = Audio.getInstance();
        backgroundMusic = audio.createPlayable("/audio/background/lailaihei_short.ogg");
        //TODO position wrong calculated
        musicBoxPosition = terrain.getMusicBoxPosition(TERRAIN_SCALE, CAMERA_POS_STEP, backgroundMusic.getDurationInMiliSeconds());

        System.out.println("Music Box Location:");
        System.out.println("X: " + musicBoxPosition.x);
        System.out.println("Y: " + musicBoxPosition.y);
        System.out.println("Z: " + musicBoxPosition.z);
        backgroundMusic.setPosition(musicBoxPosition);
        backgroundMusic.play();
        backgroundMusic.enableSourceSoundDecrease();

        //load the audio files in advance
        loseSound = audio.createPlayable("/audio/lose.ogg", true);
        winSound = audio.createPlayable("/audio/win.ogg", true);

        scene = new Scene();
        scene.setGameItems(terrain.getGameItems());
        player = new Player(new Camera());
        player.setPlayerHeight(playerHeight);

        // "Music Box"
        
        MusicBox musicBox = new MusicBox("models/tower/tower2.obj", "/models/tower");
        musicBox.setScale(0.3f);
        musicBox.setPosition(musicBoxPosition.x, 1, musicBoxPosition.z);
        //musicBox.setPosition(0, 1, 0);
        scene.setMusicBox(musicBox);

        // Setup  SkyBox
        SkyBox skyBox = new SkyBox("models/skybox/skybox.obj", "/models/skybox");
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);

        // Setup Lights
        setupLights();

        //Setup Light Change cyle
        lightAngle = START_LIGHT_ANGLE;
        long durationInSeconds = backgroundMusic.getDurationInMiliSeconds() / 1000;
        angleUpdatePerCycle = (Math.abs(lightAngle) + MAX_LIGHT_ANGLE) / (durationInSeconds * (TARGET_UPS));


        // Create HUD
        hud = new Hud();

        player.getCamera().setPosition(0, 0, 0);
        player.getCamera().getRotation().x = 10.f;
    }

    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);

        // Ambient Light
        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyBoxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        // Directional Light
        float lightIntensity = 1.0f;
        Vector3f lightPosition = new Vector3f(1, 1, 0);
        sceneLight.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity));

    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        cameraInc.set(0, 0, 0);
        if (controlsDisabled)
            return;
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            cameraInc.y = 1;

        }
        if (window.isKeyPressed(GLFW_KEY_L)) {
            DebugHardcodedTestEndBoolean = true;
        }
        if (window.isKeyPressed(GLFW_KEY_K)) {
            scene.getSceneLight().getDirectionalLight().setColor(new Vector3f(255, 0, 0));
            scene.getSceneLight().setAmbientLight(new Vector3f(255, 0, 0));
        }
        if (window.isKeyPressed(GLFW_KEY_G)) {

            Vector3f cameraPosition = player.getCamera().getPosition();
            Vector2f musicBoxXZ = new Vector2f(musicBoxPosition.x, musicBoxPosition.z);
            Vector2f cameraXZ = new Vector2f(cameraPosition.x, cameraPosition.z);
            float distance = musicBoxXZ.distance(cameraXZ);
            System.out.println("Distance: " + distance);

            //print some info for debugging
            System.out.println(String.format("X: %.2f", player.getCamera().getPosition().x));
            System.out.println("Y(Height): " + player.getCamera().getPosition().y + "");
            System.out.println(String.format("Z: %.2f", player.getCamera().getPosition().z));
            //  Vector2i newChunk = determineNewChunk();
            //   System.out.println("Chunk: " + newChunk);

            //   System.out.println("Looking At: " + player.getCamera().getRotation());
            System.out.println();
        }

    }

    private void updateCurrentChunk() {
        if (terrain.getCurrentChunkPosition() == null)
            return;
        Vector2i newChunk = determineNewChunk();
        terrain.setCurrentChunkPosition(newChunk);
    }

    private Vector2i determineNewChunk() {
        int x = chunkFunction(player.getCamera().getPosition().x);
        int z = chunkFunction(player.getCamera().getPosition().z);
        return new Vector2i(x, z);
    }

    private int chunkFunction(float a) {
        //-1 if negativ, 0 if positiv
        int sign = Float.floatToRawIntBits(a) >> 31;
        return (int) (a / TERRAIN_SCALE) + sign;
    }

    private void UpdatePlayer() {
        //note we calculate the test position using 2*step and afterwards update it only 1*step
        Vector3f newPositon = player.getCamera().calculateMovePosition(cameraInc.x * CAMERA_POS_STEP * 2f, 0, cameraInc.z * CAMERA_POS_STEP * 2f);
        float height = terrain.getHeight(newPositon.x, newPositon.z);
        player.updatePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP, height);

    }

    @Override
    public void update(float interval, MouseInput mouseInput) {
        // Update camera based on mouse            
        if (mouseInput.isRightButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            player.getCamera().moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
            // Update HUD compass
            hud.rotateCompass(player.getCamera().getRotation().y);
        }
        //Update the currentChnk
        updateCurrentChunk();
        // Update camera position and player step sound
        UpdatePlayer();

        if (foundMusicBox() || timeIsOver() || DebugHardcodedTestEndBoolean)
            endGame();
        else {
            //Update the lights
            updateLight();
        }
    }

    private boolean foundMusicBox() {
        Vector3f cameraPosition = player.getCamera().getPosition();
        Vector2f musicBoxXZ = new Vector2f(musicBoxPosition.x, musicBoxPosition.z);
        Vector2f cameraXZ = new Vector2f(cameraPosition.x, cameraPosition.z);
        float distance = musicBoxXZ.distance(cameraXZ);
        won = distance < MINIMUM_WIN_DISTANCE;
        return won;
    }

    private boolean timeIsOver() {
        long now = System.currentTimeMillis();
        //1.05 of the duration for dramatic purposes. So there is a little bit with no music at the end
        long gameDuration = (long) (backgroundMusic.getDurationInMiliSeconds() * 1.05);
        return now > start + gameDuration;
    }

    private void endGame() {
        backgroundMusic.stop();
        angleUpdatePerCycle = 0;
        controlsDisabled = true;
        SceneLight sceneLight = scene.getSceneLight();
        if (won) {
            lightAngle = 80;
            updateLight();
            sceneLight.setAmbientLight(new Vector3f(0.7f, 0.7f, 0.7f));
            winSound.playIfNotAlreadyPlaying();
            //play happy music
        } else {
            lightAngle = 90;
            float red = sceneLight.getAmbientLight().x;
            float skyBoxLight = sceneLight.getSkyBoxLight().x;
            //ambient and directional light: transition to red
            red += 0.01f;
            skyBoxLight -= 0.01f;
            skyBoxLight = Math.max(skyBoxLight, 0.01f);

            sceneLight.getAmbientLight().x = Math.min(3, red);
            sceneLight.getDirectionalLight().getColor().x = red;
            //dim the sky box light
            sceneLight.getSkyBoxLight().set(skyBoxLight, skyBoxLight, skyBoxLight);
            //sad laugh and fade light
            loseSound.playIfNotAlreadyPlaying();
        }
    }

    private void updateLight() {
        // Update directional light direction, intensity and colour
        SceneLight sceneLight = scene.getSceneLight();
        DirectionalLight directionalLight = sceneLight.getDirectionalLight();
        lightAngle += angleUpdatePerCycle;
        lightAngle = Math.min(lightAngle, MAX_LIGHT_ANGLE);
        if (lightAngle <= -80 || lightAngle >= 80) {
            float factor = 1 - (Math.abs(lightAngle) - 80) / 10.0f;
            sceneLight.getSkyBoxLight().set(factor, factor, factor);
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
            sceneLight.getSkyBoxLight().set(1.0f, 1.0f, 1.0f);
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }
        double angRad = Math.toRadians(lightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angRad);
        directionalLight.getDirection().y = (float) Math.cos(angRad);
        if (lightAngle >= MAX_LIGHT_ANGLE) {
            float step = 0.001f;
            sceneLight.getAmbientLight().sub(new Vector3f(step, step, step)).max(new Vector3f(0.15f, 0.15f, 0.15f));
        }
    }


    @Override
    public void render(Window window) {
        hud.updateSize(window);
        renderer.render(window, player.getCamera(), scene, hud);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        scene.cleanup();
        if (hud != null) {
            hud.cleanup();
        }
    }
}
