package io.github.goose_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;





public class Player extends Entity {

    private float speed = 3f;

    private Texture spriteSheet;
    private Animation<TextureRegion> walkRight;
    private Animation<TextureRegion> walkLeft;
    private TextureRegion standRight;
    private TextureRegion standLeft;
    private float stateTime = 0f;
    private boolean facingRight = true;
    private boolean moving = false;
    private float flashTimer = 0f;
    private Rectangle playerBounds = new Rectangle();
    private boolean isFlashing = false;
    private static final float FLASH_BLINK_RATE = 0.08f;
    private BitmapFont collisionFont;
    private SpriteBatch fontBatch;
    private static int score;
    private int maxHealth = 10;

    
    public Player(float mapWidth, float mapHeight) {
        super("test_player.png", 100, 1);

        spriteSheet = new Texture("studentSheet.png");
        TextureRegion[][] frames = TextureRegion.split(spriteSheet, 128, 128);

        walkRight = new Animation<>(0.15f, frames[1]);
        walkRight.setPlayMode(Animation.PlayMode.LOOP);
        walkLeft = new Animation<>(0.15f, frames[4]);
        walkLeft.setPlayMode(Animation.PlayMode.LOOP);

        standRight = frames[1][0];
        standLeft = frames[4][0];

        sprite.setSize(0.8f, 0.8f);
        sprite.setPosition((mapWidth - sprite.getWidth()) / 2f, (mapHeight - sprite.getHeight()) / 2f);
        playerBounds.setSize(sprite.getWidth(), sprite.getHeight());
        playerBounds.setPosition(sprite.getX(), sprite.getY());

        collisionFont = new BitmapFont();
        collisionFont.setColor(Color.WHITE);
        fontBatch = new SpriteBatch();

    }

    public void update(FitViewport viewport, float mapWidth, float mapHeight, Game game){
        input();
        playerGameLogic(viewport, mapWidth, mapHeight);

        for (Goose g : game.getGeese()){
            if (playerBounds.overlaps(g.getGooseBounds())){
                onCollision(g);
            }
        }
        for (Item i : game.getItems()){ 
            if (playerBounds.overlaps(i.getItemBounds())){
                if (!i.inInventory){
                    i.pickUp();
                }
            }
        }

        if (isFlashing){
            flashTimer -= Gdx.graphics.getDeltaTime();
            boolean visible = ((int)(flashTimer / FLASH_BLINK_RATE)) % 2 == 0;
            sprite.setColor(visible ? Color.WHITE : Color.CLEAR);

            if (flashTimer <= 0){
                isFlashing = false;
                sprite.setColor(Color.WHITE);
            }
        }


    }
    public void setHealth(int healthInput) {
        if (healthInput < maxHealth) {
            health = healthInput;
        }else {
            health = maxHealth;
        }
    }
    private void input() {
        float delta = Gdx.graphics.getDeltaTime();

        float xMovement = 0f;
        float yMovement = 0f;

        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            xMovement += 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            xMovement -= 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            yMovement += 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN)) {
            yMovement -= 1f;
        }

        if (xMovement != 0f || yMovement != 0f) {
            float length = (float) Math.sqrt(xMovement * xMovement + yMovement * yMovement);

            xMovement /= length;
            yMovement /= length;

            sprite.translateX(xMovement * speed * delta);
            sprite.translateY(yMovement * speed * delta);

            moving = true;
            if (xMovement > 0f) facingRight = true;
            else if (xMovement < 0f) facingRight = false;
            stateTime += delta;
        } else {
            moving = false;
            stateTime = 0f;
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        TextureRegion frame;


        if (moving) {
            frame = facingRight ? walkRight.getKeyFrame(stateTime) : walkLeft.getKeyFrame(stateTime);
        } else {
            frame = facingRight ? standRight : standLeft;
        }

        spriteBatch.setColor(sprite.getColor());


        spriteBatch.draw(frame, sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());

        spriteBatch.setColor(Color.WHITE);

    }

    private void playerGameLogic(FitViewport viewport, float mapWidth, float mapHeight) {
        float playerWidth = sprite.getWidth();
        float playerHeight = sprite.getHeight();

        sprite.setX(MathUtils.clamp(sprite.getX(), 0, mapWidth - playerWidth));
        sprite.setY(MathUtils.clamp(sprite.getY(), 0, mapHeight - playerHeight));

        playerBounds.setPosition(sprite.getX(), sprite.getY());


        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        float targetX = sprite.getX() + playerWidth / 2f;
        float targetY = sprite.getY() + playerHeight / 2f;
        float delta = Gdx.graphics.getDeltaTime();

        camera.position.x += (targetX - camera.position.x) * 5f * delta;
        camera.position.y += (targetY - camera.position.y) * 5f * delta;

        float halfViewportWidth = viewport.getWorldWidth() / 2f;
        float halfViewportHeight = viewport.getWorldHeight() / 2f;

        camera.position.x = MathUtils.clamp(camera.position.x, halfViewportWidth, mapWidth - halfViewportWidth);
        camera.position.y = MathUtils.clamp(camera.position.y, halfViewportHeight, mapHeight - halfViewportHeight);
        camera.update();
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void useItem() {
        // not sure how the item logic will work yet
    }

    @Override
    public void onCollision(Entity other_Entity) {
        if (other_Entity instanceof Goose){
            gooseHit((Goose) other_Entity);
        }
    }

    private void gooseHit(Goose goose){
        if (isFlashing) return;

        this.health -= 1;
        isFlashing = true;
        flashTimer = 1.5f;

        Vector2 playerCenter = getPosition();
        Vector2 gooseCenter = goose.getPosition();
        Vector2 knockback = new Vector2(playerCenter.x - gooseCenter.x, playerCenter.y - gooseCenter.y);
        if (knockback.len2() > 0) {
            knockback.nor().scl(1.5f);
        } else {
            knockback.set(0, 1.5f);
        }
        sprite.translate(knockback.x, knockback.y);
    }

    public int getScore(){
        return score;
    }

    public void setScore(int newScore){
        this.score = newScore;
    }


    @Override
    public void dispose() {
        super.dispose();
        spriteSheet.dispose();
        collisionFont.dispose();
        fontBatch.dispose();
    }
}
