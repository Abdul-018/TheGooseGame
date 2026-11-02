package io.github.goose_game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public class GooseWaveManager {

    private List<Goose> activeGeese = new ArrayList<>();
    private String texturePath;
    private int screenWidth;
    private int screenHeight;

    private float spawnTimer = 0f;
    private float waveTimer = 0f;
    private float gameTimer = 0f;

    private int currentWave = 1;
    private int spawnedInWave = 0;
    private int geeseDispersed = 0;
    private boolean gameOver = false;

    public GooseWaveManager(String texturePath, int screenWidth, int screenHeight) {
        this.texturePath = texturePath;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        spawnGeese(2);
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
        for (int i = activeGeese.size() - 1; i >= 0; i--) {
            Goose goose = activeGeese.get(i);
            goose.update(delta, playerPosition);

            if (goose.getState() == Goose.GooseState.Dispersed) {
                goose.dispose();
                activeGeese.remove(i);
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
        // increment wave and reset wave counters WITHOUT wiping activeGeese
        currentWave++;
        spawnedInWave = 0;
        spawnTimer = 0f;
        waveTimer = 0f;

        // start the new wave by spawning its initial 2 geese alongside surviving geese
        spawnGeese(2);
    }

    private void spawnGeese(int count) {
        for (int i = 0; i < count; i++) {
            float spawnX = 0;
            float spawnY = 0;

            switch (MathUtils.random(3)) {
                case 0:
                    spawnX = -32;
                    spawnY = MathUtils.random(0, screenHeight);
                    break;
                case 1:
                    spawnX = screenWidth + 32;
                    spawnY = MathUtils.random(0, screenHeight);
                    break;
                case 2:
                    spawnX = MathUtils.random(0, screenWidth);
                    spawnY = -32;
                    break;
                case 3:
                    spawnX = MathUtils.random(0, screenWidth);
                    spawnY = screenHeight + 32;
                    break;
            }

            activeGeese.add(new Goose(texturePath, spawnX, spawnY, 1));
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

    public List<Goose> getActiveGeese() 
    {
        return activeGeese; 
    }
    public int getCurrentWave() 
    {
        return currentWave; 
    }
    public int getTotalGeeseDispersed() 
    { 
        return geeseDispersed; 
    }
    public float getWaveTimer() 
    { 
        return waveTimer; 
    }
    public float getTotalGameTimer() 
    { 
        return gameTimer; 
    }
    public boolean isGameOver() 
    { 
        return gameOver; 
    }
}