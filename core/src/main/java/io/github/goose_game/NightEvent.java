package io.github.goose_game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class NightEvent {

    // seconds since the game started, placeholder values still need tuning
    private static final float NIGHT_START = 120f;
    private static final float NIGHT_LENGTH = 20f;
    private static final Color NIGHT_COLOR = new Color(0.05f, 0.05f, 0.25f, 0.5f);

    private final Texture pixel;
    private float elapsed = 0f;

    public NightEvent() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
    }

    public void update(float delta) {
        elapsed += delta;
    }

    public boolean isNight() {
        return elapsed >= NIGHT_START && elapsed < NIGHT_START + NIGHT_LENGTH;
    }

    // call right after the map is drawn and before the entities, so only the map gets darker
    public void draw(SpriteBatch spriteBatch, OrthographicCamera camera) {
        if (!isNight()) {
            return;
        }
        float width = camera.viewportWidth * camera.zoom;
        float height = camera.viewportHeight * camera.zoom;
        Color previous = spriteBatch.getColor().cpy();
        spriteBatch.setColor(NIGHT_COLOR);
        spriteBatch.draw(pixel, camera.position.x - width / 2f, camera.position.y - height / 2f, width, height);
        spriteBatch.setColor(previous);
    }

    public void dispose() {
        pixel.dispose();
    }
}
