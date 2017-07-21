package gnakcg.engine.graph;

import org.joml.Vector4f;

/**
 * Represents a material.
 * Based on the LWJGL Book.
 */
public class Material {

    public static final Vector4f DEFAULT_AMBIENT_COLOUR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    private Vector4f colour;
    private float reflectance;

    private Texture texture;

    public Material() {
        this.colour = DEFAULT_AMBIENT_COLOUR;
        this.texture = null;
        this.reflectance = 0;
    }

    public Material(Vector4f colour, float reflectance) {
        this(colour, null, reflectance);
    }

    public Material(Texture texture) {
        this(DEFAULT_AMBIENT_COLOUR, texture, 0);
    }

    public Material(Texture texture, float reflectance) {
        this(DEFAULT_AMBIENT_COLOUR, texture, reflectance);
    }

    public Material(Vector4f colour, Texture texture, float reflectance) {
        this.colour = colour;
        this.texture = texture;
        this.reflectance = reflectance;

    }

    public Vector4f getColour() {
        return colour;
    }

    public void setColour(Vector4f colour) {
        this.colour = colour;
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
                "colour=" + colour +
                ", reflectance=" + reflectance +
                ", texture=" + texture +
                '}';
    }
}