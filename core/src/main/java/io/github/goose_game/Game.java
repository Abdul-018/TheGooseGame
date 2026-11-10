package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.*;

public class Game {

    private float mapWidth;
    private float mapHeight;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMapTileLayer collisionLayer;
    private int[] backgroundLayers;
    private int[] foregroundLayers;
    private final Rectangle spawnCheck = new Rectangle();

    private SpriteBatch spriteBatch;
    private FitViewport viewport;

    private Player player;
    private Timer timer;
    private Array<Goose> geese = new Array<>();
    private Array<Item> items = new Array<>();

    public void create() {
        spriteBatch = new SpriteBatch();

        map = new TmxMapLoader().load("map/Goose Game Map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 16f);

        mapWidth = map.getProperties().get("width", Integer.class);
        mapHeight = map.getProperties().get("height", Integer.class);

        viewport = new FitViewport(15, 10);
        viewport.getCamera().position.set(mapWidth / 2f, mapHeight / 2f, 0);

        player = new Player(mapWidth, mapHeight);
        player.setScore(0);
        // for (int i = 0; i < 3; i++) {
        // float x = (float) (Math.random() * mapWidth);
        // float y = (float) (Math.random() * mapHeight);
        // geese.add(new Goose("goose.png", x, y, 3, player));
        // }

        // for (int i = 0; i < 100; i++) {
        // float x = (float) (Math.random() * (mapWidth - 0.2f));
        // float y = (float) (Math.random() * (mapHeight - 0.2f));
        // items.add(new Bread(x, y));
        // }

        // for (int i = 0; i < 20; i++) {
        // float x = (float) (Math.random() * (mapWidth - 0.2f));
        // float y = (float) (Math.random() * (mapHeight - 0.2f));
        // items.add(new GoldenFeather(x, y));
        // }

    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public void render() {
        // float delta = Gdx.graphics.getDeltaTime();
        player.update(viewport, mapWidth, mapHeight, this);
        // for (Goose g : geese)
        // g.update(delta, player.getPosition(), geese);
        draw();

    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();

        mapRenderer.setView((OrthographicCamera) viewport.getCamera());
        mapRenderer.render();

        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        // for (Item i : items) {
        // if (!i.collected) {
        // i.draw(spriteBatch);
        // }
        // }
        player.draw(spriteBatch);
        // for (Goose g : geese) {
        // g.draw(spriteBatch);
        // }
        spriteBatch.end();
    }

    public void dispose() {
        spriteBatch.dispose();
        mapRenderer.dispose();
        player.dispose();
        // for (Goose g : geese)
        // g.dispose();
        // for (Item i : items)
        // i.dispose();
    }

    public Array<Goose> getGeese() {
        return this.geese;
    }

    public Array<Item> getItems() {
        return this.items;
    }

}
