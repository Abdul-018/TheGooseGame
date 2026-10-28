package io.github.goose_game;

public class Item extends Entity{ // Is there any point in extending here?
    
    public static int itemId;
    public boolean inInventory;
    public boolean itemUsed = false;
    public static float effectDuration;


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

        this.itemAction();

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

    public void itemAction(){

    }

    public void renderItem(int xCoord, int yCoord){

    }

    public void endEffect(){
        
    }
}
