package io.github.goose_game.Menus;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;

import io.github.goose_game.Game;
import io.github.goose_game.Uimenu.Button;
import io.github.goose_game.Uimenu.Menu;
import io.github.goose_game.Uimenu.Uielement;
public class MainMenu {
    public static Menu mainMenu;
    static Texture mainmenuBG;
    static Texture mainmenuBGLeft;
    static Texture mainmenuBGRight;
    static Texture mainmenuButton;
    static Texture mainmenuButtonDown;
    static Texture mainmenuGear;
    static Texture mainmenuGearDown;
    static ArrayList<Uielement> mainMenuElements;
    public static MenuGenerator mainMenuMethods = new MenuGenerator(100){
        @Override 
        public void LoadAssets(){
            mainmenuBG =  new Texture("mainmenu_background.png");
            mainmenuBGLeft =  new Texture("mainmenu_background_left.png");
            mainmenuBGRight =  new Texture("mainmenu_background_right.png");
            mainmenuButton =  new Texture("mainmenu_button.png");
            mainmenuButtonDown =  new Texture("mainmenu_button_down.png");
            mainmenuGear =  new Texture("mainmenu_gear.png");
            mainmenuGearDown =  new Texture("mainmenu_gear_down.png");
        }
        @Override 
        public void LoadObjects(){
            mainMenuElements = new ArrayList<>();
            mainMenuElements.add(new Uielement(mainmenuBG,new float[]{640,360},Uielement.Align.MIDDLE,1f,4f));
            mainMenuElements.add(new Uielement(mainmenuBGLeft,new float[]{8,360},Uielement.Align.LEFT,1f,4f,1));
            mainMenuElements.add(new Uielement(mainmenuBGRight,new float[]{1272,360},Uielement.Align.RIGHT,1f,4f,1));
            mainMenu = new Menu(mainMenuElements,Menu.Passtype.NONE,true);
            mainMenu.Open();
            mainMenu.elementsList.add(new Button(mainmenuButton,new float[]{640,214},Uielement.Align.MIDDLE,1f,1f,4f,3){
                @Override 
                public void Update(){
                    //if (this.PointerOn(Button.PointerpostoLocal(Gdx.input.getX(), Gdx.input.getY(), viewport))){
                    if (this.PointerOn(Menu.mostRecentViewport)&&Gdx.input.isTouched()){
                        this.setTexture(mainmenuButtonDown);
                    }else{
                        this.setTexture(mainmenuButton);
                    }
                }
                @Override 
                public boolean ProcessesKey(int passedInput){
                    ArrayList<Integer> handledKeys = new ArrayList<>(Input.Keys.M);
                    for (int e : handledKeys) {
                        if (passedInput == e){
                            System.out.println("key is same");
                            return true;
                        }else{System.out.println("key is different");}
                    }
                    return false;
                }
                @Override 
                public boolean OnClick(){
                    mainMenu.Close();
                    //replace with game start function
                    if (Game.gameOver)Game.currentPlayer.setHealth(10);
                    Game.gameOver = false;
                    //replace with game start function
                    System.out.println("clicked");
                    return true;
                }
            });
            mainMenu.elementsList.add(new Button(mainmenuGear,new float[]{1192,96},Uielement.Align.RIGHT,1f,1f,4f,2){
                @Override 
                public void Update(){
                    //if (this.PointerOn(Button.PointerpostoLocal(Gdx.input.getX(), Gdx.input.getY(), viewport))){
                    if (this.PointerOn(Menu.mostRecentViewport)&&Gdx.input.isTouched()){//this.PointerOn(viewport)&&Gdx.input.isTouched()){
                        this.setTexture(mainmenuGearDown);
                    }else{
                        this.setTexture(mainmenuGear);
                    }
                }
                @Override 
                public boolean OnClick(){
                    //open settings menu
                    return true;
                }
            });
        }
    };
}
