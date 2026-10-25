package io.github.goose_game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

    public Goose(String texturePath, float x, float y, int health)
    {
        /* 
        This line is basically just an inheritance bridge
        It calls on the entity class so we can load health and assets
        I've put the documentation for it on the resources channel
        */
        super(texturePath, health); 
        this.sprite.setSize(200f, 200f);
        this.sprite.setPosition(x, y);
        this.speed = 100f; //(pixels per second)
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
                break;
            case Distracted:
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
                if (distance > 2f) {
                    direction.set(targetPosition.x - currentX, targetPosition.y - currentY).nor();
                    sprite.translate(direction.x * speed * delta, direction.y * speed * delta);
                }
                break;
            }

    }

    @Override 
    public void draw(SpriteBatch spriteBatch){
        if (state!=GooseState.Dispersed){
            super.draw(spriteBatch);
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

    //collision detection 

}
