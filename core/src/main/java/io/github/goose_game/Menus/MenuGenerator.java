package io.github.goose_game.Menus;

public class MenuGenerator {
    public int loadPos;
    public MenuGenerator(int loadPosInput) {
        loadPos = loadPosInput;
        Methods.menuList.add(this);
    }
    public MenuGenerator() {
        loadPos = 0;
        Methods.menuList.add(this);
    }
    public void LoadObjects(){
        
    }
    public void LoadAssets(){
        
    }
    public int getPri(){
        return loadPos;
    }
}
