package gnakcg.engine.graph;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import gnakcg.utils.ResourceLoader;
import gnakcg.utils.Utils;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

/**
 * Encapsulates an openGL texture.
 * Based on the LWJGL Book.
 */
public class Texture {

    private final int id;

    public Texture(String fileName) throws Exception {
        this(ResourceLoader.getInstance().getResourceAsStream(fileName));
    }

    public Texture(InputStream is) throws Exception {
        // Load Texture file
        PNGDecoder decoder = new PNGDecoder(is);

        int width = decoder.getWidth();
        int height = decoder.getHeight();

        // Load texture contents into a byte buffer
        ByteBuffer buf = ByteBuffer.allocateDirect(
                4 * decoder.getWidth() * decoder.getHeight());
        decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
        buf.flip();

        // Create a new OpenGL texture 
        this.id = glGenTextures();
        // Bind the texture
        bind();

        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        // Upload the texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        // Generate Mip Map. Used for images with a smaller scale
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void cleanup() {
        glDeleteTextures(id);
    }
}
