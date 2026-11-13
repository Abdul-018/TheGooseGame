package io.github.goose_game.Menus;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import com.badlogic.gdx.graphics.Color;

import io.github.goose_game.Game;
import io.github.goose_game.Inventory;
import io.github.goose_game.Uimenu.Menu;
import io.github.goose_game.Uimenu.Uielement;
public class UIMenu {
    public static Menu gameUI;
    static Texture healthBook;
    static Texture healthBookTorn1Darkened;
    static Texture healthBookTorn2Darkened;
    static Texture empty;
    static ArrayList<Pixmap> numTextures;
    public static int scoreNum = 0;
    static int healthy = 62;
    private static int lastScoreUpdate = 0;
    private static int lastTimerUpdate = -1;
    private static int lastWaveUpdate = -1;
    static Pixmap score;
    static ArrayList<Uielement> gameUIElements;
    public static MenuGenerator gameUIMethods = new MenuGenerator(0){
        @Override
        public void LoadAssets(){
            numTextures = new ArrayList<>();
            Pixmap numSheet = new Pixmap(Gdx.files.internal("menus/uimenu_nums.png"));
            for (int i = 0; i < 10; i++) {
                Pixmap pixmap = new Pixmap( 7, 15, Pixmap.Format.RGBA8888 );
                pixmap.drawPixmap(numSheet, -9*i, 0);
                numTextures.add(pixmap);
            }
            numSheet.dispose();
            healthBook =  new Texture("menus/health_book.png");
            score = new Pixmap(Gdx.files.internal("menus/uimenu_score.png"));
            healthBookTorn1Darkened =  new Texture("menus/health_book_torn1_darkened.png");
            healthBookTorn2Darkened =  new Texture("menus/health_book_torn2_darkened.png");
            empty = new Texture("items/empty.png");
        }
        @Override
        public void LoadObjects(){
            gameUIElements = new ArrayList<>();
            gameUIElements.add(new Uielement(empty, new float[]{0,-3}, Uielement.Align.LEFT, 1, 1, 255){
                @Override
                public boolean ProcessesKey(int passedInput){
                    return (passedInput == Input.Keys.ESCAPE);
                }
                @Override
                public boolean OnKey(int passedInput){
                    PauseMenu.pauseMenu.Open();
                    return true;
                }
            });
            gameUIElements.add(new Uielement(MakeScoreTexture(0),new float[]{640,695},Uielement.Align.MIDDLE,1f,2f,1){
                @Override
                public void Update(){
                    scoreNum = Game.currentPlayer.getScore();
                    if (scoreNum != lastScoreUpdate){
                        this.setTexture(MakeScoreTexture(scoreNum));
                        lastScoreUpdate = scoreNum;
                    }
                }
            });
            // timer display in the top left
            gameUIElements.add(new Uielement(MakeTimerTexture(0),new float[]{80,695},Uielement.Align.LEFT,1f,2f,1){
                @Override
                public void Update(){
                    int secs = (int) Game.gameTime;
                    if (secs != lastTimerUpdate){
                        this.setTexture(MakeTimerTexture(secs));
                        lastTimerUpdate = secs;
                    }
                }
            });
            // inventory display on the left side
            gameUIElements.add(new Uielement(MakeInventoryTexture(),new float[]{64,330},Uielement.Align.LEFT,1f,4f,1){
                @Override
                public void Update(){
                    this.pos[1] = 640-4*(4+(24+(Game.currentInventory.getItems().size()-1)*28f)/2);
                    this.setTexture(MakeInventoryTexture());
                }
            });
            gameUIElements.add(new Uielement(healthBook,new float[]{1206,healthy},Uielement.Align.RIGHT,1f,4f,1){
                @Override
                public void Update(){
                    int health = Game.currentPlayer.getHealth()-0;
                    if (health > 1){
                        this.setTexture(healthBook);
                    }else if(health>0){
                        this.setTexture(healthBookTorn1Darkened);
                    }else{
                        this.setTexture(healthBookTorn2Darkened);
                    }
                }
            });
            gameUIElements.add(new Uielement(healthBook,new float[]{1090,healthy},Uielement.Align.RIGHT,1f,4f,1){
                @Override
                public void Update(){
                    int health = Game.currentPlayer.getHealth()-2;
                    if (health > 1){
                        this.setTexture(healthBook);
                    }else if(health>0){
                        this.setTexture(healthBookTorn1Darkened);
                    }else{
                        this.setTexture(healthBookTorn2Darkened);
                    }
                }
            });
            gameUIElements.add(new Uielement(healthBook,new float[]{974,healthy},Uielement.Align.RIGHT,1f,4f,1){
                @Override
                public void Update(){
                    int health = Game.currentPlayer.getHealth()-4;
                    if (health > 1){
                        this.setTexture(healthBook);
                    }else if(health>0){
                        this.setTexture(healthBookTorn1Darkened);
                    }else{
                        this.setTexture(healthBookTorn2Darkened);
                    }
                }
            });
            gameUIElements.add(new Uielement(healthBook,new float[]{974-116,healthy},Uielement.Align.RIGHT,1f,4f,1){
                @Override
                public void Update(){
                    int health = Game.currentPlayer.getHealth()-6;
                    if (health > 1){
                        this.setTexture(healthBook);
                    }else if(health>0){
                        this.setTexture(healthBookTorn1Darkened);
                    }else{
                        this.setTexture(healthBookTorn2Darkened);
                    }
                }
            });
            gameUIElements.add(new Uielement(healthBook,new float[]{974-116*2,healthy},Uielement.Align.RIGHT,1f,4f,1){
                @Override
                public void Update(){
                    int health = Game.currentPlayer.getHealth()-8;
                    if (health > 1){
                        this.setTexture(healthBook);
                    }else if(health>0){
                        this.setTexture(healthBookTorn1Darkened);
                    }else{
                        this.setTexture(healthBookTorn2Darkened);
                    }
                }
            });
            // wave counter in the bottom left
            gameUIElements.add(new Uielement(MakeWaveTexture(1),new float[]{80,30},Uielement.Align.LEFT,1f,2f,1){
                @Override
                public void Update(){
                    int wave = Game.instance.getWaveManager().getCurrentWave();
                    if (wave != lastWaveUpdate){
                        this.setTexture(MakeWaveTexture(wave));
                        lastWaveUpdate = wave;
                    }
                }
            });
            gameUI = new Menu(gameUIElements,Menu.Passtype.ALL,true);
            gameUI.Open();
        }
    };

