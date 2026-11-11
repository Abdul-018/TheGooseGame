package io.github.goose_game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.goose_game.Menus.LoseScreenMenu;
import io.github.goose_game.Menus.MainMenu;
import io.github.goose_game.Menus.Methods;
import io.github.goose_game.Menus.PauseMenu;
import io.github.goose_game.Menus.UIMenu;
import io.github.goose_game.Menus.WinScreenMenu;
import io.github.goose_game.Uimenu.Menu;
public class Main3 implements ApplicationListener {
    Texture backgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    //Sound dropSound;
    Sound paperTearSound;
    Sound paperShuffleSound;
    Music music;
    SpriteBatch spriteBatch;
    public ExtendViewport viewport;
    Sprite bucketSprite;
    Vector2 touchPos;
    Array<Sprite> dropSprites;
    float dropTimer;
    Rectangle bucketRectangle;
    Rectangle dropRectangle;
    static public int score = 0;
    public static int playerHealth = 10;
    @Override
    public void create() {
        Methods.LoadAllAssets();
        backgroundTexture = new Texture("background.png"); 
        bucketTexture = new Texture("bucket.png");
        dropTexture = new Texture("drop.png");
        //dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        paperTearSound = Gdx.audio.newSound(Gdx.files.internal("tearing_paper.mp3"));
        paperShuffleSound = Gdx.audio.newSound(Gdx.files.internal("shuffle_paper.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("Just Nasty.mp3"));
        spriteBatch = new SpriteBatch(); 
        viewport = new ExtendViewport(16, 9);
        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(1, 1);
        touchPos = new Vector2();
        dropSprites = new Array<>();
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
        setInputP();
        Methods.LoadAllObjects();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        Menu.Updateviewport(viewport);
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void input() {
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();
        if (UIMenu.gameUI.IsAtTop()){
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                bucketSprite.translateX(speed * delta);
            } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                bucketSprite.translateX(-speed * delta);
            }
            if (Gdx.input.isTouched()) {
                touchPos.set(Gdx.input.getX(), Gdx.input.getY());
                viewport.unproject(touchPos);
                bucketSprite.setCenterX(touchPos.x);
            }
        }
    }

    private void logic() {
        Menu.Updateall();
        if (UIMenu.gameUI.IsAtTop()){
            float worldWidth = viewport.getWorldWidth();
            //float worldHeight = viewport.getWorldHeight();
            float bucketWidth = bucketSprite.getWidth();
            float bucketHeight = bucketSprite.getHeight();

            bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));

            float delta = Gdx.graphics.getDeltaTime();
            bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);

            for (int i = dropSprites.size - 1; i >= 0; i--) {
                Sprite dropSprite = dropSprites.get(i);
                float dropWidth = dropSprite.getWidth();
                float dropHeight = dropSprite.getHeight();

                dropSprite.translateY(-2f * delta);
                dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

                if (dropSprite.getY() < -dropHeight) {dropSprites.removeIndex(i); if (playerHealth > 0){playerHealth -= 1; paperTearSound.play();} if(playerHealth<=0){PauseMenu.pauseMenu.Close();LoseScreenMenu.loseMenu.Open();}}
                else if (bucketRectangle.overlaps(dropRectangle)) {
                    dropSprites.removeIndex(i);
                    if (playerHealth < 10){playerHealth += 1;}else{PauseMenu.pauseMenu.Close();WinScreenMenu.winMenu.Open();}
                    score += 1;
                    paperShuffleSound.play();
                }
            }
            dropTimer += delta;
            if (dropTimer > 1f) {
                dropTimer = 0;
                createDroplet();
            }
        }
    }
    private void setInputP() {
        Gdx.input.setInputProcessor(new InputAdapter () {

            @Override
            public boolean touchUp (int x, int y, int pointer, int button) {
                System.out.println("click!!!");
                return Menu.Inputall(x,y,viewport);
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
        
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        bucketSprite.draw(spriteBatch);

        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }
        System.out.println("drawstart");
        Menu.Drawall(spriteBatch);


        spriteBatch.end();
    }

    private void createDroplet() {
        float dropWidth = 1;
        float dropHeight = 1;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth));
        dropSprite.setY(worldHeight);
        dropSprites.add(dropSprite);
    }

    @Override
    public void pause() {
        
    }

    @Override
    public void resume() {
        
    }

    @Override
    public void dispose() {
        
    }
}
