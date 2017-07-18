package gnakcg.engine.graph;

import org.joml.Vector4f;

/**
 * Represents a material.
 * Based on the LWJGL Book.
 */
public class Material {

    public static final Vector4f DEFAULT_COLOUR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    private Vector4f ambientColour;

    private Vector4f diffuseColour;

    private Vector4f specularColour;

    private float reflectance;

    private Texture texture;

    public Material() {
        this.ambientColour = DEFAULT_COLOUR;
        this.diffuseColour = new Vector4f(0.5f, 0.5f, 0.5f, 0.5f);
        this.specularColour = DEFAULT_COLOUR;
        this.texture = null;
        this.reflectance = 0;
    }

    public Material(Vector4f colour, float reflectance) {
        this(colour, colour, colour, null, reflectance);
    }

    public Material(Texture texture) {
        this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, texture, 0);
    }

    public Material(Texture texture, float reflectance) {
        this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, texture, reflectance);
    }

    public Material(Vector4f ambientColour, Vector4f diffuseColour, Vector4f specularColour, Texture texture, float reflectance) {
        this.ambientColour = ambientColour;
        this.diffuseColour = diffuseColour;
        this.specularColour = specularColour;
        this.texture = texture;
        this.reflectance = reflectance;

    }

    public Vector4f getAmbientColour() {
        return ambientColour;
    }

    public void setAmbientColour(Vector4f ambientColour) {
        this.ambientColour = ambientColour;
    }

    public Vector4f getDiffuseColour() {
        return diffuseColour;
    }

    public Vector4f getSpecularColour() {
        return specularColour;
    }

    public float getReflectance() {
        return reflectance;
    }

    public boolean isTextured() {
        return this.texture != null;
    }

    public Texture getTexture() {
        return texture;
    }
    @Override
    public String toString() {
        return "Material{" +
                "ambientColour=" + ambientColour +
                ", diffuseColour=" + diffuseColour +
                ", specularColour=" + specularColour +
                ", reflectance=" + reflectance +
                ", texture=" + texture +
                '}';
    }
}