    public static Texture MakeInventoryTexture(){
        Inventory inv = Game.currentInventory;
        if (inv == null || inv.getItems().isEmpty()){
            return empty;
        }else{
            int size = inv.getItems().size();
            int curIndex = inv.GetCurrItemIndex();
            Pixmap inventoryBG = new Pixmap(Gdx.files.internal("items/itembg.png"));
            Pixmap inventorySelect = new Pixmap(Gdx.files.internal("items/itemselect.png"));
            Pixmap pixmap = new Pixmap(24,24+(size-1)*(28),Pixmap.Format.RGBA8888);
            for (int i = 0; i < size; i++) {
                pixmap.drawPixmap(inventoryBG, 0, i*28);
                Pixmap itemGraphic = new Pixmap(Gdx.files.internal(inv.getItems().get(i).invGraphicInternalAddress));
                pixmap.drawPixmap(itemGraphic, 4, i*28+4);
                if (i == curIndex){
                    pixmap.drawPixmap(inventorySelect, 1, i*28+1);
                }
                itemGraphic.dispose();
            }
            Texture output = new Texture(pixmap);
            pixmap.dispose();
            inventoryBG.dispose();
            inventorySelect.dispose();
            return output;
        }
    }

    public static Texture MakeScoreTexture(int num){
        String str = Integer.toString(num);
        Pixmap raw = new Pixmap(52 + 9 * str.length(), 15, Pixmap.Format.RGBA8888);
        raw.drawPixmap(score, 0, 0);
        int div = 1;
        for (int i = 0; i < str.length(); i++) {
            raw.drawPixmap(numTextures.get((scoreNum/div)%10), 54 + 9*(str.length()-i-1), 0);
            div *= 10;
        }
        Pixmap outlined = addOutline(raw);
        raw.dispose();
        Texture output = new Texture(outlined);
        outlined.dispose();
        return output;
    }

