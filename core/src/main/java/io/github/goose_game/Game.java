package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.goose_game.Menus.MainMenu;
import io.github.goose_game.Menus.PauseMenu;
import io.github.goose_game.Menus.UIMenu;
import io.github.goose_game.Uimenu.Menu;

public class Game {

    // map and viewport dimensions unified from both versions
    private final float VIEWPORT_WIDTH = 24f;
    private final float VIEWPORT_HEIGHT = 13.5f;
    private final float mapUnitScale = VIEWPORT_WIDTH / (64f * 16f);
    
    private final float worldMapWidth = 100f * 16f * mapUnitScale;
    private final float worldMapHeight = 100f * 16f * mapUnitScale;

    // tiled map components from code 2
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private MapCollision mapCollision;
    private PathFinder pathfinder;
    private GooseWaveManager waveManager;

    // rendering and viewports code 1 viewports plus code 2 map rendering
    private SpriteBatch spriteBatch;
    private ExtendViewport viewport;
    public static ExtendViewport menuViewport;

    // game entities and status code 1 ui menus items plus code 2 systems
    public static Player currentPlayer;
    private Player player;
    private Timer timer;
    public static Inventory currentInventory;
    private Inventory inventory;
    private Array<Item> items = new Array<>();
    public boolean isPaused = true;
    public static boolean gameOver = false;

    public void create() {
        spriteBatch = new SpriteBatch();
        
        // setup viewports code 1 style extendviewports for flexible ui and game scaling
        viewport = new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        menuViewport = new ExtendViewport(16, 9);

        // load tiled map and systems from code 2
        tiledMap = new TmxMapLoader().load("map/Goose Game Map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, mapUnitScale, spriteBatch);

        mapCollision = new MapCollision(tiledMap, worldMapWidth, worldMapHeight);
        pathfinder = new PathFinder(mapCollision, worldMapWidth, worldMapHeight);

        // initialize player
        player = new Player(worldMapWidth, worldMapHeight);
        player.getSprite().setPosition(10f, 9f);
        player.setScore(0);
        player.setHealth(10);
        currentPlayer = player;

        // initialize goose wave manager from code 2 instead of raw array
        waveManager = new GooseWaveManager("goose2.png", worldMapWidth, worldMapHeight, mapCollision, pathfinder, player);
        // spawn items from code 1
        for (int i = 0; i < 100; i++) {
            float x = (float)(Math.random() * (worldMapWidth - 0.2f));
            float y = (float)(Math.random() * (worldMapHeight - 0.2f));
            items.add(new Bread(x, y, this));
        }

        for (int i = 0; i < 20; i++) {
            float x = (float)(Math.random() * (worldMapWidth - 0.2f));
            float y = (float)(Math.random() * (worldMapHeight - 0.2f));
            items.add(new GoldenFeather(x, y,this));
        }
        inventory = new Inventory();
        currentInventory = inventory;
        setInputProcessor();
        
    }

    public void resize(int width, int height) {
        viewport.update(width, height, false);
        menuViewport.update(width, height, true);
        Menu.Updateviewport(menuViewport);
    }

    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        
        // pause handling from code 1
        isPaused = (false == UIMenu.gameUI.IsAtTop());
        if (!isPaused) {
            // player update with collision support from code 2
            player.update(viewport, worldMapWidth, worldMapHeight, this, mapCollision);
            
            // wave manager updates geese using pathfinding and collision
            waveManager.update(delta, player.getCenterPosition());

            for (Item i : items){
                if (i instanceof Bread){
                    ((Bread) i).update(delta);
                }
            }
        }

        // instance health plus player gethealth
        // system out println static health plus currentplayer gethealth

        Menu.Updateall();
        draw();
    }

    private void setInputProcessor() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchUp(int x, int y, int pointer, int button) {
                return Menu.Inputall(x, y, menuViewport);
            }

            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    System.out.println("escape pressed");
                    if (UIMenu.gameUI.IsAtTop()) {
                        PauseMenu.pauseMenu.Open();
                    } else if (MainMenu.mainMenu.IsAtTop()) {
                        Gdx.app.exit();
                    } else if (PauseMenu.pauseMenu.IsAtTop()) {
                        PauseMenu.pauseMenu.Close();
                    } else {
                        Menu.Inputall(keycode);
                    }
                } else {
                    Menu.Inputall(keycode);
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.E){
                    Item selected = inventory.getSelected();

                    if (selected != null){
                        selected.useItem(player, getGeese(), spriteBatch);
                        System.out.println("USED");
                    }
                }
                // cycle inventory with q and r
                else if (keycode == Input.Keys.Q) {
                    inventory.selectPrevItem();
                    System.out.println("Selected Prev Item: " + inventory.GetCurrItemIndex());
                } 
                else if (keycode == Input.Keys.R) {
                    inventory.selectNextItem();
                    System.out.println("Selected Next Item: " + inventory.GetCurrItemIndex());
                }
                return false;
            }

            // cycle inventory with mouse scroll wheel
            @Override
            public boolean scrolled(float amountX, float amountY) {
                // amounty is positive for scrolling down negative for scrolling up
                if (amountY > 0) {
                    inventory.selectNextItem();
                } else if (amountY < 0) {
                    inventory.selectPrevItem();
                }
                return false;
            }
        });
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        
        // apply game world viewport and render tiled map code 2
        viewport.apply();
        OrthographicCamera cam = (OrthographicCamera) viewport.getCamera();
        mapRenderer.setView(cam);
        mapRenderer.render();

        spriteBatch.setProjectionMatrix(cam.combined);
        spriteBatch.begin();

        // draw items code 1
        for (Item i : items) {
            
            i.draw(spriteBatch);
            
        }

        // draw player and geese via wavemanager
        player.draw(spriteBatch);
        waveManager.draw(spriteBatch);

        spriteBatch.end();

        // render ui menus overlay code 1
        menuViewport.apply();
        spriteBatch.setProjectionMatrix(menuViewport.getCamera().combined);
        spriteBatch.begin();
        Menu.Drawall(spriteBatch);
        spriteBatch.end();
    }

    public void dispose() {
        spriteBatch.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        if (tiledMap != null) tiledMap.dispose();
        player.dispose();
        if (waveManager != null) waveManager.dispose();
        for (Item i : items) i.dispose();
    }

    public Array<Goose> getGeese() {
        return waveManager.getActiveGeese();
    }

    public Inventory getInventory(){
        return this.inventory;
    }

    public Array<Item> getItems() {
        return this.items;
    }
}