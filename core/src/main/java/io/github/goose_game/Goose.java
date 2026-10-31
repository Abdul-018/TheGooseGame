package io.github.goose_game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Goose extends Entity {

    public enum GooseState{
        Chasing,
        Distracted,
        Dispersed
    }
    // I used enum because its the quickest and easiest way to manage geese states

    private GooseState state;
    private float speed;
    private Vector2 direction;
    private float distractionTimer; // COuntdown for how long the goose stays distracted for

    private Texture spriteSheet;
    private Animation<TextureRegion> walkRight;
    private Animation<TextureRegion> walkLeft;
    private TextureRegion standRight;
    private TextureRegion standLeft;
    private float stateTime = 0f;
    private boolean facingRight = true;
    private boolean moving = false;

    public Goose(String texturePath, float x, float y, int health)
    {
        /*
        This line is basically just an inheritance bridge
        It calls on the entity class so we can load health and assets
        I've put the documentation for it on the resources channel
        */
        super(texturePath, health);

        spriteSheet = new Texture("gooseSheet.png");
        TextureRegion[][] frames = TextureRegion.split(spriteSheet, 128, 128);

        // row 2 (index 1) = walk right, row 5 (index 4) = walk left. other rows for blinking and mouth
        walkRight = new Animation<>(0.15f, frames[1]);
        walkRight.setPlayMode(Animation.PlayMode.LOOP);
        walkLeft = new Animation<>(0.15f, frames[4]);
        walkLeft.setPlayMode(Animation.PlayMode.LOOP);

        standRight = frames[1][0];
        standLeft = frames[4][0];

        this.sprite.setSize(1f, 1f);
        this.sprite.setPosition(x, y);
        this.speed = 3f;
        this.direction = new Vector2();
        this.state = GooseState.Chasing; // The goose has to be in a chasing state when it spawns
        this.distractionTimer = 0f;

    }
    //targetPosition is gonna be the coordinates of the player
    //This method basically runs every frame to update what the goose is doing/where its going
    public void update(float delta, Vector2 targetPosition){
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
            case Chasing:
                if (targetPosition == null) return;
                float currentX = sprite.getX() + (sprite.getWidth() / 2f);
                float currentY = sprite.getY() + (sprite.getHeight() / 2f);

                float distance = Vector2.dst(currentX, currentY, targetPosition.x, targetPosition.y);
                if (distance > 0.1f) {
                    direction.set(targetPosition.x - currentX, targetPosition.y - currentY).nor();
                    sprite.translate(direction.x * speed * delta, direction.y * speed * delta);

                    moving = true;
                    if (direction.x > 0f) facingRight = true;
                    else if (direction.x < 0f) facingRight = false;
                    stateTime += delta;
                } else {
                    moving = false;
                }
                break;
            }

    }

    @Override
    public void draw(SpriteBatch spriteBatch){
        if (state != GooseState.Dispersed){
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
    public void onCollision(){
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

    public GooseState getState(){
        return state;
    }

    @Override
    public void dispose() {
        super.dispose();
        spriteSheet.dispose();
    }

    //collision detection

}
