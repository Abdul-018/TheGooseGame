package io.github.goose_game;

import com.badlogic.gdx.math.MathUtils;

public class NightEvent {

    // placeholder lengths in seconds, still need tuning (all must stay above zero)
    private static final float MIN_DAY_LENGTH = 30f;
    private static final float MAX_DAY_LENGTH = 90f;
    private static final float NIGHT_LENGTH = 20f;

    private boolean night = false;
    private float timer = MathUtils.random(MIN_DAY_LENGTH, MAX_DAY_LENGTH);

    public void update(float delta) {
        timer -= delta;
        // while, not if, so a very long frame can pass through more than one phase
        while (timer <= 0) {
            night = !night;
            timer += night ? NIGHT_LENGTH : MathUtils.random(MIN_DAY_LENGTH, MAX_DAY_LENGTH);
        }
    }

    public boolean isNight() {
        return night;
    }
}
