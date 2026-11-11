package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.goose_game.Menus.LoseScreenMenu;
import io.github.goose_game.Menus.PauseMenu;

public class Player extends Entity {

    private float speed = 3f;

    private Texture spriteSheet;
    private Animation<TextureRegion> walkRight;
    private Animation<TextureRegion> walkLeft;
    private TextureRegion standRight;
    private TextureRegion standLeft;
    private float stateTime = 0f;
    private boolean facingRight = true;
    private boolean moving = false;
    private float flashTimer = 0f;

    // Precise collision bounds and sizing (from Code 2)
    private Rectangle playerBounds = new Rectangle();
    private final float boundsWidth = 0.35f;
    private final float boundsHeight = 0.35f;

    private boolean isFlashing = false;
    private static final float FLASH_BLINK_RATE = 0.08f;
    private BitmapFont collisionFont;
    private SpriteBatch fontBatch;

    private static int score;
    private int maxHealth = 10;

    public Player(float mapWidth, float mapHeight) {
        super("test_player.png", 100, 1);

        spriteSheet = new Texture("studentSheet.png");
        TextureRegion[][] frames = TextureRegion.split(spriteSheet, 128, 128);

        walkRight = new Animation<>(0.15f, frames[1]);
        walkRight.setPlayMode(Animation.PlayMode.LOOP);
        walkLeft = new Animation<>(0.15f, frames[4]);
        walkLeft.setPlayMode(Animation.PlayMode.LOOP);

        standRight = frames[1][0];
        standLeft = frames[4][0];

        sprite.setSize(0.8f, 0.8f);
        sprite.setPosition((mapWidth - sprite.getWidth()) / 2f, (mapHeight - sprite.getHeight()) / 2f);
        
        updateBounds();

        collisionFont = new BitmapFont();
        collisionFont.setColor(Color.WHITE);
        fontBatch = new SpriteBatch();
    }

    private void updateBounds() {
        float offsetX = (sprite.getWidth() - boundsWidth) / 2f;
        float offsetY = (sprite.getHeight() - boundsHeight) / 2f;
        playerBounds.set(sprite.getX() + offsetX, sprite.getY() + offsetY, boundsWidth, boundsHeight);
    }

    public Vector2 getCenterPosition() {
        return playerBounds.getCenter(new Vector2());
    }

    public void update(Viewport viewport, float mapWidth, float mapHeight, Game game, MapCollision mapCollision) {
        input(mapCollision);
        playerGameLogic(viewport, mapWidth, mapHeight);

        // Check collisions with geese
        for (int i = 0; i < game.getGeese().size; i++) {
            Goose g = game.getGeese().get(i);
            if (playerBounds.overlaps(g.getGooseBounds())) {
                onCollision(g, mapCollision);
            }
        }

        // Check collisions with items (from Code 1)
        for (Item item : game.getItems()) {
            if (playerBounds.overlaps(item.getItemBounds())) {
                if (!item.inInventory) {
                    item.pickUp();
                }
            }
        }

        if (isFlashing) {
            flashTimer -= Gdx.graphics.getDeltaTime();
            boolean visible = ((int)(flashTimer / FLASH_BLINK_RATE)) % 2 == 0;
            sprite.setColor(visible ? Color.WHITE : Color.CLEAR);

            if (flashTimer <= 0) {
                isFlashing = false;
                sprite.setColor(Color.WHITE);
            }
        }
    }

    public void setHealth(int healthInput) {
        if (healthInput < maxHealth) {
            health = healthInput;
        } else {
            health = maxHealth;
        }
    }

    private void input(MapCollision mapCollision) {
        float delta = Gdx.graphics.getDeltaTime();

        float xMovement = 0f;
        float yMovement = 0f;

        // Combined WASD and Arrow key support (from Code 1)
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) xMovement += 1f;
        if (Gdx.input.isKeyPressed(Keys.LEFT)  || Gdx.input.isKeyPressed(Keys.A)) xMovement -= 1f;
        if (Gdx.input.isKeyPressed(Keys.UP)    || Gdx.input.isKeyPressed(Keys.W)) yMovement += 1f;
        if (Gdx.input.isKeyPressed(Keys.DOWN)  || Gdx.input.isKeyPressed(Keys.S)) yMovement -= 1f;

