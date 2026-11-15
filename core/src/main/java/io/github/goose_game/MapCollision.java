package io.github.goose_game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class MapCollision {

    // list of all collision boxes across the map
    private Array<Rectangle> collisionBoxes = new Array<>();

    // spatial grid so collides() only checks nearby boxes
    private Array<Rectangle>[][] grid;
    private int gridCols;
    private int gridRows;
    private float cellW;
    private float cellH;

    // loads collision boxes when the map is provided
    public MapCollision(TiledMap map, float mapWidth, float mapHeight) {
        loadCollisions(map, mapWidth, mapHeight);
    }

    // scans the tiled map layer and turns filled tiles into collision rects
    public void loadCollisions(TiledMap map, float mapWidth, float mapHeight) {
        collisionBoxes.clear();

        MapLayer layer = map.getLayers().get("Collision");
        if (layer instanceof TiledMapTileLayer) {
            TiledMapTileLayer tLayer = (TiledMapTileLayer) layer;

            float cellWidth = mapWidth / tLayer.getWidth();
            float cellHeight = mapHeight / tLayer.getHeight();

            // loop through every tile cell on the map grid
            for (int x = 0; x < tLayer.getWidth(); x++) {
                for (int y = 0; y < tLayer.getHeight(); y++) {
                    TiledMapTileLayer.Cell cell = tLayer.getCell(x, y);
                    // if theres a tile present make a collision box for it
                    if (cell != null && cell.getTile() != null) {
                        collisionBoxes.add(new Rectangle(
                            x * cellWidth,
                            y * cellHeight,
                            cellWidth,
                            cellHeight
                        ));
                    }
                }
            }
        }

        buildSpatialGrid(mapWidth, mapHeight);
    }

    @SuppressWarnings("unchecked")
    private void buildSpatialGrid(float mapWidth, float mapHeight) {
        gridCols = 20;
        gridRows = 20;
        cellW = mapWidth / gridCols;
        cellH = mapHeight / gridRows;

        grid = new Array[gridCols][gridRows];
        for (int gx = 0; gx < gridCols; gx++) {
            for (int gy = 0; gy < gridRows; gy++) {
                grid[gx][gy] = new Array<>();
            }
        }

        for (int i = 0; i < collisionBoxes.size; i++) {
            Rectangle box = collisionBoxes.get(i);
            int minGX = Math.max(0, (int)(box.x / cellW));
            int minGY = Math.max(0, (int)(box.y / cellH));
            int maxGX = Math.min(gridCols - 1, (int)((box.x + box.width) / cellW));
            int maxGY = Math.min(gridRows - 1, (int)((box.y + box.height) / cellH));
            for (int gx = minGX; gx <= maxGX; gx++) {
                for (int gy = minGY; gy <= maxGY; gy++) {
                    grid[gx][gy].add(box);
                }
            }
        }
    }

    // checks if a given bounding box overlaps with any wall in the map
    public boolean collides(Rectangle bounds) {
        int minGX = Math.max(0, (int)(bounds.x / cellW));
        int minGY = Math.max(0, (int)(bounds.y / cellH));
        int maxGX = Math.min(gridCols - 1, (int)((bounds.x + bounds.width) / cellW));
        int maxGY = Math.min(gridRows - 1, (int)((bounds.y + bounds.height) / cellH));

        for (int gx = minGX; gx <= maxGX; gx++) {
            for (int gy = minGY; gy <= maxGY; gy++) {
                Array<Rectangle> cell = grid[gx][gy];
                for (int i = 0; i < cell.size; i++) {
                    if (cell.get(i).overlaps(bounds)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Array<Rectangle> getCollisionBoxes() {
        return collisionBoxes;
    }
}