package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.goose_game.Menus.MainMenu;
import io.github.goose_game.Menus.PauseMenu;
import io.github.goose_game.Menus.UIMenu;
import io.github.goose_game.Uimenu.Menu;
public class Game {

    private final float mapWidth = 24f;
    private final float mapHeight = 15f;
    private Texture backgroundTexture;
    public static ExtendViewport menuViewport;
    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    public static Player currentPlayer;
    private Player player;
    private Timer timer;
    private Array<Goose> geese = new Array<>();
    private Array<Item> items = new Array<>();
    public boolean isPaused = true;

    public void create() {
        backgroundTexture = new Texture("test_background.png");
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
        menuViewport = new ExtendViewport(16,9);
        player = new Player(mapWidth, mapHeight);
        player.setScore(0);
        currentPlayer = player;
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

        setInputProcessor();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, false);
        menuViewport.update(width, height, true);
        Menu.Updateviewport(menuViewport);
    }

    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        isPaused = (false == UIMenu.gameUI.IsAtTop());
        if (isPaused == false){
            player.update(viewport, mapWidth, mapHeight, this);
            for (Goose g : geese) g.update(delta, player.getPosition(), geese);
        }
        draw();
    }

    private void setInputProcessor() {
        Gdx.input.setInputProcessor(new InputAdapter () {

            @Override
            public boolean touchUp (int x, int y, int pointer, int button) {
                System.out.println("click!!!");
                return Menu.Inputall(x,y,menuViewport);
            }
            @Override 
            public boolean keyUp (int keycode) {
                if (keycode == Input.Keys.ESCAPE){
                    System.out.println("escape pressed");
                    if (UIMenu.gameUI.IsAtTop()){
                        PauseMenu.pauseMenu.Open();
                    }else if (MainMenu.mainMenu.IsAtTop()){
                        Gdx.app.exit();
                    }else if (PauseMenu.pauseMenu.IsAtTop()){
                        PauseMenu.pauseMenu.Close();
                    }else{
                    Menu.Inputall(keycode);}
                }else{
                    Menu.Inputall(keycode);
                }
                return false;
            }
        });
    }

    private void draw() {
        //UIMenu.gameUI.Open();
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
        menuViewport.apply();
        spriteBatch.setProjectionMatrix(menuViewport.getCamera().combined);
        spriteBatch.begin();
        Menu.Drawall(spriteBatch);
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
