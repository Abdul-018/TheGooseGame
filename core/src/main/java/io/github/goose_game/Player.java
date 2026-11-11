package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class Player extends Entity {

    private float speed = 3f;

    private Texture spriteSheet;
    private Animation<TextureRegion> walkRight;
    private Animation<TextureRegion> walkLeft;
    private TextureRegion standRight;
    private TextureRegion standLeft;
    private float stateTime = 0f;

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    };

    private Direction facing = Direction.DOWN;

    private Animation<TextureRegion> walkUp;
    private Animation<TextureRegion> walkDown;
    private TextureRegion standUp;
    private TextureRegion standDown;
    private boolean moving = false;
    private float flashTimer = 0f;
    private Rectangle playerBounds = new Rectangle();
    private final Rectangle feetBounds = new Rectangle();
    private Game game;
    private boolean isFlashing = false;
    private static final float FLASH_BLINK_RATE = 0.08f;
    private BitmapFont collisionFont;
    private SpriteBatch fontBatch;
    private int score;

    public Player(float mapWidth, float mapHeight) {
        super("test_player.png", 100, 1);

        spriteSheet = new Texture("StudentSheet2.png");
        TextureRegion[][] frames = TextureRegion.split(spriteSheet, 32, 32);

        TextureRegion[] walkingDirection = { frames[4][0], frames[4][1], frames[4][2] };
        TextureRegion[] walkingDirectionReversed = new TextureRegion[3];
        for (int i = 0; i < 3; i++) {
            walkingDirectionReversed[i] = new TextureRegion(walkingDirection[i]);
            walkingDirectionReversed[i].flip(true, false);
        }
        walkUp = new Animation<>(0.15f, frames[3][0], frames[3][1], frames[3][2]);
        walkUp.setPlayMode(Animation.PlayMode.LOOP);
        walkDown = new Animation<>(0.15f, frames[5][0], frames[5][1], frames[5][2]);
        walkDown.setPlayMode(Animation.PlayMode.LOOP);
        walkRight = new Animation<>(0.15f, walkingDirection);
        walkRight.setPlayMode(Animation.PlayMode.LOOP);
        walkLeft = new Animation<>(0.15f, walkingDirectionReversed);
        walkLeft.setPlayMode(Animation.PlayMode.LOOP);

        standRight = frames[1][0];
        standLeft = new TextureRegion(frames[1][0]);
        standLeft.flip(true, false);
        standUp = frames[0][0];
        standDown = frames[2][0];

        sprite.setSize(2f, 2f);
        // Spawn on Tiled tile (47, 32). Tiled counts rows from the top, libGDX from the
        // bottom,
        // so row 32 is y = 99 - 32 = 67. x is offset by half a tile so she's centred on
        // the tile.
        sprite.setPosition(46.5f, 67f);
        playerBounds.setSize(sprite.getWidth(), sprite.getHeight());
        playerBounds.setPosition(sprite.getX(), sprite.getY());

        collisionFont = new BitmapFont();
        collisionFont.setColor(Color.WHITE);
        fontBatch = new SpriteBatch();

    }

    public void update(FitViewport viewport, float mapWidth, float mapHeight, Game game) {
        this.game = game;
        input();
        playerGameLogic(viewport, mapWidth, mapHeight);

        for (Goose g : game.getGeese()) {
            if (playerBounds.overlaps(g.getGooseBounds())) {
                onCollision(g);
            }
        }
        for (Item i : game.getItems()) {
            if (playerBounds.overlaps(i.getItemBounds())) {
                if (!i.inInventory) {
                    i.pickUp();
                }
            }
        }

        if (isFlashing) {
            flashTimer -= Gdx.graphics.getDeltaTime();
            boolean visible = ((int) (flashTimer / FLASH_BLINK_RATE)) % 2 == 0;
            sprite.setColor(visible ? Color.WHITE : Color.CLEAR);

            if (flashTimer <= 0) {
                isFlashing = false;
                sprite.setColor(Color.WHITE);
            }
        }

    }

    private void input() {
        float delta = Gdx.graphics.getDeltaTime();

        float xMovement = 0f;
        float yMovement = 0f;

        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {
            xMovement += 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
            xMovement -= 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) {
            yMovement += 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) {
            yMovement -= 1f;
        }

        if (xMovement != 0f || yMovement != 0f) {
            float length = (float) Math.sqrt(xMovement * xMovement + yMovement * yMovement);

            xMovement /= length;
            yMovement /= length;

            move(xMovement * speed * delta, yMovement * speed * delta);

            moving = true;
            if (Math.abs(xMovement) >= Math.abs(yMovement)) {
                facing = xMovement > 0f ? Direction.RIGHT : Direction.LEFT;
            } else {
                facing = yMovement > 0f ? Direction.UP : Direction.DOWN;
            }
            stateTime += delta;
        } else {
            moving = false;
            stateTime = 0f;
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        TextureRegion frame;

        switch (facing) {
            case UP:
                frame = moving ? walkUp.getKeyFrame(stateTime) : standUp;
                break;
            case DOWN:
                frame = moving ? walkDown.getKeyFrame(stateTime) : standDown;
                break;
            case LEFT:
                frame = moving ? walkLeft.getKeyFrame(stateTime) : standLeft;
                break;
            default:
                frame = moving ? walkRight.getKeyFrame(stateTime) : standRight;
                break;
        }

        spriteBatch.setColor(sprite.getColor());

        spriteBatch.draw(frame, sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());

        spriteBatch.setColor(Color.WHITE);

    }

    private void playerGameLogic(FitViewport viewport, float mapWidth, float mapHeight) {
        float playerWidth = sprite.getWidth();
        float playerHeight = sprite.getHeight();

        sprite.setX(MathUtils.clamp(sprite.getX(), 0, mapWidth - playerWidth));
        sprite.setY(MathUtils.clamp(sprite.getY(), 0, mapHeight - playerHeight));

        playerBounds.setPosition(sprite.getX(), sprite.getY());

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

    public void useItem() {
        // not sure how the item logic will work yet
    }

    @Override
    public void onCollision(Entity other_Entity) {
        if (other_Entity instanceof Goose) {
            gooseHit((Goose) other_Entity);
        }
    }

    private void gooseHit(Goose goose) {
        if (isFlashing)
            return;

        this.health -= 1;
        isFlashing = true;
        flashTimer = 1.5f;

        Vector2 playerCenter = getPosition();
        Vector2 gooseCenter = goose.getPosition();
        Vector2 knockback = new Vector2(playerCenter.x - gooseCenter.x, playerCenter.y - gooseCenter.y);
        if (knockback.len2() > 0) {
            knockback.nor().scl(1.5f);
        } else {
            knockback.set(0, 1.5f);
        }
        sprite.translate(knockback.x, knockback.y);
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

    private void updateFeet() {
        float w = sprite.getWidth();
        float h = sprite.getHeight();

        feetBounds.set(sprite.getX() + w * 0.28f,
                sprite.getY() + h * 0.125f, w * 0.44f, h * 0.235f);

    }

    private void move(float dx, float dy) {
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / 0.25f));
        float stepX = dx / steps;
        float stepY = dy / steps;

        for (int i = 0; i < steps; i++) {
            sprite.translateX(stepX);
            updateFeet();
            if (game.collisionChecker(feetBounds)) {
                sprite.translateX(-stepX);
            }

            sprite.translateY(stepY);
            updateFeet();
            if (game.collisionChecker(feetBounds)) {
                sprite.translateY(-stepY);
            }
        }
    }
}
