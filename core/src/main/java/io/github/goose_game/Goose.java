package io.github.goose_game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Goose extends Entity {

    public enum GooseState {
        Chasing,
        Distracted,
        Dispersed
    }

    public static final float SIZE = 0.8f;

    private static final float STOP_DISTANCE = 0.1f;
    private static final float SEPARATION_DISTANCE = SIZE;
    private static final float SEPARATION_STRENGTH = 3f;
    private static final float MIN_MOVE = 0.2f;
    private static final float NIGHT_SPEED_MULTIPLIER = 1.3f; // placeholder, still needs tuning

    private final GooseSprites sprites;
    private final int variant;
    private GooseState state;
    private float speed;
    private Vector2 direction;
    private float distractionTimer; // seconds left until the goose goes back to chasing

    private Rectangle gooseBounds = new Rectangle();
    private float orbitAngle;
    private float orbitSpeed;

    private float stateTime = 0f;
    private boolean facingRight = true;
    private boolean moving = false;
    private boolean night = false;

    public Goose(GooseSprites sprites, float x, float y, int health) {
        super(health);
        this.sprites = sprites;
        this.variant = MathUtils.random(sprites.getVariantCount() - 1);
        this.sprite.setSize(SIZE, SIZE);
        this.sprite.setPosition(x, y);
        this.speed = 2f;
        this.direction = new Vector2();
        this.state = GooseState.Chasing;
        this.distractionTimer = 0f;

        gooseBounds.setSize(sprite.getWidth(), sprite.getHeight());
        gooseBounds.setPosition(sprite.getX(), sprite.getY());
        this.orbitAngle = MathUtils.random(360f);
        this.orbitSpeed = MathUtils.random(10f, 30f);
    }

    // targetPosition is the centre of the player
    public void update(float delta, Vector2 targetPosition, Array<Goose> geese) {
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
                break;
            case Chasing:
                if (targetPosition != null) {
                    chase(delta, targetPosition, geese);
                }
                break;
        }
        gooseBounds.setPosition(sprite.getX(), sprite.getY());
    }

    private void chase(float delta, Vector2 targetPosition, Array<Goose> geese) {
        float centerX = centerX();
        float centerY = centerY();

        // geese target a point near the player based on its relative angle
        float orbitRadius = 0.8f;
        float targetX = targetPosition.x + MathUtils.cosDeg(orbitAngle) * orbitRadius;
        float targetY = targetPosition.y + MathUtils.sinDeg(orbitAngle) * orbitRadius;

        // rotates the orbit angle randomly so geese move differently from each other and drift around
        orbitAngle += orbitSpeed * delta;

        direction.set(targetX - centerX, targetY - centerY);
        float distance = direction.len();
        if (distance > STOP_DISTANCE) {
            direction.scl(1f / distance);
        } else {
            direction.setZero();
        }
        addSeparation(centerX, centerY, geese);

        // Ignore small movements to reduce jitter.
        float strength = direction.len();
        if (strength < MIN_MOVE) {
            moving = false;
            return;
        }

        float currentSpeed = night ? speed * NIGHT_SPEED_MULTIPLIER : speed;
        float step = currentSpeed * delta * Math.min(strength, 1f);
        if (distance > STOP_DISTANCE) {
            step = Math.min(step, distance); // a long frame could otherwise carry the goose past its target
        }
        direction.scl(1f / strength);
        sprite.translate(direction.x * step, direction.y * step);

        moving = true;
        if (direction.x > 0f) {
            facingRight = true;
        } else if (direction.x < 0f) {
            facingRight = false;
        }
        stateTime += delta;
    }

    // Pushes this goose away from nearby geese so a swarm doesn't merge into one sprite.
    // Index loop because Game is already iterating geese and libGDX iterators can't be nested.
    private void addSeparation(float centerX, float centerY, Array<Goose> geese) {
        for (int i = 0; i < geese.size; i++) {
            Goose other = geese.get(i);
            if (other == this || other.state == GooseState.Dispersed) {
                continue;
            }

            float awayX = centerX - other.centerX();
            float awayY = centerY - other.centerY();
            float distance = Vector2.len(awayX, awayY);
            if (distance >= SEPARATION_DISTANCE) {
                continue;
            }

            // geese on the exact same spot have no direction to push in, so pick one at random
            if (distance == 0f) {
                float angle = MathUtils.random(360f);
                awayX = MathUtils.cosDeg(angle);
                awayY = MathUtils.sinDeg(angle);
            } else {
                awayX /= distance;
                awayY /= distance;
            }
            float push = (1f - distance / SEPARATION_DISTANCE) * SEPARATION_STRENGTH;
            direction.add(awayX * push, awayY * push);
        }
    }

    private float centerX() {
        return sprite.getX() + sprite.getWidth() / 2f;
    }

    private float centerY() {
        return sprite.getY() + sprite.getHeight() / 2f;
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        if (state != GooseState.Dispersed) {
            spriteBatch.draw(sprites.getFrame(variant, night, moving, facingRight, stateTime),
                sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
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
    }

    public void distract(float duration) {
        if (state == GooseState.Dispersed) {
            return;
        }
        this.state = GooseState.Distracted;
        this.distractionTimer = duration;
    }

    public GooseState getState() {
        return state;
    }

    public Rectangle getGooseBounds() {
        return gooseBounds;
    }

    // night gives the geese red eyes and makes them faster
    public void setNight(boolean night) {
        this.night = night;
    }
}
