package io.github.goose_game;

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
        Dispersed
    }
    // i used enum because its the quickest and easiest way to manage geese states

    private GooseState state;
    private float speed;
    private Vector2 direction;
    private float distractionTimer; // countdown for how long the goose stays distracted for

    private Texture spriteSheet;
    private Texture spriteSheet2;
    private Texture spriteSheet3;
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

    // pathfinding and anti stuck variables from code 2
    private Array<Vector2> currentPath = new Array<>();
    private float pathRecalcTimer = 0f;
    private static final float RECALC_INTERVAL = 0.3f;

    private Vector2 lastPosition = new Vector2();
    private float stuckTimer = 0f;

    private Player player;

    public Goose(String texturePath, float x, float y, int health, Player player) {
        /*
        this line is basically just an inheritance bridge
        it calls on the entity class so we can load health and assets
        ive put the documentation for it on the resources channel
        */
        super(texturePath, health, 1);

        spriteSheet = new Texture("gooseSheet.png");
        spriteSheet2 = new Texture("gooseSheet2.png");
        spriteSheet3 = new Texture("gooseSheet3.png");

        Random random = new Random();
        Texture[] textures = {spriteSheet, spriteSheet2, spriteSheet3};
        Texture selectedTexture = textures[random.nextInt(textures.length)];

        TextureRegion[][] frames = TextureRegion.split(selectedTexture, 128, 128);

        // row 2 index 1 is walk right row 5 index 4 is walk left other rows for blinking and mouth
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
        this.state = GooseState.Chasing; // the goose has to be in a chasing state when it spawns
        this.distractionTimer = 0f;

        updateBounds();
        lastPosition.set(getCenterPosition());

        this.player = player;
    }

    // syncs the collision bounds with the sprites current position
    private void updateBounds() {
        float offsetX = (sprite.getWidth() - boundsWidth) / 2f;
        float offsetY = (sprite.getHeight() - boundsHeight) / 2f;
        gooseBounds.set(sprite.getX() + offsetX, sprite.getY() + offsetY, boundsWidth, boundsHeight);
    }

    // gets the exact middle of the goose for distance checks
    public Vector2 getCenterPosition() {
        return gooseBounds.getCenter(new Vector2());
    }

    // overloaded update method supporting pathfinder and mapcollision code 2 while keeping player context
    public void update(float delta, Vector2 targetPosition, Array<Goose> geese, MapCollision mapCollision, PathFinder pathfinder) {
        switch (state) {
            case Dispersed:
                // no updates if goose is dead
                moving = false;
                break;
            case Distracted:
                moving = false;
                distractionTimer -= delta; // essentially decreasing the timer since the last frame
                if (distractionTimer <= 0) {
                    state = GooseState.Chasing; // so the goose starts chasing the player again
                }
                return;
            case Chasing:
                if (targetPosition == null) return;

                Vector2 gooseCenter = getCenterPosition();
                pathRecalcTimer += delta;

                // make sure geese dont get stuck in corners or obstacles
                if (gooseCenter.dst(lastPosition) < 0.03f) {
                    stuckTimer += delta;
                    if (stuckTimer > 0.2f) {
                        stuckTimer = 0f;
                        if (currentPath.size > 0) {
                            currentPath.removeIndex(0); // drop current waypoint and try next
                        } else {
                            pathRecalcTimer = RECALC_INTERVAL; // force recalculate path right now
                        }
                    }
                } else {
                    stuckTimer = 0f;
                    lastPosition.set(gooseCenter);
                }

                // ask the pathfinder for a new route if timer is up or we have no path
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

                    // move on x axis and check wall collisions
                    float oldX = sprite.getX();
                    sprite.setX(oldX + direction.x * speed * delta);
                    updateBounds();
                    if (mapCollision != null && mapCollision.collides(gooseBounds)) {
                        sprite.setX(oldX);
                        updateBounds();
                    }

                    // move on y axis and check wall collisions
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

                // entity separation loop so geese dont stack on top of each other
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

                            // push away on x
                            float sepX = sprite.getX();
                            sprite.setX(sepX + push.x);
                            updateBounds();
                            if (mapCollision != null && mapCollision.collides(gooseBounds)) {
                                sprite.setX(sepX);
                                updateBounds();
                            }

                            // push away on y
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

    // keep original update signature compatible if called without pathfinder or collision
    public void update(float delta, Vector2 targetPosition, Array<Goose> geese) {
        update(delta, targetPosition, geese, null, null);
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        if (state != GooseState.Dispersed) {
            TextureRegion frame;
            if (moving) {
                frame = facingRight ? walkRight.getKeyFrame(stateTime) : walkLeft.getKeyFrame(stateTime);
            } else {
                frame = facingRight ? standRight : standLeft;
            }
            spriteBatch.draw(frame, sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
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
        this.state = GooseState.Dispersed;
        if (player != null) {
            int currentScore = player.getScore();
            player.setScore(currentScore + 20);
        }
    }

    public void distract(float duration) {
        this.state = GooseState.Distracted;
        this.distractionTimer = duration;
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
        spriteSheet2.dispose();
        spriteSheet3.dispose();
    }
}