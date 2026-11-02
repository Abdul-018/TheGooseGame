package io.github.goose_game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GooseSwarmEvent {

    // placeholder values, still need tuning
    private static final int SWARM_SIZE = 6;
    private static final float SPAWN_DISTANCE = 4f;

    private final Rectangle triggerArea;
    private final GooseSprites gooseSprites;
    private boolean triggered = false;

    public GooseSwarmEvent(float x, float y, float width, float height, GooseSprites gooseSprites) {
        this.triggerArea = new Rectangle(x, y, width, height);
        this.gooseSprites = gooseSprites;
    }

    public void update(Vector2 playerCenter, Array<Goose> geese, float mapWidth, float mapHeight) {
        if (triggered || !triggerArea.contains(playerCenter)) {
            return;
        }
        triggered = true;

        // TODO: check spawn spots against walls once the map and collisions are in
        for (int i = 0; i < SWARM_SIZE; i++) {
            float angle = i * 360f / SWARM_SIZE;
            float x = playerCenter.x + MathUtils.cosDeg(angle) * SPAWN_DISTANCE - Goose.SIZE / 2f;
            float y = playerCenter.y + MathUtils.sinDeg(angle) * SPAWN_DISTANCE - Goose.SIZE / 2f;
            x = MathUtils.clamp(x, 0, mapWidth - Goose.SIZE);
            y = MathUtils.clamp(y, 0, mapHeight - Goose.SIZE);
            geese.add(new Goose(gooseSprites, x, y, 3));
        }
    }
}
