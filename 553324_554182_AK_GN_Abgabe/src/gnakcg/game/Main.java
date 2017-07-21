package gnakcg.game;

import gnakcg.engine.GameEngine;
import gnakcg.engine.IGameLogic;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        try {
            IGameLogic gameLogic = new Game(GameParameters.DEFAULT_PARAMETERS);
            GameEngine gameEng = new GameEngine("SoundSource", gameLogic);
            new Thread(gameEng, GameEngine.OPENGL_THREAD_NAME).start();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}