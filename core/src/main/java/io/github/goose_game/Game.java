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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.goose_game.Menus.MainMenu;
import io.github.goose_game.Menus.PauseMenu;
import io.github.goose_game.Menus.UIMenu;
import io.github.goose_game.Uimenu.Menu;

public class Game {

    public static Game instance;

    // Item spawn caps
    private static final int MAX_BREAD = 50;
    private static final int MAX_GOLDEN_FEATHERS = 2;

    private final float VIEWPORT_WIDTH = 14f;
    private final float VIEWPORT_HEIGHT = 7.875f;

    // keep mapUnitScale fixed so the world stays 37.5f x 37.5f regardless of zoom
    private final float mapUnitScale = 24f / (64f * 16f);
    
    private final float worldMapWidth = 100f * 16f * mapUnitScale;
    private final float worldMapHeight = 100f * 16f * mapUnitScale;

    // Tiled map components
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private MapCollision mapCollision;
    private PathFinder pathfinder;
    private GooseWaveManager waveManager;
    private Rectangle fenceInterior;

    // Rendering and viewports
    private SpriteBatch spriteBatch;
    private ExtendViewport viewport;
    public static ExtendViewport menuViewport;

    // Game entities and status
    public static Player currentPlayer;
    private Player player;
    private Timer timer;
    private NightEvent nightEvent;
    private GooseSwarmEvent swarmEvent;
    public static Inventory currentInventory;
    private Inventory inventory;
    private Array<Item> items = new Array<>();
    public boolean isPaused = true;
    public static boolean gameOver = false;

