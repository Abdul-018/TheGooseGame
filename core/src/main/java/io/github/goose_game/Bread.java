package io.github.goose_game;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public class Bread extends Item {

    private Circle effectArea;
    public Texture breadTexture;
    Sprite breadSprite;

    public Bread(){
        super("bread.png");
        this.itemId = 1; // Do we need this? - weren't we gonna have individual ids for each instance but just compare types for each type of item?
        this.inInventory = false;
        this.effectDuration = 10f;
    }

    @Override
    public void itemAction(Player player, Array<Goose> geese) {

        this.effectArea = new Circle(player.getPosition().x, player.getPosition().y, 10);

        renderItem((int)(player.getPosition().x + 10), (int)(player.getPosition().y)); // Throws slightly to the side of the player

        for (Goose g : geese){
            if (effectArea.contains(g.getPosition().x, g.getPosition().y)){
                g.distract(effectDuration);
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
        if (breadTexture != null) {
            breadTexture.dispose();
        }
    }

}
