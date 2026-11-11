package io.github.goose_game.Menus;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import io.github.goose_game.Game;
import io.github.goose_game.Uimenu.Menu;
import io.github.goose_game.Uimenu.Uielement;
public class UIMenu {
    public static Menu gameUI;
    static Texture healthBook;
    //static Texture healthBookTorn1;
    //static Texture healthBookTorn2;
    static Texture healthBookTorn1Darkened;
    static Texture healthBookTorn2Darkened;
    //static Texture healthBookClosed;
    static Texture empty;
    static ArrayList<Pixmap> numTextures;
    public static int scoreNum = 0;
    //static int healthy = 695;
    static int healthy = 62;
    private static int lastScoreUpdate = 0;
    static Pixmap score;
    static ArrayList<Uielement> gameUIElements;
    public static MenuGenerator gameUIMethods = new MenuGenerator(0){
        @Override 
        public void LoadAssets(){
            numTextures = new ArrayList<>();
            Pixmap numSheet = new Pixmap(Gdx.files.internal("uimenu_nums.png"));
            for (int i = 0; i < 10; i++) {
                Pixmap pixmap = new Pixmap( 7, 15, Pixmap.Format.RGBA8888 );
                pixmap.drawPixmap(numSheet, -9*i, 0);
                numTextures.add(pixmap);
                //pixmap.dispose();
            }
            numSheet.dispose();
            healthBook =  new Texture("health_book.png");
            score = new Pixmap(Gdx.files.internal("uimenu_score.png"));
            //healthBookTorn1 =  new Texture("health_book_torn1.png");
            //healthBookTorn2 =  new Texture("health_book_torn2.png");
            healthBookTorn1Darkened =  new Texture("health_book_torn1_darkened.png");
            healthBookTorn2Darkened =  new Texture("health_book_torn2_darkened.png");
            //healthBookClosed =  new Texture("health_book_closed.png");
            empty = new Texture("empty.png");
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
            gameUIElements.add(new Uielement(healthBook,new float[]{1206,healthy},Uielement.Align.RIGHT,1f,4f,1){
                @Override 
                public void Update(){
                    int health = Game.currentPlayer.getHealth()-0;
                    //if (health > 1){
                    //    if (health > 2){
                    //        this.setTexture(healthBook);
                    //    }
                    //    else{
                    //        this.setTexture(healthBookTorn1);
                    //    }
                    //}else if(health>0){
                    //    this.setTexture(healthBookTorn2);
                    //}else{
                    //    this.setTexture(healthBookClosed);
                    //}
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

    public static Texture MakeScoreTexture(int num){
        String str = Integer.toString(num);
        Pixmap pixmap = new Pixmap(52+9*(str.length()),15,Pixmap.Format.RGBA8888);
        pixmap.drawPixmap(score,0,0);
        int div = 1;
        for (int i = 0; i < str.length(); i++) {
            pixmap.drawPixmap(numTextures.get((scoreNum/div)%10), 54+9*(str.length()-i-1), 0);
            div *= 10;
        }
        Texture output = new Texture(pixmap);
        pixmap.dispose();
        return output;
    }
}
