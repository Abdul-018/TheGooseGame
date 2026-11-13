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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;



public class Player extends Entity {

    private float speed = 130f;
    private int score = 0;

    private Texture spriteSheet;
    private Animation<TextureRegion> walkDown;
    private Animation<TextureRegion> walkUp;
    private Animation<TextureRegion> walkLeft;
    private Animation<TextureRegion> walkRight;
    private TextureRegion standDown;
    private TextureRegion standUp;
    private TextureRegion standLeft;
    private TextureRegion standRight;
    private float stateTime = 0f;
    private enum Direction { DOWN, UP, LEFT, RIGHT }
    private Direction facing = Direction.DOWN;
    private boolean moving = false;
    private float flashTimer = 0f;
    private Rectangle playerBounds = new Rectangle();
    private boolean isFlashing = false;
    private static final float FLASH_BLINK_RATE = 0.08f;
    private static final float HITBOX_W = 14f;
    private static final float HITBOX_H = 14f;
    private BitmapFont collisionFont;
    private SpriteBatch fontBatch;
    private Array<Rectangle> currentCollisionRects;

    public Player(float mapWidth, float mapHeight) {
        super("player/playerSheet.png", 100);

        spriteSheet = new Texture("player/playerSheet.png");
        spriteSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[][] frames = TextureRegion.split(spriteSheet, 32, 32);

        walkUp = new Animation<>(0.15f, new TextureRegion[]{frames[3][0], frames[3][1], frames[3][2]});
        walkUp.setPlayMode(Animation.PlayMode.LOOP);
        walkRight = new Animation<>(0.15f, new TextureRegion[]{frames[4][0], frames[4][1], frames[4][2]});
        walkRight.setPlayMode(Animation.PlayMode.LOOP);
        walkDown = new Animation<>(0.15f, new TextureRegion[]{frames[5][0], frames[5][1], frames[5][2]});
        walkDown.setPlayMode(Animation.PlayMode.LOOP);

        // flipping the right walk frames for left cos theres no left sprites
        TextureRegion[] leftFrames = new TextureRegion[3];
        for (int i = 0; i < 3; i++) {
            leftFrames[i] = new TextureRegion(frames[4][i]);
            leftFrames[i].flip(true, false);
        }
        walkLeft = new Animation<>(0.15f, leftFrames);
        walkLeft.setPlayMode(Animation.PlayMode.LOOP);

        standUp = frames[0][0];
        standRight = frames[1][0];
        standDown = frames[2][0];
        standLeft = new TextureRegion(frames[1][0]);
        standLeft.flip(true, false);

        sprite.setSize(32f, 32f);
        sprite.setPosition((mapWidth - sprite.getWidth()) / 2f, (mapHeight - sprite.getHeight()) / 2f);
        playerBounds.setSize(HITBOX_W, HITBOX_H);
        syncBounds();

        collisionFont = new BitmapFont();
        collisionFont.setColor(Color.WHITE);
        fontBatch = new SpriteBatch();
    }