        if (xMovement != 0f || yMovement != 0f) {
            float length = (float) Math.sqrt(xMovement * xMovement + yMovement * yMovement);
            xMovement /= length;
            yMovement /= length;

            // X-axis movement and map collision sliding (from Code 2)
            float oldX = sprite.getX();
            sprite.setX(oldX + xMovement * speed * delta);
            updateBounds();
            if (mapCollision != null && mapCollision.collides(playerBounds)) {
                sprite.setX(oldX);
                updateBounds();
            }

            // Y-axis movement and map collision sliding (from Code 2)
            float oldY = sprite.getY();
            sprite.setY(oldY + yMovement * speed * delta);
            updateBounds();
            if (mapCollision != null && mapCollision.collides(playerBounds)) {
                sprite.setY(oldY);
                updateBounds();
            }

            moving = true;
            if (xMovement > 0f) facingRight = true;
            else if (xMovement < 0f) facingRight = false;
            stateTime += delta;
        } else {
            moving = false;
            stateTime = 0f;
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        TextureRegion frame;
        if (moving) {
            frame = facingRight ? walkRight.getKeyFrame(stateTime) : walkLeft.getKeyFrame(stateTime);
        } else {
            frame = facingRight ? standRight : standLeft;
        }

        spriteBatch.setColor(sprite.getColor());
        spriteBatch.draw(frame, sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
        spriteBatch.setColor(Color.WHITE);
    }

    private void playerGameLogic(Viewport viewport, float mapWidth, float mapHeight) {
        float playerWidth = sprite.getWidth();
        float playerHeight = sprite.getHeight();

        sprite.setX(MathUtils.clamp(sprite.getX(), 0, mapWidth - playerWidth));
        sprite.setY(MathUtils.clamp(sprite.getY(), 0, mapHeight - playerHeight));
        updateBounds();

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

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public Rectangle getPlayerBounds() {
        return playerBounds;
    }

    public void useItem() {
        // Reserved for future item usage logic
    }

    @Override
    public void onCollision(Entity other_Entity) {
        // Fallback overload
    }

    public void onCollision(Entity other_Entity, MapCollision mapCollision) {
        if (other_Entity instanceof Goose) {
            gooseHit((Goose) other_Entity, mapCollision);
        }
    }

    private void gooseHit(Goose goose, MapCollision mapCollision) {
        if (isFlashing) return;

        this.health -= 1;
        if (health <= 0) {
            Game.gameOver = true;
            PauseMenu.pauseMenu.Close();
            LoseScreenMenu.loseMenu.Open();
            return;
        }

        isFlashing = true;
        flashTimer = 1.5f;

        Vector2 playerCenter = getCenterPosition();
        Vector2 gooseCenter = goose.getCenterPosition();
        Vector2 knockback = new Vector2(playerCenter.x - gooseCenter.x, playerCenter.y - gooseCenter.y);
        if (knockback.len2() > 0) {
            knockback.nor().scl(1.2f);
        } else {
            knockback.set(0, 1.2f);
        }

        // Incremental knockback to prevent pushing into map collision walls (from Code 2)
        float totalDist = knockback.len();
        Vector2 stepDir = new Vector2(knockback).nor();
        float stepSize = 0.05f;
        float moved = 0f;

        while (moved < totalDist) {
            float nextStep = Math.min(stepSize, totalDist - moved);

            float oldX = sprite.getX();
            sprite.setX(oldX + stepDir.x * nextStep);
            updateBounds();
            if (mapCollision != null && mapCollision.collides(playerBounds)) {
                sprite.setX(oldX);
                updateBounds();
            }

            float oldY = sprite.getY();
            sprite.setY(oldY + stepDir.y * nextStep);
            updateBounds();
            if (mapCollision != null && mapCollision.collides(mapCollision.collides(playerBounds) ? playerBounds : playerBounds) == true) { // simplified safely below
                // handled by mapCollision check
            }
            // Correct block for Y axis check:
            if (mapCollision != null && mapCollision.collides(playerBounds)) {
                sprite.setY(oldY);
                updateBounds();
            }

            moved += nextStep;
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int newScore) {
        this.score = newScore;
    }

    @Override
    public void dispose() {
        super.dispose();
        spriteSheet.dispose();
        collisionFont.dispose();
        fontBatch.dispose();
    }
}