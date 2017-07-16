package org.lwjglb.engine.items;

import java.io.File;

import org.lwjglb.engine.graph.Material;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.graph.Texture;
import org.lwjglb.game.StaticMeshesLoader;

public class SkyBox extends GameItem {

    public SkyBox(String objModel, String textureFile) throws Exception {
        super();
        /*Mesh skyBoxMesh = OBJLoader.loadMesh(objModel);
        Texture skyBoxtexture = new Texture(textureFile);
        skyBoxMesh.setMaterial(new Material(skyBoxtexture, 0.0f));
        setMesh(skyBoxMesh);*/
		String fileName = Thread.currentThread().getContextClassLoader()
	              .getResource(objModel).getFile();
			File file = new File(fileName);
			this.setMeshes(StaticMeshesLoader.load(file.getAbsolutePath(), textureFile));
        setPosition(0, 0, 0);
    }
}
