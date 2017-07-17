package org.lwjglb.game;

import org.lwjglb.engine.GameEngine;
import org.lwjglb.engine.IGameLogic;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        try {
            IGameLogic gameLogic = new DummyGame(GameParameters.DEFAULT_PARAMETERS);
            GameEngine gameEng = new GameEngine("GAME", gameLogic);
            new Thread(gameEng, GameEngine.OPENGL_THREAD_NAME).start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}