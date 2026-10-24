package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Player extends Entity {

    private final float speed = 4f;

    public Player() {
        super("test_player.png", 100);
        sprite.setSize(1, 1);
    }

    public void update(FitViewport viewport) {
        input();
        keepInsideWorld(viewport);
    }

    private void input() {
        float delta = Gdx.graphics.getDeltaTime();

        float xMovement = 0f;
        float yMovement = 0f;

        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            xMovement += 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            xMovement -= 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            yMovement += 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN)) {
            yMovement -= 1f;
        }

        if (xMovement != 0f || yMovement != 0f) {
            float length = (float) Math.sqrt(xMovement * xMovement + yMovement * yMovement);

            xMovement /= length;
            yMovement /= length;

            sprite.translateX(xMovement * speed * delta);
            sprite.translateY(yMovement * speed * delta);
        }
    }

    private void keepInsideWorld(FitViewport viewport) {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        float playerWidth = sprite.getWidth();
        float playerHeight = sprite.getHeight();

        sprite.setX(MathUtils.clamp(sprite.getX(), 0, worldWidth - playerWidth));
        sprite.setY(MathUtils.clamp(sprite.getY(), 0, worldHeight - playerHeight));
    }

    public void useItem() {
        // not sure how the item logic will work yet
    }

    @Override
    public void onCollision() {
        // we can do this later
    }
}
