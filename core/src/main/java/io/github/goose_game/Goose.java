package io.github.goose_game;

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

    public enum GooseState{
        Chasing,
        Distracted,
        Dispersed,
        Fleeing
    }

    private GooseState state;
    private float speed;
    private Vector2 direction;
    private float distractionTimer;
    private Vector2 fleeDirection = new Vector2();
    private float fleeChangeTimer = 0f;

    private Texture spriteSheet;
    private Animation<TextureRegion> walkRight;
    private Animation<TextureRegion> walkLeft;
    private TextureRegion standRight;
    private TextureRegion standLeft;
    private float stateTime = 0f;
    private boolean facingRight = true;
    private boolean moving = false;

    private Rectangle gooseBounds = new Rectangle();
    private static final float HITBOX_W = 14f;
    private static final float HITBOX_H = 14f;

    // pathfinding stuff
    private Array<Vector2> currentPath = new Array<>();
    private float pathRecalcTimer = 0f;
    private static final float RECALC_INTERVAL = 0.3f;
    private Vector2 lastPosition = new Vector2();
    private float stuckTimer = 0f;


    private static final String[] SHEET_PATHS = {"goose/goose_sheet_1x.png", "goose/duck_brown_sheet_1x.png", "goose/duck_grey_sheet_1x.png"};

    public Goose(String texturePath, float x, float y, int health)
    {
        super(SHEET_PATHS[0], health);

        String[] sheetPaths = SHEET_PATHS;
        Random random = new Random();
        spriteSheet = new Texture(sheetPaths[random.nextInt(sheetPaths.length)]);
        spriteSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);


        TextureRegion[][] frames = TextureRegion.split(spriteSheet, 32, 32);

        // row 2 (index 1) = walk right, row 5 (index 4) = walk left. other rows for blinking and mouth
        walkRight = new Animation<>(0.15f, frames[1]);
        walkRight.setPlayMode(Animation.PlayMode.LOOP);
        walkLeft = new Animation<>(0.15f, frames[4]);
        walkLeft.setPlayMode(Animation.PlayMode.LOOP);

        standRight = frames[1][0];
        standLeft = frames[4][0];

        this.sprite.setSize(32f, 32f);
        this.sprite.setPosition(x, y);
        this.speed = 65f;
        this.direction = new Vector2();
        this.state = GooseState.Chasing; // The goose has to be in a chasing state when it spawns
        this.distractionTimer = 0f;

        gooseBounds.setSize(HITBOX_W, HITBOX_H);
        syncBounds();
        lastPosition.set(sprite.getX(), sprite.getY());
    }

    //targetPosition is gonna be the coordinates of the player
    //This method basically runs every frame to update what the goose is doing/where its going
    public void update(float delta, Vector2 targetPosition, Array<Goose> geese,
                       Array<Rectangle> collisionRects, PathFinder pathfinder){
        switch (state)
        {
            case Dispersed:
                //No updates if goose is "dead"
                moving = false;
                break;
            case Distracted:
                moving = false;
                distractionTimer = distractionTimer - delta; //essentially decreasing the timer since the last frame
                if (distractionTimer <= 0){
                    state = GooseState.Chasing; // So the goose starts chasing the player again
                }
                return;
            case Fleeing:
                // run away in a random direction, changing every 1-2 seconds
                fleeChangeTimer -= delta;
                if (fleeChangeTimer <= 0f || fleeDirection.len2() == 0f) {
                    fleeDirection.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
                    fleeChangeTimer = 1f + MathUtils.random(1f);
                }

                float fleeOldX = sprite.getX();
                sprite.translateX(fleeDirection.x * speed * delta);
                syncBounds();
                if (overlapsAny(gooseBounds, collisionRects)) {
                    sprite.setX(fleeOldX);
                    syncBounds();
                    fleeDirection.x = -fleeDirection.x;
                }

                float fleeOldY = sprite.getY();
                sprite.translateY(fleeDirection.y * speed * delta);
                syncBounds();
                if (overlapsAny(gooseBounds, collisionRects)) {
                    sprite.setY(fleeOldY);
                    syncBounds();
                    fleeDirection.y = -fleeDirection.y;
                }

                moving = true;
                if (fleeDirection.x > 0f) facingRight = true;
                else if (fleeDirection.x < 0f) facingRight = false;
                stateTime += delta;
                break;
            case Chasing:
                if (targetPosition == null) return;

                Vector2 gooseCenter = getPosition();
                pathRecalcTimer += delta;

                // checks if the goose is stuck and skips to next waypoint if it is
                if (gooseCenter.dst(lastPosition) < 0.5f) {
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

                // recalculates the path every so often so it doesnt get lost
                if (pathfinder != null && (pathRecalcTimer >= RECALC_INTERVAL || currentPath.size == 0)) {
                    pathRecalcTimer = 0f;
                    currentPath = pathfinder.findPath(gooseCenter, targetPosition);
                }

                // gets the next point the goose needs to walk to
                Vector2 targetPoint = targetPosition;
                if (currentPath.size > 0) {
                    targetPoint = currentPath.get(0);
                    if (gooseCenter.dst(targetPoint) < 8f) {
                        currentPath.removeIndex(0);
                        if (currentPath.size > 0) {
                            targetPoint = currentPath.get(0);
                        } else {
                            targetPoint = targetPosition;
                        }
                    }
                }

                float distance = Vector2.dst(gooseCenter.x, gooseCenter.y, targetPoint.x, targetPoint.y);
                if (distance > 1f) {
                    direction.set(targetPoint.x - gooseCenter.x, targetPoint.y - gooseCenter.y).nor();

                    // wall collision, tries x then y so it slides along walls instead of getting stuck
                    float oldGX = sprite.getX();
                    sprite.translateX(direction.x * speed * delta);
                    syncBounds();
                    if (overlapsAny(gooseBounds, collisionRects)) {
                        sprite.setX(oldGX);
                        syncBounds();
                    }

                    float oldGY = sprite.getY();
                    sprite.translateY(direction.y * speed * delta);
                    syncBounds();
                    if (overlapsAny(gooseBounds, collisionRects)) {
                        sprite.setY(oldGY);
                        syncBounds();
                    }

                    moving = true;
                    if (direction.x > 0f) facingRight = true;
                    else if (direction.x < 0f) facingRight = false;
                    stateTime += delta;
                } else {
                    moving = false;
                }

                // stops geese from stacking on top of eachother
                if (geese != null) {
                    for (int i = 0; i < geese.size; i++) {
                        Goose other = geese.get(i);
                        if (other == this || other.getState() == GooseState.Dispersed) continue;
                        if (gooseBounds.overlaps(other.getGooseBounds())) {
                            Vector2 push = getPosition().sub(other.getPosition());
                            if (push.len2() < 0.01f) {
                                push.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f));
                            }
                            push.nor().scl(40f * delta);
                            float sepOldX = sprite.getX();
                            sprite.translateX(push.x);
                            syncBounds();
                            if (overlapsAny(gooseBounds, collisionRects)) {
                                sprite.setX(sepOldX);
                                syncBounds();
                            }
                            float sepOldY = sprite.getY();
                            sprite.translateY(push.y);
                            syncBounds();
                            if (overlapsAny(gooseBounds, collisionRects)) {
                                sprite.setY(sepOldY);
                                syncBounds();
                            }
                        }
                    }
                }
                break;
            }

        syncBounds();
    }

    private void syncBounds() {
        float offsetX = (sprite.getWidth() - HITBOX_W) / 2f;
        float offsetY = (sprite.getHeight() - HITBOX_H) / 2f;
        gooseBounds.setPosition(sprite.getX() + offsetX, sprite.getY() + offsetY);
    }

    private boolean overlapsAny(Rectangle bounds, Array<Rectangle> rects) {
        for (Rectangle r : rects) {
            if (bounds.overlaps(r)) return true;
        }
        return false;
    }

    private static final float FLASH_BLINK_RATE = 0.08f;

    @Override
    public void draw(SpriteBatch spriteBatch){

        if (state != GooseState.Dispersed){
            TextureRegion frame;
            if (moving) {
                frame = facingRight ? walkRight.getKeyFrame(stateTime) : walkLeft.getKeyFrame(stateTime);
            } else {
                frame = facingRight ? standRight : standLeft;
            }

            // flash while fleeing (golden feather active)
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
    public void onCollision(Entity other_Entity){
        health--;
        if (health<=0){
            disperse();
        }
    }

    public void disperse()
    {
        this.state = GooseState.Dispersed;
    }

    public void distract(float duration){
        this.state = GooseState.Distracted;
        this.distractionTimer = duration;
    }

    public void flee(){
        this.state = GooseState.Fleeing;
        this.fleeDirection.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
        this.fleeChangeTimer = 1f + MathUtils.random(1f);
        this.currentPath.clear();
    }

    public void stopFleeing(){
        if (this.state == GooseState.Fleeing) {
            this.state = GooseState.Chasing;
            this.currentPath.clear();
            this.pathRecalcTimer = RECALC_INTERVAL;
        }
    }

    public GooseState getState(){
        return state;
    }

    public Rectangle getGooseBounds(){
        return gooseBounds;
    }

    // puts the goose back to default so it starts chasing again
    public void resetState(){
        this.state = GooseState.Chasing;
        this.health = 3;
        this.distractionTimer = 0f;
        this.currentPath.clear();
        this.stuckTimer = 0f;
        this.pathRecalcTimer = 0f;
    }

    @Override
    public void dispose() {
        super.dispose();
        spriteSheet.dispose();
    }

    //collision detection

}
