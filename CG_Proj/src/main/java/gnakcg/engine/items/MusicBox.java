package gnakcg.engine.items;

import java.io.File;

import gnakcg.engine.Utils;
import gnakcg.engine.loaders.StaticMeshesLoader;
import gnakcg.engine.services.Audio;

/**
 * Encapsulates the music box.
 *
 * @author Anton K.
 * @author Gires N.
 */
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

    public MusicBox(String objFilePath, String texturesDir, String audioFilePath) throws Exception {
        super(objFilePath,texturesDir);

        //load audio
        backgroundMusic = Audio.getInstance().createPlayable(audioFilePath);
        backgroundMusic.setPosition(this.getPosition());
        backgroundMusic.enableSourceSoundDecrease();
        backgroundMusic.play();
    }
}
