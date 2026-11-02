package io.github.goose_game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private GooseWaveManager waveManager;
    private Vector2 mousePos;

    @Override
    public void create() {
        batch = new SpriteBatch();
        mousePos = new Vector2();
        waveManager = new GooseWaveManager("goose.png", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        mousePos.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            for (Goose goose : waveManager.getActiveGeese()) {
                if (goose.getSprite().getBoundingRectangle().contains(mousePos.x, mousePos.y)) {
                    goose.onCollision();
                    System.out.println("Goose dispersed! Total dispersed: " + waveManager.getTotalGeeseDispersed());
                    break;
                }
            }
        }

        waveManager.update(delta, mousePos);

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        batch.begin();
        waveManager.draw(batch);
        batch.end();

        System.out.printf("Game Time: %.1fs/300s | Wave: %d | Wave Time: %.1fs/30s | Active Geese: %d | Total Dispersed: %d%n",
                waveManager.getTotalGameTimer(),
                waveManager.getCurrentWave(),
                waveManager.getWaveTimer(),
                waveManager.getActiveGeese().size(),
                waveManager.getTotalGeeseDispersed());
    }

    @Override
    public void dispose() {
        batch.dispose();
        waveManager.dispose();
    }
}