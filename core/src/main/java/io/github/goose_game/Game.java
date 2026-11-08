package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Game {

    private final float mapWidth = 24f;
    private final float mapHeight = 15f;

    private Texture backgroundTexture;

    private SpriteBatch spriteBatch;
    private FitViewport viewport;

    private Player player;
    private Timer timer;
    private Array<Goose> geese = new Array<>();
    private Array<Item> items = new Array<>();

    public void create() {
        backgroundTexture = new Texture("test_background.png");

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        player = new Player(mapWidth, mapHeight);
        player.setScore(0);
        for (int i = 0; i < 3; i++) {
            float x = (float)(Math.random() * mapWidth);
            float y = (float)(Math.random() * mapHeight);
            geese.add(new Goose("goose.png", x, y, 3, player));
        }

        for (int i = 0; i < 100; i++){
            float x = (float)(Math.random() * (mapWidth-  0.2f));
            float y = (float)(Math.random() * (mapHeight-0.2f));
            items.add(new Bread(x,y));
        }

        for (int i = 0; i < 20; i++){
            float x = (float)(Math.random() * (mapWidth-  0.2f));
            float y = (float)(Math.random() * (mapHeight-0.2f));
            items.add(new GoldenFeather(x,y));
        }

        

    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        player.update(viewport, mapWidth, mapHeight, this);
        for (Goose g : geese) g.update(delta, player.getPosition(), geese);
        draw();

    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        spriteBatch.draw(backgroundTexture, 0, 0, mapWidth, mapHeight);
        for (Item i : items){
            if (!i.collected){
                i.draw(spriteBatch);
            }
        }
        player.draw(spriteBatch);
        for (Goose g : geese) {
            g.draw(spriteBatch);
        }
        spriteBatch.end();
    }

    public void dispose() {
        spriteBatch.dispose();
        backgroundTexture.dispose();
        player.dispose();
        for (Goose g : geese) g.dispose();
        for (Item i : items) i.dispose();
    }

    public Array<Goose> getGeese(){
        return this.geese;
    }

    public Array<Item> getItems(){
        return this.items;
    }

}
