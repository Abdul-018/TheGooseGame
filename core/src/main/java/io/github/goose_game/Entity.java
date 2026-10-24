package io.github.goose_game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Entity {

    protected Texture texture;
    protected Sprite sprite;
    protected int health;

    public Entity(String texturePath, int health) {
        this.texture = new Texture(texturePath);
        this.sprite = new Sprite(texture);
        this.health = health;
    }

    public void draw(SpriteBatch spriteBatch) {
        sprite.draw(spriteBatch);
    }

    public void onCollision() {
        // this will be inherited by the player and goose? maybe also item?
    }

    public void dispose() {
        texture.dispose();
    }
}
