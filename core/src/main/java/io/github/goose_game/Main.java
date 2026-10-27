package io.github.goose_game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Goose testGoose;
    private Vector2 targetPosition;

    @Override
    public void create() {
        batch = new SpriteBatch();

        testGoose = new Goose("goose.png", 10, 10, 1);

        targetPosition = new Vector2();
    }
    
    private Rectangle mouseHitbox = new Rectangle(0, 0, 32, 32);

    @Override
    public void render() {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY(); 
        mouseHitbox.set(mouseX - (mouseHitbox.width / 2f), mouseY - (mouseHitbox.height / 2f), 32, 32);

        float delta = Gdx.graphics.getDeltaTime();
        targetPosition.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
        testGoose.update(delta, targetPosition);
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        testGoose.draw(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        testGoose.dispose();
    }


}