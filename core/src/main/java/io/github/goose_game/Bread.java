package io.github.goose_game;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Gdx;

public class Bread extends Item {

    private Circle effectArea;
    public Texture breadTexture;
    //private Sprite breadSprite;
    private boolean throwing = false;
    private float throwTimer = 0.15f;
    private float throwSpeed = 3f;
    private boolean breadVisible = false;

    public Bread(float xCoord, float yCoord, Game game){
        super("bread.png", 0.4f, game);
        this.inInventory = false;
        this.effectDuration = 10f;
        this.collected = false;

        //breadSprite = new Sprite(new Texture("bread.png"));
        //breadSprite.setPosition(xCoord, yCoord);
    
        sprite.setPosition(xCoord, yCoord);
    
        updateBounds();

    }

    @Override
    public void itemAction(Player player, Array<Goose> geese, SpriteBatch spriteBatch) {

        this.effectArea = new Circle(player.getPosition().x, player.getPosition().y, 10);

        sprite.setPosition(player.getPosition().x+1, player.getPosition().y);  // Throws slightly to the side of the player
        //draw(spriteBatch);

        throwing = true;
        breadVisible = true;


        for (Goose g : geese){
            if (effectArea.contains(g.getPosition().x, g.getPosition().y)){
                g.distract(effectDuration);
            }
        }

        effectDuration -= Gdx.graphics.getDeltaTime();
        if(effectDuration <= 0){
            endEffect();
        }

    }

    public void update(float delta){
        if (throwing){
            sprite.translateX(throwSpeed*delta);

            throwTimer -= delta;

            if (throwTimer <= 0){
                throwing = false;
                effectDuration = 10f;
            }
        }else if (breadVisible){
            effectDuration -= delta;

            if (effectDuration <= 0){
                breadVisible = false;
            }
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        if (throwing || breadVisible){
            sprite.draw(spriteBatch);
        }else{
            super.draw(spriteBatch);
        }
    }

}
