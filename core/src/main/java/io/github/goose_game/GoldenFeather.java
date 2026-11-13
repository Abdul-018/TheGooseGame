package io.github.goose_game;

import com.badlogic.gdx.utils.Array;

public class GoldenFeather extends Item {

    private boolean active = false;
    private float timeRemaining = 0f;
    private static final float BOOST_DURATION = 10f;
    private static final float BOOSTED_SPEED = 200f;
    private static final float NORMAL_SPEED = 130f;

    public GoldenFeather(){
        super("items/feather.png");
        this.itemId = 2;
        this.inInventory = false;
        this.effectDuration = 10f;
        invGraphicInternalAddress = "items/golden_feather.png";
    }

    @Override
    public void itemAction(Player player, Array<Goose> geese) {
        player.setSpeed(BOOSTED_SPEED);
        active = true;
        timeRemaining = BOOST_DURATION;
        Game.activeFeather = this;

        // all geese flee
        for (Goose g : geese) {
            if (g.getState() != Goose.GooseState.Dispersed) {
                g.flee();
            }
        }
    }

    public void update(float delta) {
        if (active) {
            timeRemaining -= delta;
            if (timeRemaining <= 0) {
                active = false;
                Game.currentPlayer.setSpeed(NORMAL_SPEED);

                // geese go back to chasing
                for (Goose g : Game.instance.getGeese()) {
                    g.stopFleeing();
                }

                if (Game.activeFeather == this) {
                    Game.activeFeather = null;
                }
            }
        }
    }

    public boolean isActive() {
        return active;
    }
}
