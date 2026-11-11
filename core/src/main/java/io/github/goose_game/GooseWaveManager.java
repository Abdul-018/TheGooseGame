package io.github.goose_game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GooseWaveManager {

    private Array<Goose> activeGeese = new Array<>();
    private String texturePath;
    private float worldMapWidth;
    private float worldMapHeight;
    
    private MapCollision mapCollision;
    private PathFinder pathfinder;

    private float spawnTimer = 0f;
    private float waveTimer = 0f;
    private float gameTimer = 0f;

    private int currentWave = 1;
    private int spawnedInWave = 0;
    private int geeseDispersed = 0;
    private boolean gameOver = false;

    public GooseWaveManager(String texturePath, float worldMapWidth, float worldMapHeight, MapCollision mapCollision, PathFinder pathfinder) {
        this.texturePath = texturePath;
        this.worldMapWidth = worldMapWidth;
        this.worldMapHeight = worldMapHeight;
        this.mapCollision = mapCollision;
        this.pathfinder = pathfinder;

        // Spawn initial 3 geese on the bridge
        spawnGeese(3);
    }

    public void update(float delta, Vector2 playerPosition) {
        if (gameOver) return;

        gameTimer += delta;
        if (gameTimer >= 300f) {
            gameOver = true;
            return;
        }

        waveTimer += delta;

        // update active geese and remove dispersed ones
        for (int i = activeGeese.size - 1; i >= 0; i--) {
            Goose goose = activeGeese.get(i);
            
            // Pass the required pathfinding and collision references
            goose.update(delta, playerPosition, activeGeese, mapCollision, pathfinder);

            if (goose.getState() == Goose.GooseState.Dispersed) {
                goose.dispose();
                activeGeese.removeIndex(i);
                geeseDispersed++;
            }
        }

        int maxForWave = 2 + currentWave;

        // spawn 2 geese every 5 seconds until wave cap is reached
        if (spawnedInWave < maxForWave) {
            spawnTimer += delta;
            if (spawnTimer >= 5f) {
                spawnTimer = 0f;
                int amountToSpawn = Math.min(2, maxForWave - spawnedInWave);
                spawnGeese(amountToSpawn);
            }
        }

        // next wave if all active geese are defeated OR 30 second wave limit expires
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
            // Spawn randomly clustered on the left bridge (around X:14.5 to 15.5, Y:22.8 to 23.8)
            float spawnX = 10.5f + MathUtils.random(1.0f);
            float spawnY = 5.8f + MathUtils.random(1.0f);

            activeGeese.add(new Goose(texturePath, spawnX, spawnY, 3));
            spawnedInWave++;
        }
    }

    public void draw(SpriteBatch spriteBatch) {
        for (Goose goose : activeGeese) {
            goose.draw(spriteBatch);
        }
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
    public float getWaveTimer() { return waveTimer; }
    public float getTotalGameTimer() { return gameTimer; }
    public boolean isGameOver() { return gameOver; }
}