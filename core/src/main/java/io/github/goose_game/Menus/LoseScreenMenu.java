package io.github.goose_game.Menus;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import io.github.goose_game.Uimenu.Button;
import io.github.goose_game.Uimenu.Menu;
import io.github.goose_game.Uimenu.Uielement;
public class LoseScreenMenu {
    public static Menu loseMenu;
    static Texture losemenuBG;
    static Texture losemenuMainMenu;
    static Texture losemenuMainMenuDown;
    static ArrayList<Uielement> loseMenuElements;
    public static MenuGenerator loseMenuMethods = new MenuGenerator(102){
        @Override 
        public void LoadAssets(){
            losemenuBG =  new Texture("gameoverscreen.png");
            losemenuMainMenu = new Texture("pausemenu_mainmenu.png");
            losemenuMainMenuDown = new Texture("pausemenu_mainmenu_down.png");
        }
        @Override 
        public void LoadObjects(){
            loseMenuElements = new ArrayList<>();
            loseMenuElements.add(new Uielement(losemenuBG,new float[]{640,360},Uielement.Align.MIDDLE,1f,4f));
            loseMenu = new Menu(loseMenuElements,Menu.Passtype.NONE,true);
            loseMenu.elementsList.add(new Button(losemenuMainMenu,new float[]{640,118},Uielement.Align.MIDDLE,1f,1f,4f,3){
                @Override 
                public void Update(){
                    //if (this.PointerOn(Button.PointerpostoLocal(Gdx.input.getX(), Gdx.input.getY(), Menu.mostRecentViewport))){
                    if (this.PointerOn(Menu.mostRecentViewport)&&Gdx.input.isTouched()){
                        this.setTexture(losemenuMainMenuDown);
                    }else{
                        this.setTexture(losemenuMainMenu);
                    }
                }
                @Override 
                public boolean OnClick(){
                    loseMenu.Close();
                    PauseMenu.pauseMenu.Close();
                    MainMenu.mainMenu.Open();
                    System.out.println("clicked");
                    return true;
                }
            });

        }
    };
}
