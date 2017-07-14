package org.lwjglb.game;

import java.io.File;

import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.items.GameItem;

public class MusicBox extends GameItem {

	public MusicBox(String resourcePath, String texturesDir) throws Exception{
		super();
		String fileName = Thread.currentThread().getContextClassLoader()
	              .getResource(resourcePath).getFile();
			File file = new File(fileName);
			this.setMeshes(StaticMeshesLoader.load(file.getAbsolutePath(), texturesDir));
	}
}
