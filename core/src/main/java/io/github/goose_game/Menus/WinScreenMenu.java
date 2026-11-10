package io.github.goose_game.Menus;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import io.github.goose_game.Uimenu.Button;
import io.github.goose_game.Uimenu.Menu;
import io.github.goose_game.Uimenu.Uielement;
public class WinScreenMenu {
    public static Menu winMenu;
    static Texture winmenuBG;
    static Texture winmenuMainMenu;
    static Texture winmenuMainMenuDown;
    static ArrayList<Uielement> winMenuElements;
    public static MenuGenerator winMenuMethods = new MenuGenerator(102){
        @Override 
        public void LoadAssets(){
            winmenuBG =  new Texture("gamewinscreen.png");
            winmenuMainMenu = new Texture("pausemenu_mainmenu.png");
            winmenuMainMenuDown = new Texture("pausemenu_mainmenu_down.png");
        }
        @Override 
        public void LoadObjects(){
            winMenuElements = new ArrayList<>();
            winMenuElements.add(new Uielement(winmenuBG,new float[]{640,360},Uielement.Align.MIDDLE,1f,4f));
            winMenu = new Menu(winMenuElements,Menu.Passtype.NONE,true);
            winMenu.elementsList.add(new Button(winmenuMainMenu,new float[]{640,118},Uielement.Align.MIDDLE,1f,1f,4f,3){
                @Override 
                public void Update(){
                    //if (this.PointerOn(Button.PointerpostoLocal(Gdx.input.getX(), Gdx.input.getY(), Menu.mostRecentViewport))){
                    if (this.PointerOn(Menu.mostRecentViewport)&&Gdx.input.isTouched()){
                        this.setTexture(winmenuMainMenuDown);
                    }else{
                        this.setTexture(winmenuMainMenu);
                    }
                }
                @Override 
                public boolean OnClick(){
                    winMenu.Close();
                    PauseMenu.pauseMenu.Close();
                    MainMenu.mainMenu.Open();
                    System.out.println("clicked");
                    return true;
                }
            });
            winMenu.elementsList.add(new Uielement(UIMenu.MakeScoreTexture(0),new float[]{640,442},Uielement.Align.MIDDLE,1f,4f,1){
                @Override 
                public void Update(){
                    this.setTexture(UIMenu.MakeScoreTexture(io.github.goose_game.Game.currentPlayer.getScore()));
                }
            });

        }
    };
}