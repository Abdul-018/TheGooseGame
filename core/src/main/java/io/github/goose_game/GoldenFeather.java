package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public class GoldenFeather extends Item {
    float timeRemaining = effectDuration;
    public Texture featherTexture;
    Sprite featherSprite;

    public GoldenFeather(){
        super("feather.png");
        this.itemId = 2;
        this.inInventory = false;
        this.effectDuration = 10f;
    }

    @Override
    public void itemAction(Player player, Array<Goose> geese) {

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
        if (featherTexture != null) {
            featherTexture.dispose();
        }
    }

    @Override
    public void renderItem(int xCoord, int yCoord) {
        featherTexture = new Texture("feather.png");
        featherSprite = new Sprite(featherTexture);
        featherSprite.setSize(1,1);
        featherSprite.setPosition(xCoord, yCoord);
    }

}
