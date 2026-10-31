package io.github.goose_game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

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

    public Vector2 getPosition() {
        return new Vector2(sprite.getX(), sprite.getY());
    }

    public Sprite getSprite() {
        return sprite;
    }

    public int getHealth() {
        return health;
    }

    public void dispose() {
        texture.dispose();
    }
}
