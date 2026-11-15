package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

public class Goose extends Entity {

    public enum GooseState {
        Chasing,
        Distracted,
        Dispersed,
        Fleeing
    }

    private GooseState state;
    private float speed;
    private Vector2 direction;
    private float distractionTimer;

    private Texture spriteSheet;
    private Animation<TextureRegion> walkRight;
    private Animation<TextureRegion> walkLeft;
    private TextureRegion standRight;
    private TextureRegion standLeft;
    private float stateTime = 0f;
    private boolean facingRight = true;
    private boolean moving = false;

    // hit box and collision bounds
    private Rectangle gooseBounds = new Rectangle();
    private final float boundsWidth = 0.3f;
    private final float boundsHeight = 0.3f;

    // pathfinding and anti stuck variables
    private Array<Vector2> currentPath = new Array<>();
    private float pathRecalcTimer = 0f;
    private static final float RECALC_INTERVAL = 0.3f;

    private Vector2 lastPosition = new Vector2();
    private float stuckTimer = 0f;

    private Player player;

    private Vector2 fleeDirection = new Vector2();
    private float fleeChangeTimer = 0f;

    private static Sound disperseSound;

    private static final String[] GOOSE_TEXTURES = {
        "goose/goose_sheet_1x.png",
        "goose/duck_brown_sheet_1x.png",
        "goose/duck_grey_sheet_1x.png"
    };

    public Goose(String texturePath, float x, float y, int health, Player player) {
        super(texturePath, health, 1);

        if (disperseSound == null && Gdx.files.internal("sounds/disperse.ogg").exists()) {
            disperseSound = Gdx.audio.newSound(Gdx.files.internal("sounds/disperse.ogg"));
        }

        Random random = new Random();
        String selectedPath = GOOSE_TEXTURES[random.nextInt(GOOSE_TEXTURES.length)];
        spriteSheet = new Texture(selectedPath);
        spriteSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        TextureRegion[][] frames = TextureRegion.split(spriteSheet, 32, 32);

        walkRight = new Animation<>(0.15f, frames[1]);
        walkRight.setPlayMode(Animation.PlayMode.LOOP);
        walkLeft = new Animation<>(0.15f, frames[4]);
        walkLeft.setPlayMode(Animation.PlayMode.LOOP);

        standRight = frames[1][0];
        standLeft = frames[4][0];

        this.sprite.setSize(0.8f, 0.8f);
        this.sprite.setPosition(x, y);
        
        this.speed = 1.8f + MathUtils.random(0.0f, 0.4f);
        this.direction = new Vector2();
        this.state = GooseState.Chasing;
        this.distractionTimer = 0f;

        updateBounds();
        lastPosition.set(getCenterPosition());

        this.player = player;
    }

    private void updateBounds() {
        float offsetX = (sprite.getWidth() - boundsWidth) / 2f;
        float offsetY = (sprite.getHeight() - boundsHeight) / 2f;
        gooseBounds.set(sprite.getX() + offsetX, sprite.getY() + offsetY, boundsWidth, boundsHeight);
    }

    public Vector2 getCenterPosition() {
        return gooseBounds.getCenter(new Vector2());
    }

