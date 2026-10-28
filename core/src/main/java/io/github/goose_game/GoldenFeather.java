package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class GoldenFeather extends Item{
    float timeRemaining = effectDuration;
    public Texture featherTexture;
    Sprite featherSprite;
    

    public GoldenFeather(){
        this.itemId = 2;
        this.inInventory = false;   
        this.effectDuration = 10f;
    }

    @Override
    public void itemAction() {

        float delta = Gdx.graphics.getDeltaTime();

        playerSprite.setSize(3,3);
        speed = 6f;

        timeRemaining -= delta;

        if (timeRemaining <= 0){
            timeRemaining = 0;
            endEffect();
        }
    }

    @Override 
    public void endEffect(){
        playerSprite.setSize(1,1);
        speed = 4f;
        featherTexture.dispose();
    }

    @Override
    public void renderItem(int xCoord, int yCoord) {
        featherTexture = new Texture("feather.png");
        featherSprite = new Sprite(featherTexture);
        featherSprite.setSize(1,1);
        featherSprite.setPosition(xCoord, yCoord);
    }


}
