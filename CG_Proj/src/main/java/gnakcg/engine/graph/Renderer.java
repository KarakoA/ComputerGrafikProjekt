package gnakcg.engine.graph;

import gnakcg.engine.Scene;
import gnakcg.engine.SceneLight;

import java.util.Collection;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.*;

import gnakcg.engine.items.GameItem;
import gnakcg.engine.IHud;
import gnakcg.engine.Utils;
import gnakcg.engine.Window;

/**
 * Renders the Scene.
 * Based on the LWJGL Book.
 */
public class Renderer {

    /**
     * Field of View in Radians
     */
    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.001f;

    private static final float Z_FAR = 100f;

    private final Transformation transformation;

    private ShaderProgram sceneShaderProgram;

    private ShaderProgram hudAndSkyboxShaderProgram;
    private final float specularPower;

    public Renderer() {
        transformation = new Transformation();
        specularPower = 10f;
    }

    public void init() throws Exception {
        setupSceneShader();
        setupHudAndSkyboxShader();
    }


    private void setupSceneShader() throws Exception {
        // Create shader
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(Utils.loadResource("/shaders/scene_vertex.vs"));
        sceneShaderProgram.createFragmentShader(Utils.loadResource("/shaders/scene_fragment.fs"));
        sceneShaderProgram.link();

        // Create uniforms for modelView and projection matrices and texture
        sceneShaderProgram.createUniform("projectionMatrix");
        sceneShaderProgram.createUniform("modelViewMatrix");
        sceneShaderProgram.createUniform("texture_sampler");
        // Create uniform for material
        sceneShaderProgram.createMaterialUniform("material");
        // Create lighting related uniforms
        sceneShaderProgram.createUniform("specularPower");
        sceneShaderProgram.createUniform("ambientLight");
        sceneShaderProgram.createDirectionalLightUniform("directionalLight");
    }

    private void setupHudAndSkyboxShader() throws Exception {
        hudAndSkyboxShaderProgram = new ShaderProgram();
        hudAndSkyboxShaderProgram.createVertexShader(Utils.loadResource("/shaders/hud_vertex.vs"));
        hudAndSkyboxShaderProgram.createFragmentShader(Utils.loadResource("/shaders/hud_fragment.fs"));
        hudAndSkyboxShaderProgram.link();

        // Create uniforms for Ortographic-model projection matrix and base colour
        hudAndSkyboxShaderProgram.createUniform("projModelMatrix");
        hudAndSkyboxShaderProgram.createUniform("colour");
        hudAndSkyboxShaderProgram.createUniform("hasTexture");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, Camera camera, Scene scene, IHud hud) {
        clear();

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        // Update projection and view atrices once per render cycle
        transformation.updateProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        transformation.updateViewMatrix(camera);

        renderScene(scene);
        renderSkyBox(scene);

        renderHud(window, hud);
    }

    private void renderSkyBox(Scene scene) {

        hudAndSkyboxShaderProgram.bind();
        Matrix4f projectionMatrix = transformation.getProjectionMatrix();
        GameItem skyBox = scene.getSkyBox();
        Matrix4f viewMatrix = transformation.getViewMatrix();
        viewMatrix.m30(0);
        viewMatrix.m31(0);
        viewMatrix.m32(0);
        Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(skyBox, viewMatrix);
        Matrix4f projModelViewMatrix = projectionMatrix.mul(modelViewMatrix);
        Vector4f colour = new Vector4f(scene.getSceneLight().getSkyBoxLight(), 1f);

        hudAndSkyboxShaderProgram.setUniform("projModelMatrix", projModelViewMatrix);
        hudAndSkyboxShaderProgram.setUniform("colour", colour);
        hudAndSkyboxShaderProgram.setUniform("hasTexture", 1);
        scene.getSkyBox().getMesh().render();
        hudAndSkyboxShaderProgram.unbind();
    }

    public void renderScene(Scene scene) {
        sceneShaderProgram.bind();

        Matrix4f projectionMatrix = transformation.getProjectionMatrix();
        sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        Matrix4f viewMatrix = transformation.getViewMatrix();

        SceneLight sceneLight = scene.getSceneLight();
        renderLights(viewMatrix, sceneLight);

        sceneShaderProgram.setUniform("texture_sampler", 0);
        // Render each mesh with the associated game Items
        Collection<GameItem> gameItems = scene.getGameItems();
        for (GameItem gameItem : gameItems) {
            Mesh mesh = gameItem.getMesh();

            sceneShaderProgram.setUniform("material", mesh.getMaterial());
            Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(gameItem, viewMatrix);
            sceneShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

            mesh.render();
        }
        Mesh[] meshes = scene.getMusicBox().getMeshes();
        for (Mesh mesh : meshes) {
            sceneShaderProgram.setUniform("material", mesh.getMaterial());
            Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(scene.getMusicBox(), viewMatrix);
            sceneShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            mesh.render();
        }


        sceneShaderProgram.unbind();
    }

    private void renderLights(Matrix4f viewMatrix, SceneLight sceneLight) {

        sceneShaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());
        sceneShaderProgram.setUniform("specularPower", specularPower);

        // Get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        sceneShaderProgram.setUniform("directionalLight", currDirLight);
    }

    private void renderHud(Window window, IHud hud) {

        hudAndSkyboxShaderProgram.bind();

        Matrix4f ortho = transformation.getOrthoProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);
        for (GameItem gameItem : hud.getGameItems()) {
            Mesh mesh = gameItem.getMesh();
            // Set ortohtaphic and model matrix for this HUD item
            Matrix4f projModelMatrix = transformation.buildModelViewMatrix(gameItem, ortho);
            hudAndSkyboxShaderProgram.setUniform("projModelMatrix", projModelMatrix);
            hudAndSkyboxShaderProgram.setUniform("colour", gameItem.getMesh().getMaterial().getAmbientColour());
            hudAndSkyboxShaderProgram.setUniform("hasTexture", gameItem.getMesh().getMaterial().isTextured() ? 1 : 0);

            // Render the mesh for this HUD item
            mesh.render();
        }
        hudAndSkyboxShaderProgram.unbind();
    }

    public void cleanup() {
        if (sceneShaderProgram != null) {
            sceneShaderProgram.cleanup();
        }
        if (hudAndSkyboxShaderProgram != null) {
            hudAndSkyboxShaderProgram.cleanup();
        }
    }
}
