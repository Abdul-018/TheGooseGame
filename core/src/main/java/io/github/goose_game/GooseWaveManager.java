package io.github.goose_game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GooseWaveManager {

    private Array<Goose> activeGeese = new Array<>();
    private Array<Rectangle> collisionRects;
    private PathFinder pathFinder;

    private float spawnTimer = 0f;
    private float waveTimer = 0f;

    private int currentWave = 1;
    private int spawnedInWave = 0;
    private int geeseDispersed = 0;
    private boolean won = false;

    // how long the whole game lasts (5 minutes)
    private static final float GAME_DURATION = 300f;

    public GooseWaveManager(Array<Rectangle> collisionRects, PathFinder pathFinder) {
        this.collisionRects = collisionRects;
        this.pathFinder = pathFinder;

        // start with 3 geese
        spawnGeese(3);
    }

    public void update(float delta, Vector2 playerPosition) {
        if (won) return;

        // survived 5 minutes, you win
        if (Game.gameTime >= GAME_DURATION) {
            won = true;
            return;
        }

        waveTimer += delta;

        // geese chase bread if one is active, otherwise chase player
        Vector2 chaseTarget = (Game.activeBreadPosition != null) ? Game.activeBreadPosition : playerPosition;

        // update geese and remove dispersed ones
        for (int i = activeGeese.size - 1; i >= 0; i--) {
            Goose goose = activeGeese.get(i);
            goose.update(delta, chaseTarget, activeGeese, collisionRects, pathFinder);

            if (goose.getState() == Goose.GooseState.Dispersed) {
                goose.dispose();
                activeGeese.removeIndex(i);
                geeseDispersed++;
            }
        }

        int maxForWave = 2 + currentWave;

        // spawn 2 geese every 5 seconds until wave cap
        if (spawnedInWave < maxForWave) {
            spawnTimer += delta;
            if (spawnTimer >= 5f) {
                spawnTimer = 0f;
                int toSpawn = Math.min(2, maxForWave - spawnedInWave);
                spawnGeese(toSpawn);
            }
        }

        // move to next wave if all geese cleared or 30s passed
        boolean waveCleared = (spawnedInWave >= maxForWave) && activeGeese.isEmpty();
        boolean waveTimedOut = waveTimer >= 30f;

        if (waveCleared || waveTimedOut) {
            nextWave();
        }
    }

    private void nextWave() {
        currentWave++;
        spawnedInWave = 0;
        spawnTimer = 0f;
        waveTimer = 0f;
        spawnGeese(2);
    }

    private void spawnGeese(int count) {
        for (int i = 0; i < count; i++) {
            // fixed spawn area so geese dont end up outside the fence
            float x = 700f + MathUtils.random(200f);
            float y = 900f + MathUtils.random(200f);
            float[] pos = Game.instance.findClearSpot(x, y);
            Goose g = new Goose("goose/goose.png", pos[0], pos[1], 3);
            // if the feather is active, new geese should flee too
            if (Game.activeFeather != null && Game.activeFeather.isActive()) {
                g.flee();
            }
            activeGeese.add(g);
            spawnedInWave++;
        }
    }

    public void draw(SpriteBatch spriteBatch) {
        for (Goose goose : activeGeese) {
            goose.draw(spriteBatch);
        }
    }

    public void reset() {
        for (Goose g : activeGeese) g.dispose();
        activeGeese.clear();
        currentWave = 1;
        spawnedInWave = 0;
        geeseDispersed = 0;
        spawnTimer = 0f;
        waveTimer = 0f;
        won = false;
        spawnGeese(3);
    }

    public void dispose() {
        for (Goose goose : activeGeese) {
            goose.dispose();
        }
        activeGeese.clear();
    }

    public Array<Goose> getActiveGeese() { return activeGeese; }
    public int getCurrentWave() { return currentWave; }
    public int getTotalGeeseDispersed() { return geeseDispersed; }
    public boolean hasWon() { return won; }
}
