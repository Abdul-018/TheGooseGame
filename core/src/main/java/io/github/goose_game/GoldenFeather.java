package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GoldenFeather extends Item {
    float timeRemaining = effectDuration;
    public Texture featherTexture;
    Sprite featherSprite;

    public GoldenFeather(float xCoord, float yCoord){
        super("feather.png", 0.4f);
        this.itemId = 2;
        this.inInventory = false;
        this.effectDuration = 10f;

        this.collected = false;

        sprite.setPosition(xCoord, yCoord);
        //sprite.setScale(scale);
        updateBounds();
    }

    @Override
    public void itemAction(Player player, Array<Goose> geese, SpriteBatch spriteBatch) {

        float delta = Gdx.graphics.getDeltaTime();

        player.getSprite().setSize(3,3);
        player.setSpeed(6f);

        timeRemaining -= delta;

        if (timeRemaining <= 0){
            timeRemaining = 0;
            endEffect(player);
        }
    }

    public void endEffect(Player player){
        player.getSprite().setSize(1,1);
        player.setSpeed(4f);
        
    }



}
