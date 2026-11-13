package io.github.goose_game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GooseWaveManager {

    private Array<Goose> activeGeese = new Array<>();
    private GooseSprites gooseSprites;
    private float worldMapWidth;
    private float worldMapHeight;
    
    private MapCollision mapCollision;
    private PathFinder pathfinder;
    private Player player; 

    private float spawnTimer = 0f;
    private float waveTimer = 0f;
    private float gameTimer = 0f;

    private int currentWave = 1;
    private int spawnedInWave = 0;
    private int geeseDispersed = 0;
    private boolean gameOver = false;

    public GooseWaveManager(GooseSprites gooseSprites, float worldMapWidth, float worldMapHeight, MapCollision mapCollision, PathFinder pathfinder, Player player) {
        this.gooseSprites = gooseSprites;
        this.worldMapWidth = worldMapWidth;
        this.worldMapHeight = worldMapHeight;
        this.mapCollision = mapCollision;
        this.pathfinder = pathfinder;
        this.player = player;

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

        // target override point to bread if it exists and hasnt been eaten otherwise point to player
        Vector2 currentTarget = playerPosition;
        if (Bread.activeBread != null && !Bread.activeBread.isConsumed && Bread.activeBread.breadVisible) {
            currentTarget = Bread.activeBread.getPosition();
        }

        for (int i = activeGeese.size - 1; i >= 0; i--) {
            Goose goose = activeGeese.get(i);
            
            // geese will naturally pathfind to whichever target was assigned above
            goose.update(delta, currentTarget, activeGeese, mapCollision, pathfinder);

            // bread collision if this goose reaches the bread it eats it and disperses
            if (Bread.activeBread != null && !Bread.activeBread.isConsumed && Bread.activeBread.breadVisible) {
                if (goose.getCenterPosition().dst(Bread.activeBread.getPosition()) < 0.5f) {
                    goose.disperse();
                    Bread.activeBread.isConsumed = true; // flags bread so it disappears and other geese drop aggro
                }
            }

            if (goose.getState() == Goose.GooseState.Dispersed) {
                goose.dispose();
                activeGeese.removeIndex(i);
                geeseDispersed++;
            }
        }

        int maxForWave = 2 + currentWave;

        if (spawnedInWave < maxForWave) {
            spawnTimer += delta;
            if (spawnTimer >= 5f) {
                spawnTimer = 0f;
                int amountToSpawn = Math.min(2, maxForWave - spawnedInWave);
                spawnGeese(amountToSpawn);
            }
        }

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
            float spawnX = 10.5f + MathUtils.random(1.0f);
            float spawnY = 5.8f + MathUtils.random(1.0f);

            activeGeese.add(new Goose(gooseSprites, spawnX, spawnY, 3, player));
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