package io.github.goose_game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;



public class Main extends ApplicationAdapter{
    
    public Texture playerTexture;
    public Texture backgroundTexture;

    SpriteBatch spriteBatch;
    FitViewport viewport;

    Sprite playerSprite;

    @Override
    public void create() {
        playerTexture = new Texture("test_player.png");
        backgroundTexture = new Texture("test_background.png");

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8,5);

        playerSprite = new Sprite(playerTexture);
        playerSprite.setSize(1,1);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }



    @Override
    public void render() {
        input();
        logic();
        draw();
        
    }


    private void input(){
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime(); // Consistent across different machines

        if (Gdx.input.isKeyPressed(Keys.RIGHT)){
            playerSprite.translateX(speed*delta);
        } else if (Gdx.input.isKeyPressed(Keys.LEFT)){
            playerSprite.translateX(-speed*delta);
        } else if (Gdx.input.isKeyPressed(Keys.UP)){
            playerSprite.translateY(speed*delta);
        } else if (Gdx.input.isKeyPressed(Keys.DOWN)){
            playerSprite.translateY(-speed*delta);
        }
        

    }

    private void logic(){

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        float playerWidth = playerSprite.getWidth();
        float playerHeight = playerSprite.getHeight();

        playerSprite.setX(MathUtils.clamp(playerSprite.getX(), 0, worldWidth-playerWidth));
        playerSprite.setY(MathUtils.clamp(playerSprite.getY(), 0, worldHeight-playerHeight));
    }

    private void draw(){

        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.draw(backgroundTexture, 0,0,worldWidth,worldHeight);
        playerSprite.draw(spriteBatch);


        spriteBatch.end();
    }

    @Override
    public void dispose() {
     
    }


}