    public void create() {
        instance = this;
        spriteBatch = new SpriteBatch();
        
        viewport = new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        menuViewport = new ExtendViewport(16, 9);

        tiledMap = new TmxMapLoader().load("map/Goose Game Map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, mapUnitScale, spriteBatch);

        mapCollision = new MapCollision(tiledMap, worldMapWidth, worldMapHeight);
        pathfinder = new PathFinder(mapCollision, worldMapWidth, worldMapHeight);

        calculateFenceInterior();

        player = new Player(worldMapWidth, worldMapHeight);
        player.getSprite().setPosition(10f, 9f);
        player.setScore(0);
        player.setHealth(10);
        currentPlayer = player;

        waveManager = new GooseWaveManager("goose2.png", worldMapWidth, worldMapHeight, mapCollision, pathfinder, player);
        nightEvent = new NightEvent();
        swarmEvent = new GooseSwarmEvent(mapCollision, player);
        
        spawnInitialItems();
        
        inventory = new Inventory();
        currentInventory = inventory;
        setInputProcessor();
    }

    /**
     * Finds a random position on the map that does not overlap with any collision tile.
     * Uses a slightly larger padded box (0.6f x 0.6f for a 0.4f x 0.4f item) so items 
     * spawn comfortably away from walls.
     */
    
    /**
     * Finds the interior bounding box enclosed by the outer fence perimeter.
     * Checks a "Fence" tile layer first if present, otherwise uses the outer
     * bounds of the "Collision" layer.
     */
    private void calculateFenceInterior() {
        float minX = worldMapWidth;
        float minY = worldMapHeight;
        float maxX = 0f;
        float maxY = 0f;
        boolean found = false;

        // 1. Check if there is a dedicated "Fence" layer in Tiled
        com.badlogic.gdx.maps.MapLayer fenceLayer = tiledMap.getLayers().get("Fence");
        if (fenceLayer instanceof com.badlogic.gdx.maps.tiled.TiledMapTileLayer) {
            com.badlogic.gdx.maps.tiled.TiledMapTileLayer tLayer = (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) fenceLayer;
            float cellW = worldMapWidth / tLayer.getWidth();
            float cellH = worldMapHeight / tLayer.getHeight();

            for (int x = 0; x < tLayer.getWidth(); x++) {
                for (int y = 0; y < tLayer.getHeight(); y++) {
                    com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell cell = tLayer.getCell(x, y);
                    if (cell != null && cell.getTile() != null) {
                        minX = Math.min(minX, x * cellW);
                        minY = Math.min(minY, y * cellH);
                        maxX = Math.max(maxX, (x + 1) * cellW);
                        maxY = Math.max(maxY, (y + 1) * cellH);
                        found = true;
                    }
                }
            }
        }

        // 2. Otherwise derive the perimeter from the outer collision boxes
        if (!found && mapCollision != null) {
            for (Rectangle box : mapCollision.getCollisionBoxes()) {
                minX = Math.min(minX, box.x);
                minY = Math.min(minY, box.y);
                maxX = Math.max(maxX, box.x + box.width);
                maxY = Math.max(maxY, box.y + box.height);
                found = true;
            }
        }

        // Shrink inward by 1 tile (worldMapWidth / 100f = 0.375f) so spawns stay inside the fence
        float tileSize = worldMapWidth / 100f;
        if (found && (maxX - minX) > tileSize * 2f && (maxY - minY) > tileSize * 2f) {
            fenceInterior = new Rectangle(
                minX + tileSize,
                minY + tileSize,
                (maxX - minX) - (tileSize * 2f),
                (maxY - minY) - (tileSize * 2f)
            );
        } else {
            // Fallback to full map bounds if no fence/collision tiles exist
            fenceInterior = new Rectangle(0, 0, worldMapWidth, worldMapHeight);
        }
    }

    private Vector2 getValidSpawnPosition(float itemSize) {
        if (fenceInterior == null) {
            calculateFenceInterior();
        }

        float padding = 0.2f;
        float checkSize = itemSize + (padding * 2f);
        Rectangle testBounds = new Rectangle(0, 0, checkSize, checkSize);

        com.badlogic.gdx.maps.tiled.TiledMapTileLayer waterLayer = 
            (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) tiledMap.getLayers().get("Water");
        com.badlogic.gdx.maps.tiled.TiledMapTileLayer terrainLayer = 
            (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) tiledMap.getLayers().get("Terrain");

        float minSpawnX = fenceInterior.x + padding;
        float maxSpawnX = Math.max(minSpawnX, fenceInterior.x + fenceInterior.width - itemSize - padding);
        float minSpawnY = fenceInterior.y + padding;
        float maxSpawnY = Math.max(minSpawnY, fenceInterior.y + fenceInterior.height - itemSize - padding);

        for (int attempt = 0; attempt < 200; attempt++) {
            float x = minSpawnX + (float)(Math.random() * (maxSpawnX - minSpawnX));
            float y = minSpawnY + (float)(Math.random() * (maxSpawnY - minSpawnY));

            testBounds.setPosition(x - padding, y - padding);

            // 1. Skip if overlapping any wall/fence/collision block
            if (mapCollision != null && mapCollision.collides(testBounds)) {
                continue;
            }

            // Convert world coordinates (center of the item) into Tiled grid coordinates (0-99)
            float centerX = x + (itemSize / 2f);
            float centerY = y + (itemSize / 2f);
            int tileX = (int)((centerX / worldMapWidth) * 100);
            int tileY = (int)((centerY / worldMapHeight) * 100);

            // 2. Skip if a Water tile is present without terrain over it
            if (waterLayer != null) {
                com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell waterCell = waterLayer.getCell(tileX, tileY);
                if (waterCell != null && waterCell.getTile() != null) {
                    boolean hasTerrainOverWater = false;
                    if (terrainLayer != null) {
                        com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell terrainCell = terrainLayer.getCell(tileX, tileY);
                        if (terrainCell != null && terrainCell.getTile() != null) {
                            hasTerrainOverWater = true;
                        }
                    }
                    if (!hasTerrainOverWater) {
                        continue;
                    }
                }
            }

            return new Vector2(x, y);
        }

        // Fallback near player spawn
        return new Vector2(10f, 9f);
    }

    private void spawnBread() {
        Vector2 pos = getValidSpawnPosition(0.4f);
        items.add(new Bread(pos.x, pos.y, this));
    }

    private void spawnGoldenFeather() {
        Vector2 pos = getValidSpawnPosition(0.4f);
        items.add(new GoldenFeather(pos.x, pos.y, this));
    }

    private void spawnInitialItems() {
        for (int i = 0; i < MAX_BREAD; i++) {
            spawnBread();
        }
        for (int i = 0; i < MAX_GOLDEN_FEATHERS; i++) {
            spawnGoldenFeather();
        }
    }

    /**
     * Cleans up consumed items and replenishes Bread and Golden Feathers 
     * so they stay at their target counts throughout the game.
     */
    private void maintainItems() {
        int activeBreadCount = 0;
        int activeFeatherCount = 0;

        // Iterate backwards so we can safely remove used items
        for (int i = items.size - 1; i >= 0; i--) {
            Item item = items.get(i);

            // If Bread was thrown and finished its 10s distraction or was eaten, mark it finished
            if (item instanceof Bread) {
                Bread b = (Bread) item;
                if (b.itemUsed && !b.breadVisible) {
                    b.dispose();
                    items.removeIndex(i);
                    continue;
                }
            } else if (item.itemUsed) {
                item.dispose();
                items.removeIndex(i);
                continue;
            }

            if (item instanceof Bread) {
                activeBreadCount++;
            } else if (item instanceof GoldenFeather) {
                activeFeatherCount++;
            }
        }

        // Replenish missing Bread
        while (activeBreadCount < MAX_BREAD) {
            spawnBread();
            activeBreadCount++;
        }

        // Replenish missing Golden Feathers (keeps exactly 2 active in the game)
        while (activeFeatherCount < MAX_GOLDEN_FEATHERS) {
            spawnGoldenFeather();
            activeFeatherCount++;
        }
    }

    public void resize(int width, int height) {
        viewport.update(width, height, false);
        menuViewport.update(width, height, true);
        Menu.Updateviewport(menuViewport);
    }

    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        
        isPaused = (false == UIMenu.gameUI.IsAtTop());
        if (!isPaused) {
            player.update(viewport, worldMapWidth, worldMapHeight, this, mapCollision);
            waveManager.update(delta, player.getCenterPosition());
            nightEvent.update(delta);
            swarmEvent.update(player.getCenterPosition(), waveManager.getActiveGeese(), worldMapWidth, worldMapHeight);

            for (Item i : items) {
                if (i instanceof Bread) {
                    ((Bread) i).update(delta);
                }
            }

            // Clean up used items and replenish spawns
            maintainItems();
        }

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
                if (keycode == Input.Keys.E) {
                    Item selected = inventory.getSelected();
                    if (selected != null) {
                        selected.useItem(player, getGeese(), spriteBatch);
                    }
                } else if (keycode == Input.Keys.Q) {
                    inventory.selectPrevItem();
                } else if (keycode == Input.Keys.R) {
                    inventory.selectNextItem();
                }
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
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
        
        viewport.apply();
        OrthographicCamera cam = (OrthographicCamera) viewport.getCamera();
        mapRenderer.setView(cam);
        mapRenderer.render();

        spriteBatch.setProjectionMatrix(cam.combined);
        spriteBatch.begin();
        nightEvent.draw(spriteBatch, cam);

        for (Item i : items) {
            i.draw(spriteBatch);
        }

        player.draw(spriteBatch);
        waveManager.draw(spriteBatch);

        spriteBatch.end();

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
        nightEvent.dispose();
    }

    public Array<Goose> getGeese() {
        return waveManager.getActiveGeese();
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public Array<Item> getItems() {
        return this.items;
    }

    public static void resetGame() {
        if (instance != null) {
            instance.doReset();
        }
    }

    private void doReset() {
        Game.gameOver = false;
        
        if (player != null) {
            player.resetPlayer(10f, 9f);
        }
        
        if (items != null) {
            for (Item i : items) {
                i.dispose();
            }
            items.clear();
            spawnInitialItems();
        }
        
        if (currentInventory != null) {
            currentInventory.getItems().clear();
        }
        
        if (waveManager != null) {
            waveManager.dispose();
        }
        waveManager = new GooseWaveManager("goose2.png", worldMapWidth, worldMapHeight, mapCollision, pathfinder, player);
        nightEvent.dispose();
        nightEvent = new NightEvent();
        swarmEvent = new GooseSwarmEvent(mapCollision, player);
    }
    public int getActiveBreadCount() {
        int count = 0;
        for (Item item : items) {
            if (item instanceof Bread) {
                count++;
            }
        }
        return count;
    }
}