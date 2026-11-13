package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class Item extends Entity { // Is there any point in extending here? - yeah, even if we arent pulling anything it fits the class structure and allows us to later on

    public static int itemId;
    public boolean inInventory;
    public boolean itemUsed = false;
    public static float effectDuration;
    public String invGraphicInternalAddress = "items/empty.png";

    // bounce animation stuff
    private float bounceTimer;
    private static final float BOUNCE_HEIGHT = 3f;
    private static final float BOUNCE_SPEED = 3f;

    public Item(String texturePath) {
        super(texturePath, 1);
        // random offset so they dont all bounce in sync
        bounceTimer = MathUtils.random(0f, MathUtils.PI2);
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        if (!inInventory && !itemUsed) {
            bounceTimer += Gdx.graphics.getDeltaTime() * BOUNCE_SPEED;
            float offset = MathUtils.sin(bounceTimer) * BOUNCE_HEIGHT;
            sprite.translateY(offset);
            sprite.draw(spriteBatch);
            sprite.translateY(-offset);
        }
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

        this.itemAction(null, null);

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
                Game.currentInventory.addItem(this);
            }catch (InvalidItemException e){
                System.out.println("Item error: " + e.getMessage());
            }
        }
    }

    public void itemAction(Player player, Array<Goose> geese){

    }

    public void renderItem(int xCoord, int yCoord){

    }

    public void endEffect(){

    }
}
