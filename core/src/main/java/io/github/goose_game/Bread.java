package io.github.goose_game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Bread extends Item {

    private boolean breadVisible = false;
    private float breadTimer = 0f;
    private static final float BREAD_DURATION = 10f;

    public Bread(){
        super("items/bread.png");
        this.itemId = 1;
        this.inInventory = false;
        this.effectDuration = 10f;
        invGraphicInternalAddress = "items/bread_loaf.png";
    }

    @Override
    public void itemAction(Player player, Array<Goose> geese) {
        // drop bread at the players position
        sprite.setPosition(player.getPosition().x - sprite.getWidth() / 2f,
                           player.getPosition().y - sprite.getHeight() / 2f);
        breadVisible = true;
        breadTimer = BREAD_DURATION;

        // tell the game theres active bread so geese chase it
        Game.activeBreadPosition = new Vector2(player.getPosition().x, player.getPosition().y);
        Game.activeBread = this;
    }

    public void update(float delta) {
        if (breadVisible) {
            breadTimer -= delta;
            if (breadTimer <= 0) {
                breadVisible = false;
                // clear the bread lure
                if (Game.activeBread == this) {
                    Game.activeBreadPosition = null;
                    Game.activeBread = null;
                }
            }
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        if (breadVisible) {
            // draw at the dropped position (no bounce)
            sprite.draw(spriteBatch);
        } else if (!inInventory && !itemUsed) {
            // draw with bounce when on the ground waiting to be picked up
            super.draw(spriteBatch);
        }
    }

    public boolean isBreadVisible() {
        return breadVisible;
    }

    // called when bread is consumed (all geese dispersed or expired)
    public void consumeBread() {
        breadVisible = false;
        if (Game.activeBread == this) {
            Game.activeBreadPosition = null;
            Game.activeBread = null;
        }
    }
}
