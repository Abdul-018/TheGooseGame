package io.github.goose_game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Bread extends Item {
    
    // static reference so the wave manager can easily locate the active distraction
    public static Bread activeBread = null;
    public boolean isConsumed = false;

    private boolean throwing = false;
    private float throwTimer = 0.15f;
    private float throwSpeed = 3f;
    public boolean breadVisible = false;

    public Bread(float xCoord, float yCoord, Game game){
        super("bread.png", 0.4f, game);
        this.inInventory = false;
        this.effectDuration = 10f;
        this.collected = false;

        invGraphicInternalAddress = "bread_loaf.png";
        sprite.setPosition(xCoord, yCoord);
    
        updateBounds();
    }

    @Override
    public void itemAction(Player player, Array<Goose> geese, SpriteBatch spriteBatch) {
        sprite.setPosition(player.getPosition().x, player.getPosition().y); 
        
        throwing = true;
        breadVisible = true;
        isConsumed = false;
        activeBread = this;       
        // set this bread as the active target
        effectDuration = 10f;
        throwTimer = 0.15f;
    }

    public void update(float delta){
        if (throwing){
            sprite.translateY(-throwSpeed * delta);
            throwTimer -= delta;

            if (throwTimer <= 0){
                throwing = false;
            }
        } else if (breadVisible){
            effectDuration -= delta;

            // bread disappears if time runs out or if a goose eats it
            if (effectDuration <= 0 || isConsumed){
                breadVisible = false;
                if (activeBread == this) {
                    activeBread = null;
                }
            }
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        if (throwing || breadVisible){
            sprite.draw(spriteBatch);
        } else {
            super.draw(spriteBatch);
        }
    }
}