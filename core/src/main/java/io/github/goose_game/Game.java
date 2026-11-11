package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Game {

    // map size definitions in world units
    private final float mapWidth = 24f;
    private final float mapHeight = 13.5f;

    private TiledMap tiledMap;
    private MapCollision mapCollision;
    private PathFinder pathfinder;

    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private FitViewport viewport;

    private Player player;
    private Array<Goose> geese = new Array<>();

    // setup everything when the game boots up
    public void create() {
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        viewport = new FitViewport(mapWidth, mapHeight);

        // load the tile map and setup collision and pathfinding systems
        tiledMap = new TmxMapLoader().load("CollisionMap.tmx");
        mapCollision = new MapCollision(tiledMap, mapWidth, mapHeight);
        pathfinder = new PathFinder(mapCollision, mapWidth, mapHeight);

        // spawn player
        player = new Player(mapWidth, mapHeight);

        // spawn a few initial geese around the map
        geese.add(new Goose("goose.png", 22f, 1.5f, 3));
        geese.add(new Goose("goose.png", 2f, 11.5f, 3));
        geese.add(new Goose("goose.png", 22f, 11.5f, 3));
    }

    // handle window resizing to keep the viewport scaling correctly
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    // updates player and all geese positions/states and then triggers draw
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        player.update(viewport, mapWidth, mapHeight, this, mapCollision);
        for (int i = 0; i < geese.size; i++) {
            geese.get(i).update(delta, player.getCenterPosition(), geese, mapCollision, pathfinder);
        }

        draw();
    }

    // renders background, collision walls, grid lines, player, and geese
    private void draw() {
        ScreenUtils.clear(Color.WHITE);
        viewport.apply();

        // draw collision walls using shape renderer
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);

        for (int i = 0; i < mapCollision.getCollisionBoxes().size; i++) {
            Rectangle box = mapCollision.getCollisionBoxes().get(i);
            shapeRenderer.rect(box.x, box.y, box.width, box.height);
        }
        shapeRenderer.end();

        // draw grid lines for debugging
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.LIGHT_GRAY);

        float tileW = mapWidth / 64f;
        float tileH = mapHeight / 36f;

        for (int x = 0; x <= 64; x++) {
            shapeRenderer.line(x * tileW, 0, x * tileW, mapHeight);
        }
        for (int y = 0; y <= 36; y++) {
            shapeRenderer.line(0, y * tileH, mapWidth, y * tileH);
        }
        shapeRenderer.end();

        // draw sprites using sprite batch
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        player.draw(spriteBatch);
        for (int i = 0; i < geese.size; i++) {
            geese.get(i).draw(spriteBatch);
        }

        spriteBatch.end();
    }

    public void dispose() {
        spriteBatch.dispose();
        shapeRenderer.dispose();
        if (tiledMap != null) tiledMap.dispose();
        player.dispose();
        for (int i = 0; i < geese.size; i++) {
            geese.get(i).dispose();
        }
    }

    public Array<Goose> getGeese() {
        return this.geese;
    }
}