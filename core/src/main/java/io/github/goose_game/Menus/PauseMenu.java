package io.github.goose_game.Menus;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;

import io.github.goose_game.Uimenu.Button;
import io.github.goose_game.Uimenu.Menu;
import io.github.goose_game.Uimenu.Uielement;
public class PauseMenu {
    public static Menu pauseMenu;
    static Texture pausemenuBG;
    static Texture pausemenuText;
    static Texture pausemenuMainMenu;
    static Texture pausemenuMainMenuDown;
    static Texture pausemenuResume;
    static Texture pausemenuResumeDown;
    static ArrayList<Uielement> pauseMenuElements;
    public static MenuGenerator pauseMenuMethods = new MenuGenerator(101){
        @Override
        public void LoadAssets(){
            pausemenuBG =  new Texture("menus/pausemenu_background.png");
            pausemenuText =  new Texture("menus/pausemenu_text.png");
            pausemenuMainMenu = new Texture("menus/pausemenu_mainmenu.png");
            pausemenuMainMenuDown = new Texture("menus/pausemenu_mainmenu_down.png");
            pausemenuResume = new Texture("menus/pausemenu_resume.png");
            pausemenuResumeDown = new Texture("menus/pausemenu_resume_down.png");
        }
        @Override
        public void LoadObjects(){
            pauseMenuElements = new ArrayList<>();
            pauseMenuElements.add(new Uielement(pausemenuBG,new float[]{640,360},Uielement.Align.MIDDLE,1f,4f));
            pauseMenuElements.add(new Uielement(pausemenuText,new float[]{640,552},Uielement.Align.MIDDLE,1f,4f));
            pauseMenu = new Menu(pauseMenuElements,Menu.Passtype.NONE,true);
            pauseMenu.elementsList.add(new Button(pausemenuMainMenu,new float[]{640,270},Uielement.Align.MIDDLE,1f,1f,4f,3){
                @Override
                public void Update(){
                    if (this.PointerOn(Menu.mostRecentViewport)&&Gdx.input.isTouched()){
                        this.setTexture(pausemenuMainMenuDown);
                    }else{
                        this.setTexture(pausemenuMainMenu);
                    }
                }
                @Override
                public boolean ProcessesKey(int passedInput){
                    int[] keyList = new int[]{Input.Keys.M};
                    for (int e : keyList) {
                        if (e == passedInput){
                            return this.OnKey(passedInput);
                        }
                    }
                    return false;
                }
                @Override
                public boolean OnKey(int passedInput) {
                    if (passedInput == Input.Keys.M){
                        pauseMenu.Close();
                        MainMenu.mainMenu.Open();
                        return true;
                    }
                    return false;
                }
                @Override
                public boolean OnClick(){
                    pauseMenu.Close();
                    MainMenu.mainMenu.Open();
                    return true;
                }
            });
            pauseMenu.elementsList.add(new Button(pausemenuResume,new float[]{640,126},Uielement.Align.MIDDLE,1f,1f,4f,1){
                @Override
                public void Update(){
                    if (this.PointerOn(Menu.mostRecentViewport)&&Gdx.input.isTouched()){
                        this.setTexture(pausemenuResumeDown);
                    }else{
                        this.setTexture(pausemenuResume);
                    }
                }
                @Override
                public boolean ProcessesKey(int passedInput){
                    int[] keyList = new int[]{Input.Keys.M};
                    for (int e : keyList) {
                        if (e == passedInput){
                            return this.OnKey(passedInput);
                        }
                    }
                    return false;
                }
                @Override
                public boolean OnKey(int passedInput) {
                    if (passedInput == Input.Keys.M){
                        pauseMenu.Close();
                        return true;
                    }
                    return false;
                }
                @Override
                public boolean OnClick(){
                    pauseMenu.Close();
                    return true;
                }
            });

        }
    };
}
