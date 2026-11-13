package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import com.badlogic.gdx.math.Vector2;

import io.github.goose_game.Menus.LoseScreenMenu;
import io.github.goose_game.Menus.MainMenu;
import io.github.goose_game.Menus.PauseMenu;
import io.github.goose_game.Menus.UIMenu;
import io.github.goose_game.Menus.WinScreenMenu;
import io.github.goose_game.Uimenu.Menu;

public class Game {

    private final float mapWidth = 1600f;
    private final float mapHeight = 1600f;

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;

    private SpriteBatch spriteBatch;
    private ExtendViewport viewport;
    public static ExtendViewport menuViewport;

    private Player player;
    public static Player currentPlayer;
    private Timer timer;
    private GooseWaveManager waveManager;
    private Array<Item> items = new Array<>();
    private Array<Rectangle> collisionRects;
    private PathFinder pathFinder;
    private static final float PLAYER_SPAWN_X = 400f;
    private static final float PLAYER_SPAWN_Y = 864f;
    public boolean isPaused = true;
    public static boolean gameOver = false;
    public static float gameTime = 0f;
    public static Game instance;
    public static Inventory currentInventory;
    public static Vector2 activeBreadPosition = null;
    public static Bread activeBread = null;
    public static GoldenFeather activeFeather = null;

    public void create() {
        instance = this;
        tiledMap = new TmxMapLoader().load("map/Goose Game Map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        spriteBatch = new SpriteBatch();
        viewport = new ExtendViewport(280, 280);
        menuViewport = new ExtendViewport(16, 9);

        collisionRects = CollisionLoader.loadCollisionRectangles(tiledMap);
        pathFinder = new PathFinder(collisionRects, mapWidth, mapHeight, 16f);

        player = new Player(mapWidth, mapHeight);
        player.setScore(0);
        player.setHealth(10);
        currentPlayer = player;
        currentInventory = new Inventory();
        float[] playerSpawn = findClearSpot(PLAYER_SPAWN_X, PLAYER_SPAWN_Y);
        player.getSprite().setPosition(playerSpawn[0], playerSpawn[1]);

        waveManager = new GooseWaveManager(collisionRects, pathFinder);

        // spawn items inside the fenced area
        for (int i = 0; i < 10; i++) {
            float x = 400f + (float)(Math.random() * 800);
            float y = 500f + (float)(Math.random() * 600);
            float[] pos = findClearSpot(x, y);
            Bread b = new Bread();
            b.getSprite().setSize(16f, 16f);
            b.getSprite().setPosition(pos[0], pos[1]);
            items.add(b);
        }
        for (int i = 0; i < 5; i++) {
            float x = 400f + (float)(Math.random() * 800);
            float y = 500f + (float)(Math.random() * 600);
            float[] pos = findClearSpot(x, y);
            GoldenFeather f = new GoldenFeather();
            f.getSprite().setSize(16f, 16f);
            f.getSprite().setPosition(pos[0], pos[1]);
            items.add(f);
        }

        setInputProcessor();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, false);
        menuViewport.update(width, height, true);
        Menu.Updateviewport(menuViewport);
    }

    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        // only update game stuff when the game ui is on top (not paused/in menu)
        isPaused = (false == UIMenu.gameUI.IsAtTop());
        if (!isPaused) {
            gameTime += delta;
            player.update(viewport, mapWidth, mapHeight, this, collisionRects);
            waveManager.update(delta, player.getPosition());

            // check if player walks over an item
            Rectangle playerRect = new Rectangle(player.getSprite().getX(), player.getSprite().getY(),
                player.getSprite().getWidth(), player.getSprite().getHeight());
            for (int i = items.size - 1; i >= 0; i--) {
                Item item = items.get(i);
                Rectangle itemRect = new Rectangle(item.getSprite().getX(), item.getSprite().getY(),
                    item.getSprite().getWidth(), item.getSprite().getHeight());
                if (playerRect.overlaps(itemRect) && !item.inInventory && !item.itemUsed) {
                    item.onCollision(player);
                }
            }

            // update active feather effect
            if (activeFeather != null) {
                activeFeather.update(delta);
            }

            // update active bread and check goose-bread collision
            if (activeBread != null) {
                activeBread.update(delta);
                if (activeBreadPosition != null) {
                    for (int i = waveManager.getActiveGeese().size - 1; i >= 0; i--) {
                        Goose g = waveManager.getActiveGeese().get(i);
                        if (g.getState() == Goose.GooseState.Dispersed) continue;
                        float dist = g.getPosition().dst(activeBreadPosition);
                        if (dist < 16f) {
                            g.disperse();
                            player.setScore(player.getScore() + 20);
                        }
                    }
                }
            }

            // use selected item with SPACE
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                Item selected = currentInventory.getSelected();
                if (selected != null) {
                    selected.itemAction(player, waveManager.getActiveGeese());
                    currentInventory.removeItem(selected);
                    selected.inInventory = false;
                    selected.itemUsed = true;
                }
            }

