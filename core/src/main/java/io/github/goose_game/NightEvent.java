package io.github.goose_game;

import com.badlogic.gdx.math.MathUtils;

public class NightEvent {

    private static final float MIN_DAY_LENGTH = 30f;
    private static final float MAX_DAY_LENGTH = 90f;
    private static final float NIGHT_LENGTH = 20f;

    private boolean night = false;
    private float timer = MathUtils.random(MIN_DAY_LENGTH, MAX_DAY_LENGTH);

    public void update(float delta) {
        timer -= delta;
        if (timer <= 0) {
            night = !night;
            timer = night ? NIGHT_LENGTH : MathUtils.random(MIN_DAY_LENGTH, MAX_DAY_LENGTH);
        }
    }

    public boolean isNight() {
        return night;
    }
}
