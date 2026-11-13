package io.github.goose_game;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class CollisionLoader {

    private static final String[] BLOCKED_LAYERS = {"Collision"};

    public static Array<Rectangle> loadCollisionRectangles(TiledMap map) {

        Array<Rectangle> collisionRects = new Array<>();

        for (String layerName : BLOCKED_LAYERS) {
            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
            if (layer == null) continue;

            float tileW = layer.getTileWidth();
            float tileH = layer.getTileHeight();

            for (int y = 0; y < layer.getHeight(); y++) {
                for (int x = 0; x < layer.getWidth(); x++) {
                    if (layer.getCell(x, y) != null) {
                        collisionRects.add(new Rectangle(x * tileW, y * tileH, tileW, tileH));
                    }
                }
            }
        }

        return collisionRects;
    }
}
