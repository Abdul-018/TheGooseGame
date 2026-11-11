package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Game {

    private final float VIEWPORT_WIDTH = 24f;
    private final float VIEWPORT_HEIGHT = 13.5f;
    private final float mapUnitScale = VIEWPORT_WIDTH / (64f * 16f);
    
    private final float worldMapWidth = 100f * 16f * mapUnitScale;
    private final float worldMapHeight = 100f * 16f * mapUnitScale;

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private MapCollision mapCollision;
    private PathFinder pathfinder;

    private SpriteBatch spriteBatch;
    private FitViewport viewport;

    private Player player;
    private GooseWaveManager waveManager;

    public void create() {
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);

        tiledMap = new TmxMapLoader().load("map/Goose Game Map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, mapUnitScale, spriteBatch);

        mapCollision = new MapCollision(tiledMap, worldMapWidth, worldMapHeight);
        pathfinder = new PathFinder(mapCollision, worldMapWidth, worldMapHeight);

        player = new Player(worldMapWidth, worldMapHeight);
        player.getSprite().setPosition(10f, 9f); 

        // Initialize the wave manager instead of the raw array
        waveManager = new GooseWaveManager("goose.png", worldMapWidth, worldMapHeight, mapCollision, pathfinder);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        player.update(viewport, worldMapWidth, worldMapHeight, this, mapCollision);
        
        // Let the wave manager handle updating all geese and checking wave timers
        waveManager.update(delta, player.getCenterPosition());

        draw();
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();

        OrthographicCamera cam = (OrthographicCamera) viewport.getCamera();
        mapRenderer.setView(cam);
        mapRenderer.render();

        spriteBatch.setProjectionMatrix(cam.combined);
        spriteBatch.begin();

        player.draw(spriteBatch);
        
        // Let the wave manager handle drawing the geese
        waveManager.draw(spriteBatch);

        spriteBatch.end();
    }

    public void dispose() {
        spriteBatch.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        if (tiledMap != null) tiledMap.dispose();
        player.dispose();
        if (waveManager != null) waveManager.dispose();
    }

    public Array<Goose> getGeese() {
        return waveManager.getActiveGeese();
    }
}