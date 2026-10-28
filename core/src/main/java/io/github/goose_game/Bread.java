package io.github.goose_game;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Bread extends Item {

    private static Circle effectArea;
    public Texture breadTexture;
    Sprite breadSprite;
    
    public Bread(){
        this.itemId = 1; // Do we need this?
        this.inInventory = false;
        this.effectArea = new Circle(player.x, player.y, 10);
        this.effectDuration = 10f;
    }
     


    @Override
    public void itemAction() {

        renderItem(player.x + 10, player.y); // Throws slightly to the side of the player
        
        for (Goose g : geese){
            if (effectArea.contains(g.x, g.y)){
                Goose.distract(effectDuration);
            }
        }
        endEffect();

    }

    @Override
    public void renderItem(int xCoord, int yCoord) {
        breadTexture = new Texture("bread.png");
        breadSprite = new Sprite(breadTexture);
        breadSprite.setSize(1,1);
        breadSprite.setPosition(xCoord, yCoord);
    }

    @Override
    public void endEffect() {
        breadTexture.dispose();
    }


}
