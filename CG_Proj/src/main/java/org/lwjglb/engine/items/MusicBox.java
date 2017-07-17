package org.lwjglb.engine.items;

import java.io.File;

import org.lwjglb.engine.services.Audio;
import org.lwjglb.game.StaticMeshesLoader;

public class MusicBox extends GameItem {
    private Audio.Playable backgroundMusic;

    @Override
    public void setPosition(float x, float y, float z) {
        super.setPosition(x, y, z);
        backgroundMusic.setPosition(getPosition());
    }

    public Audio.Playable getBackgroundMusic() {
        return backgroundMusic;
    }
    public MusicBox(String resourcePath, String texturesDir, String audioFilePath) throws Exception {
        super();
        //load meshes
        String fileName = Thread.currentThread().getContextClassLoader()
                .getResource(resourcePath).getFile();
        File file = new File(fileName);
        this.setMeshes(StaticMeshesLoader.load(file.getAbsolutePath(), texturesDir));

        //load audio
        backgroundMusic = Audio.getInstance().createPlayable(audioFilePath);
        backgroundMusic.setPosition(this.getPosition());
        backgroundMusic.enableSourceSoundDecrease();
        backgroundMusic.play();
    }
}
