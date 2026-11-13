package io.github.goose_game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

// Loads every goose sheet once so all geese share them. Game owns this and disposes it.
public class GooseSprites {

    // night sheets are the same as the day ones but with red eyes, in the same order
    private final Sheet[] daySheets = {
        new Sheet("gooseSheet.png"),
        new Sheet("gooseSheet2.png"),
        new Sheet("gooseSheet3.png")
    };
    private final Sheet[] nightSheets = {
        new Sheet("gooseSheetNight.png"),
        new Sheet("gooseSheet2Night.png"),
        new Sheet("gooseSheet3Night.png")
    };

    public int getVariantCount() {
        return daySheets.length;
    }

    public TextureRegion getFrame(int variant, boolean night, boolean moving, boolean facingRight, float stateTime) {
        Sheet sheet = night ? nightSheets[variant] : daySheets[variant];
        if (moving) {
            return facingRight ? sheet.walkRight.getKeyFrame(stateTime) : sheet.walkLeft.getKeyFrame(stateTime);
        }
        return facingRight ? sheet.standRight : sheet.standLeft;
    }

    public void dispose() {
        for (Sheet sheet : daySheets) {
            sheet.texture.dispose();
        }
        for (Sheet sheet : nightSheets) {
            sheet.texture.dispose();
        }
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