    public void update(float delta, Vector2 targetPosition, Array<Goose> geese, MapCollision mapCollision, PathFinder pathfinder) {
        switch (state) {
            case Dispersed:
                moving = false;
                break;
            case Distracted:
                moving = false;
                distractionTimer -= delta;
                if (distractionTimer <= 0) {
                    state = GooseState.Chasing;
                }
                return;
            case Fleeing:
                updateFleeing(delta, mapCollision);
                return;
            case Chasing:
                if (targetPosition == null) return;

                Vector2 gooseCenter = getCenterPosition();
                pathRecalcTimer += delta;

                if (gooseCenter.dst(lastPosition) < 0.03f) {
                    stuckTimer += delta;
                    if (stuckTimer > 0.2f) {
                        stuckTimer = 0f;
                        if (currentPath.size > 0) {
                            currentPath.removeIndex(0);
                        } else {
                            pathRecalcTimer = RECALC_INTERVAL;
                        }
                    }
                } else {
                    stuckTimer = 0f;
                    lastPosition.set(gooseCenter);
                }

                if (pathfinder != null && (pathRecalcTimer >= RECALC_INTERVAL || currentPath.isEmpty())) {
                    pathRecalcTimer = 0f;
                    currentPath = pathfinder.findPath(gooseCenter, targetPosition);
                }

                Vector2 targetPoint = targetPosition;
                if (currentPath.size > 0) {
                    targetPoint = currentPath.get(0);
                    if (gooseCenter.dst(targetPoint) < 0.35f) {
                        currentPath.removeIndex(0);
                        if (currentPath.size > 0) {
                            targetPoint = currentPath.get(0);
                        }
                    }
                }

                float distance = Vector2.dst(gooseCenter.x, gooseCenter.y, targetPoint.x, targetPoint.y);
                if (distance > 0.05f) {
                    direction.set(targetPoint.x - gooseCenter.x, targetPoint.y - gooseCenter.y).nor();

                    float oldX = sprite.getX();
                    sprite.setX(oldX + direction.x * speed * delta);
                    updateBounds();
                    if (mapCollision != null && mapCollision.collides(gooseBounds)) {
                        sprite.setX(oldX);
                        updateBounds();
                    }

                    float oldY = sprite.getY();
                    sprite.setY(oldY + direction.y * speed * delta);
                    updateBounds();
                    if (mapCollision != null && mapCollision.collides(gooseBounds)) {
                        sprite.setY(oldY);
                        updateBounds();
                    }

                    moving = true;
                    if (direction.x > 0f) facingRight = true;
                    else if (direction.x < 0f) facingRight = false;
                    stateTime += delta;
                } else {
                    moving = false;
                }

                if (geese != null) {
                    for (int i = 0; i < geese.size; i++) {
                        Goose other = geese.get(i);
                        if (other == this || other.getState() == GooseState.Dispersed) continue;

                        if (gooseBounds.overlaps(other.getGooseBounds())) {
                            Vector2 push = getCenterPosition().sub(other.getCenterPosition());
                            if (push.len2() < 0.0001f) {
                                push.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f));
                            }
                            push.nor().scl(1.2f * delta);

                            float sepX = sprite.getX();
                            sprite.setX(sepX + push.x);
                            updateBounds();
                            if (mapCollision != null && mapCollision.collides(gooseBounds)) {
                                sprite.setX(sepX);
                                updateBounds();
                            }

                            float sepY = sprite.getY();
                            sprite.setY(sepY + push.y);
                            updateBounds();
                            if (mapCollision != null && mapCollision.collides(gooseBounds)) {
                                sprite.setY(sepY);
                                updateBounds();
                            }
                        }
                    }
                }
                break;
        }
        updateBounds();
    }

    private void updateFleeing(float delta, MapCollision mapCollision) {
        fleeChangeTimer -= delta;
        if (fleeChangeTimer <= 0f || fleeDirection.len2() == 0f) {
            fleeDirection.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
            fleeChangeTimer = 1f + MathUtils.random(1f);
        }

        float oldX = sprite.getX();
        sprite.setX(oldX + fleeDirection.x * speed * delta);
        updateBounds();
        if (mapCollision != null && mapCollision.collides(gooseBounds)) {
            sprite.setX(oldX);
            updateBounds();
            fleeDirection.x = -fleeDirection.x;
        }

        float oldY = sprite.getY();
        sprite.setY(oldY + fleeDirection.y * speed * delta);
        updateBounds();
        if (mapCollision != null && mapCollision.collides(gooseBounds)) {
            sprite.setY(oldY);
            updateBounds();
            fleeDirection.y = -fleeDirection.y;
        }

        moving = true;
        if (fleeDirection.x > 0f) facingRight = true;
        else if (fleeDirection.x < 0f) facingRight = false;
        stateTime += delta;
    }

    public void update(float delta, Vector2 targetPosition, Array<Goose> geese) {
        update(delta, targetPosition, geese, null, null);
    }

    private static final float FLASH_BLINK_RATE = 0.08f;

    @Override
    public void draw(SpriteBatch spriteBatch) {
        if (state != GooseState.Dispersed) {
            TextureRegion frame;
            if (moving) {
                frame = facingRight ? walkRight.getKeyFrame(stateTime) : walkLeft.getKeyFrame(stateTime);
            } else {
                frame = facingRight ? standRight : standLeft;
            }

            if (state == GooseState.Fleeing) {
                boolean visible = ((int)(stateTime / FLASH_BLINK_RATE)) % 2 == 0;
                spriteBatch.setColor(visible ? Color.WHITE : Color.CLEAR);
            }

            spriteBatch.draw(frame, sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());

            if (state == GooseState.Fleeing) {
                spriteBatch.setColor(Color.WHITE);
            }
        }
    }

    @Override
    public void onCollision(Entity other_Entity) {
        health--;
        if (health <= 0) {
            disperse();
        }
    }

    public void disperse() {
        if (this.state == GooseState.Dispersed) return;
        this.state = GooseState.Dispersed;
        if (disperseSound != null) {
            disperseSound.play(0.7f);
        }
        if (player != null) {
            int currentScore = player.getScore();
            player.setScore(currentScore + 20);
        }
    }

    public void distract(float duration) {
        this.state = GooseState.Distracted;
        this.distractionTimer = duration;
    }

    public void flee() {
        if (state == GooseState.Dispersed) return;
        state = GooseState.Fleeing;
        fleeDirection.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
        fleeChangeTimer = 1f + MathUtils.random(1f);
        currentPath.clear();
    }

    public void stopFleeing() {
        if (state == GooseState.Fleeing) {
            state = GooseState.Chasing;
            currentPath.clear();
            pathRecalcTimer = RECALC_INTERVAL;
        }
    }

    public GooseState getState() {
        return state;
    }

    public Rectangle getGooseBounds() {
        return gooseBounds;
    }

    @Override
    public void dispose() {
        super.dispose();
        spriteSheet.dispose();
    }

    public static void disposeAudio() {
        if (disperseSound != null) {
            disperseSound.dispose();
            disperseSound = null;
        }
    }
}