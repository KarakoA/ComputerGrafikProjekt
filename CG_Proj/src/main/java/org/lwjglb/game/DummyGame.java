package org.lwjglb.game;

import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjglb.engine.*;
import org.lwjglb.engine.graph.Camera;
import org.lwjglb.engine.graph.Renderer;
import org.lwjglb.engine.graph.lights.DirectionalLight;

import org.lwjglb.engine.items.SkyBox;
import org.lwjglb.engine.items.Terrain;
import org.lwjglb.engine.services.Audio;

import java.util.LinkedList;

public class DummyGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;

    private final Vector3f cameraInc;

    private final Renderer renderer;

    private Player player;
    private Audio.Playable backgroundMusic;

    private Scene scene;

    private Hud hud;

    private float lightAngle;

    private static final float CAMERA_POS_STEP = 0.04f;//0.05f;

    public DummyGame() {
        renderer = new Renderer();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        lightAngle = -90;
    }

    private Terrain terrain;
    float terrainScale;

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        backgroundMusic = Audio.getInstance().createPlayable("/audio/lailaihei_mono.ogg");
        backgroundMusic.setPosition(new Vector3f(0.0f, 0.0f, 0.0f));
        backgroundMusic.play();

        scene = new Scene();

        float skyBoxScale = 50.0f;
        terrainScale = 20;
        float minY = 0;
        float maxY = 0.3f;

        float playerHeight = 0.15f;//0.001f;
        int textInc = 40;

        terrain = new Terrain(terrainScale, minY, maxY, "/textures/terrain.png", textInc);

        scene.setGameItems(terrain.getGameItems());
        player = new Player(new Camera());
        player.setPlayerHeight(playerHeight);


        // Setup  SkyBox
        SkyBox skyBox = new SkyBox("/models/skybox.obj", "/textures/skybox.png");
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);

        // Setup Lights
        setupLights();

        // Create HUD
        hud = new Hud("DEMO");


        player.getCamera().setPosition(5f, 1.0f, 5f);

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
        } else if (window.isKeyPressed(GLFW_KEY_G)) {
            backgroundMusic.testVelocityAndGain();
           backgroundMusic.setPosition(new Vector3f(0,0,0));
            // / backgroundMusic.setPosition(new Vector3f(2000,2000,2000f));
            System.out.println(String.format("X: %.2f", player.getCamera().getPosition().x));
            System.out.println(String.format("Z: %.2f", player.getCamera().getPosition().z));
            Vector2i newChunk = determineChunkNew();
            //movePlayer();
            System.out.println(player.getCamera().getPosition().y + ": Y");
            // System.out.println(newChunkO);
            System.out.println("Chunk: " + newChunk);

        }

    }

    private void movePlayer() {
        //only move when no n nzero

        Vector3f newPositon = player.getCamera().calculateMovePosition(cameraInc.x * CAMERA_POS_STEP * 2f, 0, cameraInc.z * CAMERA_POS_STEP * 2f);
        float height = terrain.getHeight(newPositon.x, newPositon.z);
        //  System.out.println("Height: "+ height);
        player.move(cameraInc.x * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP, height);

    }


    private int chunkFunction(float a) {
        //1 if negativ, 0 if positiv
        int sign = Float.floatToRawIntBits(a) >> 31;
        return (int) (a / terrainScale) + sign;
    }

    private Vector2i determineChunkNew() {
        int x = chunkFunction(player.getCamera().getPosition().x);
        int z = chunkFunction(player.getCamera().getPosition().z);
        return new Vector2i(x, z);
    }

    private void updateCurrentChunk() {
        if (terrain.getCurrentChunkPosition() == null)
            return;
        Vector2i newChunk = determineChunkNew();

        terrain.setCurrentChunkPosition(newChunk);
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

        updateCurrentChunk();
        // Update camera position
        movePlayer();
        // player.getCamera().movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);

        // Update directional light direction, intensity and colour
        SceneLight sceneLight = scene.getSceneLight();
        DirectionalLight directionalLight = sceneLight.getDirectionalLight();
        // lightAngle += 0.5f;
        lightAngle = 70f;
        if (lightAngle > 90) {
            directionalLight.setIntensity(0);
            if (lightAngle >= 360) {
                lightAngle = -90;
            }
            sceneLight.getSkyBoxLight().set(0.3f, 0.3f, 0.3f);
        } else if (lightAngle <= -80 || lightAngle >= 80) {
            float factor = 1 - (float) (Math.abs(lightAngle) - 80) / 10.0f;
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
