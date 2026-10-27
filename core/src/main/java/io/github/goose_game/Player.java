package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Player extends Entity {

    private final float speed = 4f;

    public Player(float mapWidth, float mapHeight) {
        super("test_player.png", 100);
        sprite.setSize(1, 1);
        sprite.setPosition((mapWidth - sprite.getWidth()) / 2f, (mapHeight - sprite.getHeight()) / 2f);
    }

    public void update(FitViewport viewport, float mapWidth, float mapHeight) {
        input();
        playerGameLogic(viewport, mapWidth, mapHeight);
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

    private void playerGameLogic(FitViewport viewport, float mapWidth, float mapHeight) {
        float playerWidth = sprite.getWidth();
        float playerHeight = sprite.getHeight();

        sprite.setX(MathUtils.clamp(sprite.getX(), 0, mapWidth - playerWidth));
        sprite.setY(MathUtils.clamp(sprite.getY(), 0, mapHeight - playerHeight));

        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        float targetX = sprite.getX() + playerWidth / 2f;
        float targetY = sprite.getY() + playerHeight / 2f;
        float delta = Gdx.graphics.getDeltaTime();

        camera.position.x += (targetX - camera.position.x) * 5f * delta;
        camera.position.y += (targetY - camera.position.y) * 5f * delta;

        float halfViewportWidth = viewport.getWorldWidth() / 2f;
        float halfViewportHeight = viewport.getWorldHeight() / 2f;

        camera.position.x = MathUtils.clamp(camera.position.x, halfViewportWidth, mapWidth - halfViewportWidth);
        camera.position.y = MathUtils.clamp(camera.position.y, halfViewportHeight, mapHeight - halfViewportHeight);
        camera.update();
    }

    public void useItem() {
        // not sure how the item logic will work yet
    }

    @Override
    public void onCollision() {
        // we can do this later
    }
}
