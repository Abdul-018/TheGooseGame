package io.github.goose_game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GooseSwarmEvent {

    // placeholder values, still need tuning
    private static final int SWARM_SIZE = 6;
    private static final float SPAWN_DISTANCE = 4f;
    private static final float MIN_SPAWN_DISTANCE = 1.5f;

    private final Rectangle triggerArea;
    private final GooseSprites gooseSprites;
    private final MapCollision mapCollision;
    private final Player player;
    private boolean triggered = false;

    public GooseSwarmEvent(float x, float y, float width, float height, GooseSprites gooseSprites,
                           MapCollision mapCollision, Player player) {
        this.triggerArea = new Rectangle(x, y, width, height);
        this.gooseSprites = gooseSprites;
        this.mapCollision = mapCollision;
        this.player = player;
    }

    public void update(Vector2 playerCenter, Array<Goose> geese, float mapWidth, float mapHeight) {
        if (triggered || !triggerArea.contains(playerCenter)) {
            return;
        }
        triggered = true;

        Rectangle bounds = new Rectangle(0, 0, Goose.SIZE, Goose.SIZE);
        for (int i = 0; i < SWARM_SIZE; i++) {
            float angle = i * 360f / SWARM_SIZE;

            // pull the spawn point in towards the player until it is clear of walls
            for (float distance = SPAWN_DISTANCE; distance >= MIN_SPAWN_DISTANCE; distance -= 1f) {
                float x = playerCenter.x + MathUtils.cosDeg(angle) * distance - Goose.SIZE / 2f;
                float y = playerCenter.y + MathUtils.sinDeg(angle) * distance - Goose.SIZE / 2f;
                x = MathUtils.clamp(x, 0, mapWidth - Goose.SIZE);
                y = MathUtils.clamp(y, 0, mapHeight - Goose.SIZE);

                bounds.setPosition(x, y);
                if (mapCollision == null || !mapCollision.collides(bounds)) {
                    geese.add(new Goose(gooseSprites, x, y, 3, player));
                    break;
                }
            }
        }
    }
}
