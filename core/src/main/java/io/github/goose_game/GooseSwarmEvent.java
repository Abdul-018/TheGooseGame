package io.github.goose_game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GooseSwarmEvent {

    // trigger zone on the bridge next to the library, placeholder values still need tuning
    private static final float ZONE_X = 15.75f;
    private static final float ZONE_Y = 11.7f;
    private static final float ZONE_WIDTH = 1.1f;
    private static final float ZONE_HEIGHT = 0.9f;

    private static final int SWARM_SIZE = 6;
    private static final float SPAWN_DISTANCE = 3.75f;
    private static final float MIN_SPAWN_DISTANCE = 1.4f;
    private static final float GOOSE_SIZE = 0.8f;

    private final Rectangle triggerArea = new Rectangle(ZONE_X, ZONE_Y, ZONE_WIDTH, ZONE_HEIGHT);
    private final MapCollision mapCollision;
    private final Player player;
    private boolean triggered = false;

    public GooseSwarmEvent(MapCollision mapCollision, Player player) {
        this.mapCollision = mapCollision;
        this.player = player;
    }

    public void update(Vector2 playerCenter, Array<Goose> geese, float mapWidth, float mapHeight) {
        if (triggered || !triggerArea.contains(playerCenter)) {
            return;
        }

        triggered = true;
        Game.lastEventID = 2;
        Game.lastEventOverlayTime = 3f;
        Rectangle bounds = new Rectangle(0, 0, GOOSE_SIZE, GOOSE_SIZE);
        for (int i = 0; i < SWARM_SIZE; i++) {
            float angle = i * 360f / SWARM_SIZE;

            for (float distance = SPAWN_DISTANCE; distance >= MIN_SPAWN_DISTANCE; distance -= 40f) {
                float x = playerCenter.x + MathUtils.cosDeg(angle) * distance - GOOSE_SIZE / 2f;
                float y = playerCenter.y + MathUtils.sinDeg(angle) * distance - GOOSE_SIZE / 2f;
                x = MathUtils.clamp(x, 0, mapWidth - GOOSE_SIZE);
                y = MathUtils.clamp(y, 0, mapHeight - GOOSE_SIZE);

                bounds.setPosition(x, y);
                if (mapCollision == null || !mapCollision.collides(bounds)) {
                    Goose goose = new Goose("goose/goose2.png", x, y, 3, player);
                    if (Game.activeFeather != null && Game.activeFeather.isEffectActive()) {
                        goose.flee();
                    }
                    geese.add(goose);
                    break;
                }
            }
        }
    }
}
