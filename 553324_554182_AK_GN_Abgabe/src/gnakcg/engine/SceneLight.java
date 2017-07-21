package gnakcg.engine;

import gnakcg.engine.graph.DirectionalLight;
import org.joml.Vector3f;

/**
 * Represents the light of the scene. 3 Types of lighting are used and configurable:
 * skyBoxLight, for the skybox. Ambient for the other parts of the scene and Directional for the sun.
 * Taken from the LWJGL Book.
 */
public class SceneLight {

    private Vector3f ambientLight;

    private Vector3f skyBoxLight;

    private DirectionalLight directionalLight;

    public Vector3f getAmbientLight() {
        return ambientLight;
    }

    public void setAmbientLight(Vector3f ambientLight) {
        this.ambientLight = ambientLight;
    }

    public DirectionalLight getDirectionalLight() {
        return directionalLight;
    }

    public void setDirectionalLight(DirectionalLight directionalLight) {
        this.directionalLight = directionalLight;
    }

    public Vector3f getSkyBoxLight() {
        return skyBoxLight;
    }

    public void setSkyBoxLight(Vector3f skyBoxLight) {
        this.skyBoxLight = skyBoxLight;
    }

}