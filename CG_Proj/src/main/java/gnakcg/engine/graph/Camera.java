package gnakcg.engine.graph;

import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Represents the camera.
 * Based on the LWJGL Book.
 */
public class Camera {

    private final Vector3f position;

    private final Vector2f rotation;

    public Camera() {
        position = new Vector3f(0, 0, 0);
        rotation = new Vector2f(0, 0);
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public Vector3f calculateMovePosition(float offsetX, float offsetY, float offsetZ) {
        Vector3f result = new Vector3f(position.x, position.y, position.z);
        if (offsetZ != 0) {
            result.x += (float) Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
            result.z += (float) Math.cos(Math.toRadians(rotation.y)) * offsetZ;
        }
        if (offsetX != 0) {
            result.x += (float) Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
            result.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
        }
        result.y += offsetY;
        return result;
    }


    public void movePosition(float offsetX, float offsetY, float offsetZ) {
        if (offsetZ != 0) {
            position.x += (float) Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
            position.z += (float) Math.cos(Math.toRadians(rotation.y)) * offsetZ;
        }
        if (offsetX != 0) {
            position.x += (float) Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
            position.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
        }
        position.y += offsetY;
    }

    public Vector2f getRotation() {
        return rotation;
    }

    public void setRotation(Vector2f rotation) {
        this.rotation.x=rotation.x;
        this.rotation.y=rotation.y;
    }

}