package io.github.goose_game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GooseSwarmEvent {

    private static final int SWARM_SIZE = 6;
    private static final float SPAWN_DISTANCE = 4f;

    private final Rectangle triggerArea;
    private boolean triggered = false;

    public GooseSwarmEvent(float x, float y, float width, float height) {
        triggerArea = new Rectangle(x, y, width, height);
    }

    // only goes off once, the first time the player walks into the area
    public void update(Vector2 playerPosition, Array<Goose> geese, float mapWidth, float mapHeight) {
        if (triggered || !triggerArea.contains(playerPosition)) return;
        triggered = true;

        // spawn them in a circle so the player gets surrounded
        for (int i = 0; i < SWARM_SIZE; i++) {
            float angle = i * 360f / SWARM_SIZE;
            float x = playerPosition.x + MathUtils.cosDeg(angle) * SPAWN_DISTANCE;
            float y = playerPosition.y + MathUtils.sinDeg(angle) * SPAWN_DISTANCE;
            geese.add(new Goose("goose.png", MathUtils.clamp(x, 0, mapWidth - 1), MathUtils.clamp(y, 0, mapHeight - 1), 3));
        }
    }

    public boolean isTriggered() {
        return triggered;
    }
}