    public void update(Viewport viewport, float mapWidth, float mapHeight, Game game, Array<Rectangle> collisionRects) {
        this.currentCollisionRects = collisionRects;
        input(collisionRects);
        playerGameLogic(viewport, mapWidth, mapHeight);

        Rectangle spriteBounds = new Rectangle(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
        for (Goose g : game.getGeese()){
            Rectangle gooseSprite = new Rectangle(g.getSprite().getX(), g.getSprite().getY(), g.getSprite().getWidth(), g.getSprite().getHeight());
            if (spriteBounds.overlaps(gooseSprite)){
                if (g.getState() == Goose.GooseState.Fleeing) {
                    // pacman style: running into a fleeing goose disperses it
                    g.disperse();
                    this.score += 20;
                } else {
                    onCollision(g);
                }
            }
        }
        if (isFlashing){
            flashTimer -= Gdx.graphics.getDeltaTime();
            boolean visible = ((int)(flashTimer / FLASH_BLINK_RATE)) % 2 == 0;
            sprite.setColor(visible ? Color.WHITE : Color.CLEAR);

            if (flashTimer <= 0){
                isFlashing = false;
                sprite.setColor(Color.WHITE);
            }
        }


    }

    private void input(Array<Rectangle> collisionRects) {
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

            // try moving on x first
            float oldX = sprite.getX();
            sprite.translateX(xMovement * speed * delta);
            syncBounds();
            if (overlapsAny(playerBounds, collisionRects)) {
                sprite.setX(oldX);
                syncBounds();
            }

            // then try y
            float oldY = sprite.getY();
            sprite.translateY(yMovement * speed * delta);
            syncBounds();
            if (overlapsAny(playerBounds, collisionRects)) {
                sprite.setY(oldY);
                syncBounds();
            }

            moving = true;
            if (Math.abs(xMovement) >= Math.abs(yMovement)) {
                facing = xMovement > 0 ? Direction.RIGHT : Direction.LEFT;
            } else {
                facing = yMovement > 0 ? Direction.UP : Direction.DOWN;
            }
            stateTime += delta;
        } else {
            moving = false;
            stateTime = 0f;
        }
    }

    private void syncBounds() {
        float offsetX = (sprite.getWidth() - HITBOX_W) / 2f;
        float offsetY = (sprite.getHeight() - HITBOX_H) / 2f;
        playerBounds.setPosition(sprite.getX() + offsetX, sprite.getY() + offsetY);
    }

    private boolean overlapsAny(Rectangle bounds, Array<Rectangle> rects) {
        for (Rectangle r : rects) {
            if (bounds.overlaps(r)) return true;
        }
        return false;
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        TextureRegion frame;

        if (moving) {
            switch (facing) {
                case UP:    frame = walkUp.getKeyFrame(stateTime); break;
                case LEFT:  frame = walkLeft.getKeyFrame(stateTime); break;
                case RIGHT: frame = walkRight.getKeyFrame(stateTime); break;
                default:    frame = walkDown.getKeyFrame(stateTime); break;
            }
        } else {
            switch (facing) {
                case UP:    frame = standUp; break;
                case LEFT:  frame = standLeft; break;
                case RIGHT: frame = standRight; break;
                default:    frame = standDown; break;
            }
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

        syncBounds();


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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void useItem() {
        // not sure how the item logic will work yet
    }

    @Override
    public void onCollision(Entity other_Entity) {
        if (other_Entity instanceof Goose){
            gooseHit((Goose) other_Entity);
        }
    }

    private void gooseHit(Goose goose){
        if (isFlashing) return;

        this.health -= 1;
        isFlashing = true;
        flashTimer = 1.5f;

        Vector2 playerCenter = getPosition();
        Vector2 gooseCenter = goose.getPosition();
        Vector2 dir = new Vector2(playerCenter.x - gooseCenter.x, playerCenter.y - gooseCenter.y);
        if (dir.len2() > 0) {
            dir.nor();
        } else {
            dir.set(0, 1f);
        }

        // moves the player pixel by pixel so they dont get knocked through a wall
        float startX = sprite.getX();
        float startY = sprite.getY();
        float lastSafeX = startX;
        float lastSafeY = startY;
        for (int i = 1; i <= 100; i++) {
            sprite.setPosition(startX + dir.x * i, startY + dir.y * i);
            syncBounds();
            if (currentCollisionRects != null && overlapsAny(playerBounds, currentCollisionRects)) {
                break;
            }
            lastSafeX = sprite.getX();
            lastSafeY = sprite.getY();
        }
        sprite.setPosition(lastSafeX, lastSafeY);
        syncBounds();
    }


    @Override
    public void dispose() {
        super.dispose();
        spriteSheet.dispose();
        collisionFont.dispose();
        fontBatch.dispose();
    }
}
