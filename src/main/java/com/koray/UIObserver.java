package com.koray;
public class UIObserver implements Observer {

    Game game;
    Main main;

    public UIObserver(Game game, Main main) {
        this.game = game;
        this.main = main;
    }

    @Override
    public void onEvent(GameEvent event) {

        main.updateUI(); // her eventte UI refresh
    }
}
