package gnakcg.game;

import java.util.Random;

/**
 * Represents the game parameters.
 *
 * @author Anton K.
 * @author Gires N.
 */
public class GameParameters {
    public static final GameParameters DEFAULT_PARAMETERS =
            new GameParameters(Difficulty.NORMAL, new Random().nextLong(), "/audio/background/koh_mono_mix.ogg");
    private final Difficulty difficulty;
    private final long seed;
    private String backGroundMusicPath;

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public long getSeed() {
        return seed;
    }

    public String getBackGroundMusicPath() {
        return backGroundMusicPath;
    }

    public GameParameters(Difficulty difficulty, long seed, String backGroundMusicPath) {
        this.difficulty = difficulty;
        this.seed = seed;
        this.backGroundMusicPath = backGroundMusicPath;
    }

    public enum Difficulty {
        EASY(0.35f), NORMAL(0.75f), HARD(1.15f);
        public final float factor;

        Difficulty(float factor) {
            this.factor = factor;
        }
    }
}
