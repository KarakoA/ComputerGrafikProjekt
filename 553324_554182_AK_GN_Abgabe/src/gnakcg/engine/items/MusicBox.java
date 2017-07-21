package gnakcg.engine.items;

import gnakcg.engine.graph.Mesh;
import gnakcg.utils.Audio;

/**
 * Encapsulates the music box.
 *
 * @author Anton K.
 * @author Gires N.
 */
public class MusicBox extends GameItem {
    private Audio.Playable backgroundMusic;
    private Mesh[] cachedMeshes;


    @Override
    public void setPosition(float x, float y, float z) {
        super.setPosition(x, y, z);
        backgroundMusic.setPosition(getPosition());
    }

    public Audio.Playable getBackgroundMusic() {
        return backgroundMusic;
    }

    public MusicBox(String objFilePath, String texturesDir, String audioFilePath) throws Exception {
        super();

        //save it and hide the mesh
        cachedMeshes = Mesh.fromFiles(objFilePath,texturesDir);
        setMeshes(new Mesh[0]);
        //load audio
        backgroundMusic = Audio.getInstance().createPlayable(audioFilePath);
        backgroundMusic.setPosition(this.getPosition());
        backgroundMusic.enableSourceSoundDecrease();
        backgroundMusic.play();
    }

    public void toggleMusicBoxVisiblity(boolean shouldShow) {
        if (shouldShow) {
            if (this.getMeshes().length == 0)
                this.setMeshes(cachedMeshes);
        } else {
            if (this.getMeshes().length != 0)
                this.setMeshes(new Mesh[0]);
        }
    }
}
