package io.github.goose_game;

public class Bread extends Item {
    
    public Bread(){
        this.ItemId = 1;
        this.inInventory = false;
    }
    
    @Override
    public void itemAction() {
        //TODO make the bread attract the nearby geese
    }


}
