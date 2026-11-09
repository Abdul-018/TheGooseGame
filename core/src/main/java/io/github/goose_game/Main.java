package io.github.goose_game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.OrthographicCamera;



public class Main extends ApplicationAdapter{
    
    public Texture playerTexture;
    public Texture backgroundTexture;

    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite playerSprite;

    float mapWidth = 18f;
    float mapHeight = 10f;
    
    private float time = 300f;
    private BitmapFont font;
    private boolean gameOver = false;
    private boolean win = false;

    @Override
    public void create() {
        playerTexture = new Texture("test_player.png");
        backgroundTexture = new Texture("test_background.png");

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8f,5f);

        playerSprite = new Sprite(playerTexture);
        playerSprite.setSize(1,1);

        playerSprite.setPosition(mapWidth/2f, mapHeight/2f);

        font = new BitmapFont();
        font.getData().setScale(0.04f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
    
    private Rectangle mouseHitbox = new Rectangle(0, 0, 32, 32);



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
        if (Gdx.input.isKeyJustPressed(Keys.W)) {
            win = true;
        }
        

    }

    private void logic(){

        if (gameOver || win) return;

        float delta = Gdx.graphics.getDeltaTime();
        
        if (time > 0) {
            time -= delta;
            if (time <= 0) {
                time = 0;
                gameOver = true; // Trigger lose state when time runs out
            }
        }
        
        float playerWidth = playerSprite.getWidth();
        float playerHeight = playerSprite.getHeight();

        playerSprite.setX(MathUtils.clamp(playerSprite.getX(), 0, mapWidth-playerWidth));
        playerSprite.setY(MathUtils.clamp(playerSprite.getY(), 0, mapHeight-playerHeight));

        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();

        float targetX = playerSprite.getX() + playerWidth /2f;
        float targetY = playerSprite.getY() + playerHeight/2f;

        camera.position.x += (targetX - camera.position.x) * 5f * delta;
        camera.position.y += (targetY - camera.position.y) * 5f * delta;

        float halfWidth = viewport.getWorldWidth() / 2f;
        float halfHeight = viewport.getWorldHeight() / 2f;

        camera.position.x = MathUtils.clamp(camera.position.x, halfWidth, mapWidth - halfWidth);
        camera.position.y = MathUtils.clamp(camera.position.y, halfHeight, mapHeight-halfHeight);

        camera.update();


    }

    private void draw(){

        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        spriteBatch.draw(backgroundTexture, 0,0,mapWidth,mapHeight);
        playerSprite.draw(spriteBatch);

        if (win) {
            font.draw(spriteBatch, "YOU WIN! (placeholder)", 2.5f, 3f);
        } else if (gameOver) {
            font.draw(spriteBatch, "GAME OVER - OUT OF TIME", 2.3f, 3f);
        }
        else {
            int minutes = (int) (time / 60);
            int seconds = (int) (time % 60);
            String timeText = String.format("Time: %02d:%02d", minutes, seconds);
            font.draw(spriteBatch, timeText, 0.5f, 4.5f);
        }


        spriteBatch.end();
    }

    @Override
    public void dispose() {
        
        playerTexture.dispose();
        backgroundTexture.dispose();
        spriteBatch.dispose();
        font.dispose();

    }


}
