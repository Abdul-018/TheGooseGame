package io.github.goose_game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

// Loads the goose sheets once so every goose shares them. Game owns this and disposes it.
public class GooseSprites {

    private final Sheet day = new Sheet("gooseSheet.png");
    private final Sheet night = new Sheet("gooseSheetNight.png");

    public TextureRegion getFrame(boolean redEyes, boolean moving, boolean facingRight, float stateTime) {
        Sheet sheet = redEyes ? night : day;
        if (moving) {
            return facingRight ? sheet.walkRight.getKeyFrame(stateTime) : sheet.walkLeft.getKeyFrame(stateTime);
        }
        return facingRight ? sheet.standRight : sheet.standLeft;
    }

    public void dispose() {
        day.texture.dispose();
        night.texture.dispose();
    }

    private static class Sheet {
        private final Texture texture;
        private final Animation<TextureRegion> walkRight;
        private final Animation<TextureRegion> walkLeft;
        private final TextureRegion standRight;
        private final TextureRegion standLeft;

        private Sheet(String path) {
            texture = new Texture(path);
            TextureRegion[][] frames = TextureRegion.split(texture, 128, 128);

            // row 2 (index 1) = walk right, row 5 (index 4) = walk left. other rows are for blinking and mouth
            walkRight = new Animation<>(0.15f, frames[1]);
            walkRight.setPlayMode(Animation.PlayMode.LOOP);
            walkLeft = new Animation<>(0.15f, frames[4]);
            walkLeft.setPlayMode(Animation.PlayMode.LOOP);

            standRight = frames[1][0];
            standLeft = frames[4][0];
        }
    }
}
