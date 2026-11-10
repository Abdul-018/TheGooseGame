package io.github.goose_game.Menus;

import java.util.ArrayList;
import java.util.Comparator;
public class Methods {
    public static ArrayList<MenuGenerator> menuList = new ArrayList<>();
    public static void LoadAllAssets(){
        //forcing classes to load
        MainMenu.mainMenuMethods.getPri();
        PauseMenu.pauseMenuMethods.getPri();
        UIMenu.gameUIMethods.getPri();
        LoseScreenMenu.loseMenuMethods.getPri();
        WinScreenMenu.winMenuMethods.getPri();
        //actual code
        menuList.sort(Comparator.comparing(MenuGenerator::getPri));
        int n = menuList.size();
        System.out.println(n);
        for (int i = 0; i <= n-1; i++) {
            menuList.get(i).LoadAssets();
        }
    }  
    public static void LoadAllObjects(){
        menuList.sort(Comparator.comparing(MenuGenerator::getPri));
        int n = menuList.size();
        System.out.println(n);
        for (int i = 0; i <= n-1; i++) {
            menuList.get(i).LoadObjects();
        }
    }
}
