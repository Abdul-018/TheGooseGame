package io.github.goose_game.Menus;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import io.github.goose_game.Game;
import io.github.goose_game.GoldenFeather;
import io.github.goose_game.Inventory;
import io.github.goose_game.Uimenu.Menu;
import io.github.goose_game.Uimenu.Uielement;

public class UIMenu {
    public static Menu gameUI;
    static Texture healthBook;
    static Texture healthBookTorn1Darkened;
    static Texture healthBookTorn2Darkened;
    static Texture eventNight;
    static Texture eventSwarm;
    static Texture empty;
    static ArrayList<Pixmap> numTextures;
    public static int scoreNum = 0;
    static int healthy = 62;
    private static int lastScoreUpdate = -1;
    private static int lastTimerUpdate = -1;
    private static int lastWaveUpdate = -1;
    private static int lastFeatherUpdate = -1;
    private static int lastEventUpdate = -1;
    static Pixmap score;
    static Pixmap featherIcon;
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
            healthBook = new Texture("menus/health_book.png");
            score = new Pixmap(Gdx.files.internal("menus/uimenu_score.png"));
            
            healthBookTorn1Darkened = new Texture("menus/health_book_torn1_darkened.png");
            healthBookTorn2Darkened = new Texture("menus/health_book_torn2_darkened.png");
            eventNight = new Texture("menus/eventoverlay_night.png");
            eventSwarm = new Texture("menus/eventoverlay_swarm.png");
            empty = new Texture("items/empty.png");

            Pixmap fullFeather = new Pixmap(Gdx.files.internal("items/golden_feather.png"));
            int iconH = 15;
            int iconW = (int)((float)fullFeather.getWidth() / fullFeather.getHeight() * iconH);
            featherIcon = new Pixmap(iconW, iconH, Pixmap.Format.RGBA8888);
            featherIcon.setFilter(Pixmap.Filter.BiLinear);
            featherIcon.drawPixmap(fullFeather,
                0, 0, fullFeather.getWidth(), fullFeather.getHeight(),
                0, 0, iconW, iconH);
            fullFeather.dispose();
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

            // Existing Score UI
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

            gameUIElements.add(new Uielement(MakeTimerTexture(0),new float[]{80,695},Uielement.Align.LEFT,1f,2f,1){
                @Override
                public void Update(){
                    int totalSeconds = (int) Game.gameTime;
                    if (totalSeconds != lastTimerUpdate){
                        this.setTexture(MakeTimerTexture(totalSeconds));
                        lastTimerUpdate = totalSeconds;
                    }
                }
            });
            
            gameUIElements.add(new Uielement(empty,new float[]{640,600},Uielement.Align.MIDDLE,1f,4f,1){
                @Override
                public void Update(){
                    if (Game.lastEventID != -1){
                        if (Game.lastEventOverlayTime > 0){
                            if (lastEventUpdate != Game.lastEventID){
                                lastEventUpdate = Game.lastEventID;
                                if (lastEventUpdate == 1){
                                    this.setTexture(eventNight);
                                }else if(lastEventUpdate == 2){
                                    this.setTexture(eventSwarm);
                                }
                            }
                        }else{
                            this.setTexture(empty);
                            Game.lastEventID = -1;
                            lastEventUpdate = -1;
                        }
                    }
                }
            });

            gameUIElements.add(new Uielement(MakeWaveTexture(1),new float[]{80,30},Uielement.Align.LEFT,1f,2f,1){
                @Override
                public void Update(){
                    if (Game.instance != null && Game.instance.getWaveManager() != null) {
                        int wave = Game.instance.getWaveManager().getCurrentWave();
                        if (wave != lastWaveUpdate){
                            this.setTexture(MakeWaveTexture(wave));
                            lastWaveUpdate = wave;
                        }
                    }
                }
            });

            gameUIElements.add(new Uielement(empty,new float[]{1200,695},Uielement.Align.RIGHT,1f,2f,1){
                @Override
                public void Update(){
                    GoldenFeather feather = Game.activeFeather;
                    if (feather != null && feather.isEffectActive()) {
                        int secs = (int) Math.ceil(feather.getTimeRemaining());
                        if (secs != lastFeatherUpdate) {
                            this.setTexture(MakeFeatherTimerTexture(secs));
                            lastFeatherUpdate = secs;
                        }
                    } else {
                        if (lastFeatherUpdate != -1) {
                            this.setTexture(empty);
                            lastFeatherUpdate = -1;
                        }
                    }
                }
            });

            // Existing Inventory UI
            gameUIElements.add(new Uielement(MakeInventoryTexture(),new float[]{64,330},Uielement.Align.LEFT,1f,4f,1){
                @Override 
                public void Update(){
                    this.pos[1] = 640-4*(4+(24+(Game.currentInventory.getItems().size()-1)*28f)/2);
                    this.setTexture(MakeInventoryTexture());
                }
            });

