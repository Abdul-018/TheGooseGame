package io.github.goose_game;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Bread extends Item {

    private Circle effectArea;
    public Texture breadTexture;
    private Sprite breadSprite;

    public Bread(float xCoord, float yCoord){
        super("bread.png", 0.4f);
        this.itemId = 1; // Do we need this? - weren't we gonna have individual ids for each instance but just compare types for each type of item?
        this.inInventory = false;
        this.effectDuration = 10f;
        this.collected = false;
    
        sprite.setPosition(xCoord, yCoord);
        //sprite.setScale(scale);
        updateBounds();

    }

    @Override
    public void itemAction(Player player, Array<Goose> geese, SpriteBatch spriteBatch) {

        this.effectArea = new Circle(player.getPosition().x, player.getPosition().y, 10);

        breadSprite.setPosition(player.getPosition().x+10, player.getPosition().y);  // Throws slightly to the side of the player
        draw(spriteBatch);

        for (Goose g : geese){
            if (effectArea.contains(g.getPosition().x, g.getPosition().y)){
                g.distract(effectDuration);
            }
        }
        endEffect();

    }



}
