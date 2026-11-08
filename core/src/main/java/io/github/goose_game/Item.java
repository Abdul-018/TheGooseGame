package io.github.goose_game;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Item extends Entity { // Is there any point in extending here? - yeah, even if we arent pulling anything it fits the class structure and allows us to later on

    public static int itemId;
    public boolean inInventory;
    public boolean itemUsed = false;
    public static float effectDuration;
    private Player player;
    private SpriteBatch spriteBatch;
    public float xCoord;
    public float yCoord;
    private Rectangle itemBounds = new Rectangle();
    public boolean collected;


    public Item(String texturePath, float scale) {
        super(texturePath, 1, scale);
        sprite.setSize(scale,scale);
    }

    public void updateBounds(){
        itemBounds.setSize(sprite.getWidth(), sprite.getHeight());
        itemBounds.setPosition(sprite.getX(), sprite.getY());
    }

    public void pickUp(){
        this.collected = true;
        this.inInventory = true;
        
    }

    public void useItem(){
        if(this.inInventory || !this.itemUsed){
            this.itemAction(null, null, spriteBatch);

            int currentScore = player.getScore();
            player.setScore(currentScore + 10);

            this.inInventory = false;
            this.itemUsed = true;
            //player.Inventory.remove(this);
        }
    }

    public void onCollision(Entity otherEntity){ // Dictates what happens when the item is in the game world and a player/goose touches it

        if (otherEntity instanceof Goose){
            //TODO item interacts with goose
        }else if (otherEntity instanceof Player){ // If there is a collision between item and player, the player picks the item up
            pickUp();
            
        }
    }

    public void itemAction(Player player, Array<Goose> geese, SpriteBatch spriteBatch){

    }

    //public void renderItem(float xCoord, float yCoord){

    //}




    public void endEffect(){

    }

    public Rectangle getItemBounds(){
        return itemBounds;
    }
}
