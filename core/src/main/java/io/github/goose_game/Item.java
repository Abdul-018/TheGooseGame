package io.github.goose_game;

import com.badlogic.gdx.math.Vector2;

public class Item {

    private static int IdCounter = 0;
    private final int id;
    private final String type;
    private final Vector2 position;

    protected Item(String type, float xPos, float yPos) {
        this.id = IdCounter++;
        this.type = type;
        this.position = new Vector2(xPos, yPos);
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Vector2 getPosition() {
        return position;
    }
}
