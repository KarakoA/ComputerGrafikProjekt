package gnakcg.engine.items;

import gnakcg.utils.ResourceLoader;
import gnakcg.utils.Utils;
import gnakcg.engine.graph.Mesh;
import gnakcg.engine.loaders.StaticMeshesLoader;
import org.joml.Vector3f;

/**
 * The base class for all objects to be rendered.
 * Holds a reference to a mesh(triangles,colours which can be rendered by open gl)
 * and a scale,position(in degrees) and rotation which are used by the Transformation class
 * to apply the according transformations for this object.
 * Based on the LWJGL Book.
 */
public class GameItem {

    private Mesh[] meshes;

    private final Vector3f position;

    private float scale;

    private final Vector3f rotation;

    public GameItem() {
        position = new Vector3f(0, 0, 0);
        scale = 1;
        rotation = new Vector3f(0, 0, 0);
    }

    public GameItem(Mesh mesh) {
        this();
        this.meshes = new Mesh[]{mesh};
    }

    public GameItem(Mesh[] meshes) {
        this();
        this.meshes = meshes;
    }

    /**
     * Loads the meshes from the given directories.
     *
     * @param objFilePath the path to the obj File. Can be relative
     * @param texturesDir the path to the texture directory. Can be relative
     */
    public GameItem(String objFilePath, String texturesDir) throws Exception {
        this();
        this.setMeshes(Mesh.fromFiles(objFilePath,texturesDir));
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }

    public Mesh getMesh() {
        return meshes[0];
    }

    public Mesh[] getMeshes() {
        return meshes;
    }

    public void setMeshes(Mesh[] meshes) {
        this.meshes = meshes;
    }

    public void setMesh(Mesh mesh) {
        this.meshes = new Mesh[]{mesh};
    }
}