            // Existing Health Books UI
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
            gameUI = new Menu(gameUIElements,Menu.Passtype.ALL,true);
            gameUI.Open();
        }
    };

    // black outline around text
    public static Pixmap addOutline(Pixmap src) {
        int w = src.getWidth() + 2;
        int h = src.getHeight() + 2;
        Pixmap out = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        for (int dx = 0; dx <= 2; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                if (dx == 1 && dy == 1) continue;
                out.drawPixmap(src, dx, dy);
            }
        }
        // black version
        Pixmap blackSrc = new Pixmap(src.getWidth(), src.getHeight(), Pixmap.Format.RGBA8888);
        for (int x = 0; x < src.getWidth(); x++) {
            for (int y = 0; y < src.getHeight(); y++) {
                int pixel = src.getPixel(x, y);
                int alpha = pixel & 0xFF;
                if (alpha > 0) {
                    blackSrc.drawPixel(x, y, 0x000000FF);
                }
            }
        }
        // border then original on top
        Pixmap result = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        for (int dx = 0; dx <= 2; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                if (dx == 1 && dy == 1) continue;
                result.drawPixmap(blackSrc, dx, dy);
            }
        }
        result.drawPixmap(src, 1, 1);
        blackSrc.dispose();
        out.dispose();
        return result;
    }

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
        Pixmap pixmap = new Pixmap(52+9*(str.length()),15,Pixmap.Format.RGBA8888);
        pixmap.drawPixmap(score,0,0);
        int div = 1;
        for (int i = 0; i < str.length(); i++) {
            pixmap.drawPixmap(numTextures.get((num/div)%10), 54+9*(str.length()-i-1), 0);
            div *= 10;
        }
        Pixmap outlined = addOutline(pixmap);
        pixmap.dispose();
        Texture output = new Texture(outlined);
        outlined.dispose();
        return output;
    }

    public static Texture MakeTimerTexture(int totalSeconds){
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String minStr = Integer.toString(minutes);
        String secStr = (seconds < 10 ? "0" : "") + seconds;

        int width = 9 * minStr.length() + 3 + 9 * 2;
        Pixmap pixmap = new Pixmap(width, 15, Pixmap.Format.RGBA8888);

        int xPos = 0;
        int div = 1;
        for (int i = 0; i < minStr.length() - 1; i++) div *= 10;
        for (int i = 0; i < minStr.length(); i++) {
            pixmap.drawPixmap(numTextures.get((minutes / div) % 10), xPos, 0);
            xPos += 9;
            div /= 10;
        }

        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fillRectangle(xPos, 4, 1, 1);
        pixmap.fillRectangle(xPos, 10, 1, 1);
        xPos += 3;

        pixmap.drawPixmap(numTextures.get(seconds / 10), xPos, 0);
        xPos += 9;
        pixmap.drawPixmap(numTextures.get(seconds % 10), xPos, 0);

        Pixmap outlined = addOutline(pixmap);
        pixmap.dispose();
        Texture output = new Texture(outlined);
        outlined.dispose();
        return output;
    }

    public static Texture MakeFeatherTimerTexture(int secs){
        if (secs < 0) secs = 0;
        String str = Integer.toString(secs);

        int iconW = featherIcon.getWidth();
        int width = iconW + 3 + 9 * str.length();
        Pixmap pixmap = new Pixmap(width, 15, Pixmap.Format.RGBA8888);

        pixmap.drawPixmap(featherIcon, 0, 0);

        int xPos = iconW + 3;
        int div = 1;
        for (int i = 0; i < str.length() - 1; i++) div *= 10;
        for (int i = 0; i < str.length(); i++) {
            pixmap.drawPixmap(numTextures.get((secs / div) % 10), xPos, 0);
            xPos += 9;
            div /= 10;
        }

        Pixmap outlined = addOutline(pixmap);
        pixmap.dispose();
        Texture output = new Texture(outlined);
        outlined.dispose();
        return output;
    }

    public static Texture MakeWaveTexture(int wave){
        String str = Integer.toString(wave);

        int textWidth = 25 + 9 * str.length();
        int height = 15;
        Pixmap pixmap = new Pixmap(textWidth, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);

        // W
        pixmap.fillRectangle(0, 0, 1, 11);
        pixmap.fillRectangle(1, 9, 1, 2);
        pixmap.fillRectangle(2, 7, 1, 2);
        pixmap.fillRectangle(3, 9, 1, 2);
        pixmap.fillRectangle(3, 7, 1, 2);
        pixmap.fillRectangle(4, 9, 1, 2);
        pixmap.fillRectangle(5, 0, 1, 11);

        // a
        pixmap.fillRectangle(8, 4, 3, 1);
        pixmap.fillRectangle(7, 5, 1, 6);
        pixmap.fillRectangle(11, 4, 1, 7);
        pixmap.fillRectangle(8, 7, 3, 1);
        pixmap.fillRectangle(8, 10, 3, 1);

        // v
        pixmap.fillRectangle(13, 4, 1, 5);
        pixmap.fillRectangle(14, 9, 1, 2);
        pixmap.fillRectangle(15, 10, 1, 1);
        pixmap.fillRectangle(16, 9, 1, 2);
        pixmap.fillRectangle(17, 4, 1, 5);

        // e
        pixmap.fillRectangle(20, 4, 3, 1);
        pixmap.fillRectangle(19, 4, 1, 7);
        pixmap.fillRectangle(20, 7, 3, 1);
        pixmap.fillRectangle(20, 10, 3, 1);

        int xPos = 25;
        int div = 1;
        for (int i = 0; i < str.length() - 1; i++) div *= 10;
        for (int i = 0; i < str.length(); i++) {
            pixmap.drawPixmap(numTextures.get((wave / div) % 10), xPos, 0);
            xPos += 9;
            div /= 10;
        }

        Pixmap outlined = addOutline(pixmap);
        pixmap.dispose();
        Texture output = new Texture(outlined);
        outlined.dispose();
        return output;
    }
}