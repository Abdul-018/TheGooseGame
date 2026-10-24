package io.github.goose_game;

import com.badlogic.gdx.ApplicationAdapter;

public class Main extends ApplicationAdapter {

    private Game game;

    @Override
    public void create() {
        game = new Game();
        game.create();
    }

    @Override
    public void resize(int width, int height) {
        game.resize(width, height);
    }

    @Override
    public void render() {
        game.render();
    }

    @Override
    public void dispose() {
        game.dispose();
    }
}
