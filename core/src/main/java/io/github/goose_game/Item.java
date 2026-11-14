package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Item extends Entity {

    public boolean inInventory;
    public String invGraphicInternalAddress;
    public boolean itemUsed = false;
    public static float effectDuration;
    private Player player;
    private SpriteBatch spriteBatch;
    public float xCoord;
    public float yCoord;
    private Rectangle itemBounds = new Rectangle();
    public boolean collected;
    protected Game game;

    // bobbing animation
    private float bounceTimer;
    private static final float BOUNCE_HEIGHT = 0.05f;
    private static final float BOUNCE_SPEED = 3f;


    public Item(String texturePath, float scale, Game game) {
        super(texturePath, 1, scale);
        sprite.setSize(scale,scale);
        this.game = game;
        invGraphicInternalAddress = "items/empty.png";
        bounceTimer = MathUtils.random(0f, MathUtils.PI2);
    }

    public void updateBounds(){
        itemBounds.setSize(sprite.getWidth(), sprite.getHeight());
        itemBounds.setPosition(sprite.getX(), sprite.getY());
    }

    public void pickUp(){
    //fixed the error causing ghost inventory
    if (this.collected) {
        return;
    }
    if (game.getInventory().isFull()) {
        return;
    }
    this.collected = true;
    this.inInventory = true;
    game.getInventory().addItem(this);
}

    public void useItem(Player player, Array<Goose> geese, SpriteBatch spriteBatch){
        if(this.inInventory && !this.itemUsed){
            this.itemAction(player, geese, spriteBatch);

            int currentScore = player.getScore();
            player.setScore(currentScore + 10);

            this.inInventory = false;
            this.itemUsed = true;
            game.getInventory().removeItem(this);
           
        }
    }

    public void onCollision(Entity otherEntity){ 

        if (otherEntity instanceof Goose){
            // todo item interacts with goose
        }else if (otherEntity instanceof Player){ 
            // player picks it up if they crash into it
            pickUp();
            
        }
    }

    public void itemAction(Player player, Array<Goose> geese, SpriteBatch spriteBatch){

    }


    public void endEffect(){

    }

    public Rectangle getItemBounds(){
        return itemBounds;
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        if (!collected && !itemUsed){
            bounceTimer += Gdx.graphics.getDeltaTime() * BOUNCE_SPEED;
            float bounceOffset = MathUtils.sin(bounceTimer) * BOUNCE_HEIGHT;
            float originalY = sprite.getY();
            sprite.setY(originalY + bounceOffset);
            super.draw(spriteBatch);
            sprite.setY(originalY);
        }
    }
}