    // renders the timer as M:SS or MM:SS using the same number sprites
    public static Texture MakeTimerTexture(int totalSeconds){
        int mins = totalSeconds / 60;
        int secs = totalSeconds % 60;
        String mStr = Integer.toString(mins);
        String sStr = (secs < 10 ? "0" : "") + secs;
        int contentW = 9 * mStr.length() + 5 + 9 * 2;
        Pixmap raw = new Pixmap(contentW, 15, Pixmap.Format.RGBA8888);
        int x = 0;
        for (int i = 0; i < mStr.length(); i++) {
            int digit = mStr.charAt(i) - '0';
            raw.drawPixmap(numTextures.get(digit), x, 0);
            x += 9;
        }
        // colon
        raw.setColor(Color.WHITE);
        raw.fillRectangle(x + 1, 4, 2, 2);
        raw.fillRectangle(x + 1, 9, 2, 2);
        x += 5;
        for (int i = 0; i < sStr.length(); i++) {
            int digit = sStr.charAt(i) - '0';
            raw.drawPixmap(numTextures.get(digit), x, 0);
            x += 9;
        }
        Pixmap outlined = addOutline(raw);
        raw.dispose();
        Texture output = new Texture(outlined);
        outlined.dispose();
        return output;
    }

    // renders "W" + wave number using the number sprites
    public static Texture MakeWaveTexture(int wave){
        String str = Integer.toString(wave);
        // "W" is 9px wide, then a 2px gap, then the digits
        int contentW = 9 + 2 + 9 * str.length();
        Pixmap raw = new Pixmap(contentW, 15, Pixmap.Format.RGBA8888);
        // draw a pixel art W
        raw.setColor(Color.WHITE);
        // left leg
        raw.fillRectangle(0, 0, 1, 13);
        // bottom left diagonal
        raw.fillRectangle(1, 11, 1, 2);
        raw.fillRectangle(2, 12, 1, 2);
        // middle peak
        raw.fillRectangle(3, 10, 1, 3);
        raw.fillRectangle(4, 9, 1, 2);
        // bottom right diagonal
        raw.fillRectangle(5, 10, 1, 3);
        raw.fillRectangle(6, 12, 1, 2);
        raw.fillRectangle(7, 11, 1, 2);
        // right leg
        raw.fillRectangle(8, 0, 1, 13);
        // draw the wave number digits
        int x = 11;
        for (int i = 0; i < str.length(); i++) {
            int digit = str.charAt(i) - '0';
            raw.drawPixmap(numTextures.get(digit), x, 0);
            x += 9;
        }
        Pixmap outlined = addOutline(raw);
        raw.dispose();
        Texture output = new Texture(outlined);
        outlined.dispose();
        return output;
    }

    // draws a 1px black border around any non-transparent pixels
    private static Pixmap addOutline(Pixmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        // extra pixel on each side for the outline
        Pixmap dst = new Pixmap(w + 2, h + 2, Pixmap.Format.RGBA8888);
        // first pass: stamp black at all neighbors of visible pixels
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                int pixel = src.getPixel(px, py);
                int alpha = pixel & 0xFF;
                if (alpha > 0) {
                    dst.setColor(0, 0, 0, 1f);
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            dst.drawPixel(px + 1 + dx, py + 1 + dy);
                        }
                    }
                }
            }
        }
        // second pass: draw the actual content on top
        dst.drawPixmap(src, 1, 1);
        return dst;
    }
}
