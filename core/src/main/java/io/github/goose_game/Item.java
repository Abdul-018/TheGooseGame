package io.github.goose_game;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Item extends Entity { // Is there any point in extending here? - yeah, even if we arent pulling anything it fits the class structure and allows us to later on

    public static int itemId;
    public boolean inInventory;
    public boolean itemUsed = false;
    public static float effectDuration;
    private Player player;
    private SpriteBatch spriteBatch;
    public float xCoord;
    public float yCoord;


    public Item(String texturePath, float scale) {
        super(texturePath, 1, scale);

    }

    public void pickUp() throws InvalidItemException{
        if (!this.inInventory){
            this.inInventory = true;
            //player.Inventory.append(this);

        }else{
            throw new InvalidItemException("Item already in inventory");
        }
    }

    public void useItem() throws InvalidItemException{
        if(!this.inInventory || this.itemUsed){
            throw new InvalidItemException("Item not found in inventory");
        }

        this.itemAction(null, null, spriteBatch);

        int currentScore = player.getScore();
        player.setScore(currentScore + 10);

        this.inInventory = false;
        this.itemUsed = true;
        //player.Inventory.remove(this);

    }

    public void onCollision(Entity otherEntity){ // Dictates what happens when the item is in the game world and a player/goose touches it

        if (otherEntity instanceof Goose){
            //TODO item interacts with goose
        }else if (otherEntity instanceof Player){ // If there is a collision between item and player, the player picks the item up
            try{
                pickUp();
            }catch (InvalidItemException e){
                System.out.println("Item error: " + e.getMessage());
            }
        }
    }

    public void itemAction(Player player, Array<Goose> geese, SpriteBatch spriteBatch){

    }

    public void renderItem(float xCoord, float yCoord){

    }


    public void endEffect(){

    }
}