            // cycle inventory with Q
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                currentInventory.selectNextItem();
            }

            // player died
            if (player.getHealth() <= 0 && !gameOver) {
                gameOver = true;
                LoseScreenMenu.loseMenu.Open();
            }

            // survived all 5 minutes
            if (waveManager.hasWon() && !gameOver) {
                gameOver = true;
                WinScreenMenu.winMenu.Open();
            }
        }
        Menu.Updateall();
        draw();
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();

        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        mapRenderer.setView(camera);
        mapRenderer.render();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        // draw items that havent been picked up yet
        for (Item item : items) {
            if (!item.inInventory && !item.itemUsed) {
                item.draw(spriteBatch);
            }
        }
        // draw active bread on the ground
        if (activeBread != null && activeBread.isBreadVisible()) {
            activeBread.draw(spriteBatch);
        }
        player.draw(spriteBatch);
        waveManager.draw(spriteBatch);
        spriteBatch.end();

        // draw menus on top using the menu viewport
        menuViewport.apply();
        spriteBatch.setProjectionMatrix(menuViewport.getCamera().combined);
        spriteBatch.begin();
        Menu.Drawall(spriteBatch);
        spriteBatch.end();
    }

    public void dispose() {
        spriteBatch.dispose();
        mapRenderer.dispose();
        tiledMap.dispose();
        player.dispose();
        waveManager.dispose();
        for (Item item : items) item.dispose();
    }

    private void setInputProcessor() {
        Gdx.input.setInputProcessor(new InputAdapter () {
            @Override
            public boolean touchUp (int x, int y, int pointer, int button) {
                return Menu.Inputall(x, y, menuViewport);
            }
            @Override
            public boolean keyUp (int keycode) {
                if (keycode == Input.Keys.ESCAPE){
                    if (UIMenu.gameUI.IsAtTop()){
                        PauseMenu.pauseMenu.Open();
                    }else if (MainMenu.mainMenu.IsAtTop()){
                        Gdx.app.exit();
                    }else if (PauseMenu.pauseMenu.IsAtTop()){
                        PauseMenu.pauseMenu.Close();
                    }else{
                        Menu.Inputall(keycode);
                    }
                }else{
                    Menu.Inputall(keycode);
                }
                return false;
            }
        });
    }

    // puts everyone back to where they started
    public void resetGame() {
        // reset player
        player.setHealth(10);
        player.setScore(0);
        float[] pSpawn = findClearSpot(PLAYER_SPAWN_X, PLAYER_SPAWN_Y);
        player.getSprite().setPosition(pSpawn[0], pSpawn[1]);

        // reset wave manager, clears all geese and starts fresh
        waveManager.reset();

        // put all items back on the ground
        for (Item item : items) {
            item.inInventory = false;
            item.itemUsed = false;
        }

        gameTime = 0f;
        gameOver = false;
        activeBreadPosition = null;
        activeBread = null;
        activeFeather = null;
        currentInventory = new Inventory();
    }

    public float[] findClearSpot(float cx, float cy) {
        // 48x48 test area so theres room to actually move around
        // also checks water/buildings layers so nothing spawns in the lake
        TiledMapTileLayer water = (TiledMapTileLayer) tiledMap.getLayers().get("Water");
        TiledMapTileLayer buildings = (TiledMapTileLayer) tiledMap.getLayers().get("Buildings");
        Rectangle test = new Rectangle(0, 0, 48f, 48f);
        for (int radius = 0; radius < 15; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    float x = cx + dx * 16f;
                    float y = cy + dy * 16f;
                    test.setPosition(x - 16f, y - 16f);
                    boolean blocked = false;
                    for (Rectangle r : collisionRects) {
                        if (test.overlaps(r)) { blocked = true; break; }
                    }
                    if (blocked) continue;
                    // check the tile under the center isnt water or a building
                    int tileX = (int)(x / 16f);
                    int tileY = (int)(y / 16f);
                    if (tileX < 0 || tileX >= 100 || tileY < 0 || tileY >= 100) continue;
                    if (water != null && water.getCell(tileX, tileY) != null) continue;
                    if (buildings != null && buildings.getCell(tileX, tileY) != null) continue;
                    return new float[]{x, y};
                }
            }
        }
        return new float[]{cx, cy};
    }

    public Array<Goose> getGeese(){
        return waveManager.getActiveGeese();
    }

    public Array<Item> getItems(){
        return this.items;
    }

    public GooseWaveManager getWaveManager(){
        return waveManager;
    }

}
