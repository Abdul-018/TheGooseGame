package io.github.goose_game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class GoldenFeather extends Item {

    private float timeRemaining = 0f;
    private boolean effectActive = false;
    private float savedSpeed;

    public GoldenFeather(float xCoord, float yCoord, Game game){
        super("items/feather.png", 0.4f, game);
        this.inInventory = false;
        this.effectDuration = 10f;

        this.collected = false;
        invGraphicInternalAddress = "items/golden_feather.png";
        sprite.setPosition(xCoord, yCoord);
        
        updateBounds();
    }

    @Override
    public void itemAction(Player player, Array<Goose> geese, SpriteBatch spriteBatch) {
        savedSpeed = player.getSpeed();
        player.setSpeed(5f); // speed boost

        // make all geese flee
        if (geese != null) {
            for (Goose g : geese) {
                g.flee();
            }
        }

        Game.activeFeather = this;
        timeRemaining = effectDuration;
        effectActive = true;
    }

    public float getTimeRemaining() {
        return timeRemaining;
    }

    public boolean isEffectActive() {
        return effectActive;
    }

    public void updateEffect(float delta, Player player, Array<Goose> geese) {
        if (!effectActive) return;

        timeRemaining -= delta;

        if (timeRemaining <= 0) {
            effectActive = false;
            Game.activeFeather = null;

            player.setSpeed(savedSpeed);

            if (geese != null) {
                for (Goose g : geese) {
                    g.stopFleeing();
                }
            }
        }
    }
}