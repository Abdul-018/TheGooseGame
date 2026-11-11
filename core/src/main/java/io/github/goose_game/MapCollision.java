package io.github.goose_game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class MapCollision {

    // list of all collision boxes across the map
    private Array<Rectangle> collisionBoxes = new Array<>();

    //  loads collision boxes when the map is provided
    public MapCollision(TiledMap map, float mapWidth, float mapHeight) {
        loadCollisions(map, mapWidth, mapHeight);
    }

    // scans the tiled map layer and turns filled tiles into collision rects
    public void loadCollisions(TiledMap map, float mapWidth, float mapHeight) {
        collisionBoxes.clear();

        MapLayer layer = map.getLayers().get("CollisionLayer");
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
    }

    // checks if a given bounding box overlaps with any wall in the map
    public boolean collides(Rectangle bounds) {
        for (int i = 0; i < collisionBoxes.size; i++) {
            if (collisionBoxes.get(i).overlaps(bounds)) {
                return true;
            }
        }
        return false;
    }

    public Array<Rectangle> getCollisionBoxes() {
        return collisionBoxes;
    }
}