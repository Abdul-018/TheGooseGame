package io.github.goose_game.Uimenu;

import java.util.ArrayList;
import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Menu {
    public enum Passtype {
        NONE,//absorbs all input passing on none
        SOME,
        ALL
    }
    private static ArrayList<Menu> menuStack = new ArrayList<>();
    public ArrayList<Uielement> elementsList;
    public Passtype passType;
    public boolean drawifBehind;

    public static Viewport mostRecentViewport;
    public static int baseHeight = 720;
    public static float globalScale = 1; 
    public static float screenWH = 16f/9f; 
    public static float viewportworldW = 1280;
    public static float viewportworldH = 720;
    private static ArrayList<Menu> menuList = new ArrayList<>();

    public static void Updateviewport(Viewport viewInput){
        float oldLocalWidth = screenWH*720;
        mostRecentViewport = viewInput;
        viewportworldW = viewInput.getWorldWidth();
        viewportworldH = viewInput.getWorldHeight();if (viewportworldH < 1){viewportworldH = 1;}
        globalScale = viewportworldH/(float)baseHeight;
        screenWH = viewportworldW/viewportworldH;
        float newLocalWidth = screenWH*720;
        float deltaLocalWidth = newLocalWidth-oldLocalWidth;
        float deltaLocalWidthHalf = deltaLocalWidth/2;
        for (Menu menu : menuList) {
            for (Uielement element : menu.elementsList) {
                if (element.align == Uielement.Align.MIDDLE){
                    element.pos[0] += deltaLocalWidthHalf;
                }else if (element.align == Uielement.Align.RIGHT) {
                    element.pos[0] += deltaLocalWidth;
                }
            }
        }   
        
    }

    public static boolean Inputall(int passedInput){
        boolean flag = false;
        for (int i = menuStack.size()-1; i >= 0; i += -1) {
            Menu localMenu = menuStack.get(i);
            System.out.println("menu key processed");
            boolean absorbed = localMenu.Inputself(passedInput);
            if ((absorbed && localMenu.passType == Passtype.SOME) || localMenu.passType == Passtype.NONE){
                flag = true;
                break;
            }
        }
        return flag;
    }
    public static boolean Inputall(int[] passedXY, Viewport viewInput){
        return Inputall(passedXY[0], passedXY[1], viewInput);
    }
    public static boolean Inputall(int passedX,int passedY, Viewport viewInput){
        boolean flag = false;
        float[] localXY = PointerpostoLocal(new int[]{passedX,passedY},viewInput);
        for (int i = menuStack.size()-1; i >= 0; i += -1) {
            System.out.println("click!!!2222");
            Menu localMenu = menuStack.get(i);
            boolean absorbed = localMenu.Inputself(localXY);
            if ((absorbed && localMenu.passType == Passtype.SOME) || localMenu.passType == Passtype.NONE){
                flag = true;
                break;
            }
        }
        return flag;
    }
    private boolean Inputself(int passedInput){
        //handle keypress
        boolean flag = false;
        this.elementsList.sort(Comparator.comparing(Uielement::getPri));
        int n = this.elementsList.size();
        for (int i = n-1; i >= 0; i--) {
            Uielement localElement = elementsList.get(i);
            System.out.println("element key checked");
            if (localElement instanceof Button && localElement.ProcessesKey(passedInput)) {
                flag = true;
                System.out.println("element key processed");
                if (localElement.OnKey(passedInput)){
                    break;
                }
            }
        }   
        return flag;
    }
    private boolean Inputself(float[] passedXY){
        //handle click
        boolean flag = false;
        this.elementsList.sort(Comparator.comparing(Uielement::getPri));
        int n = this.elementsList.size();
        for (int i = n-1; i >= 0; i--) {
            System.out.println("click!!!333");
            Uielement localElement = elementsList.get(i);
            //if touching in case of mouse or execute anuways if its a non touching one
            if (localElement instanceof Button && ((Button)localElement).PointerOn(passedXY)) {
                flag = true;
                System.out.println("click!!!444");
                if (((Button)localElement).OnClick()){
                    break;
                }
            }
        }
        return flag;
    }
    public boolean IsAtTop(){
        return (menuStack.size()-1 == menuStack.indexOf(this));
    }
    public static float[] PointerpostoLocal(Viewport viewInput){
        return PointerpostoLocal(new int[]{Gdx.input.getX(),Gdx.input.getY()},viewInput);
    }
    public static float[] PointerpostoLocal(int screenX,int screenY, Viewport viewInput){
        return PointerpostoLocal(new int[]{screenX,screenY},viewInput);
    }
    public static float[] PointerpostoLocal(int[] screenXY, Viewport viewInput) {
        float[] localXY = {(float)screenXY[0]-viewInput.getLeftGutterWidth(),(float)screenXY[1]-viewInput.getTopGutterHeight()};
        float mult = 720f/viewInput.getScreenHeight();
        localXY[0] *= mult;
        localXY[1] *= mult;
        localXY[1] = 720-localXY[1];
        return localXY;
    }

    public static boolean Drawall(SpriteBatch batch){
        boolean flag = false;
        if (menuStack.isEmpty()){}else{
            for (int i = 0; i <= menuStack.size()-1; i += 1) {
                Menu localMenu = menuStack.get(i);
                if (localMenu.drawifBehind || i==menuStack.size()-1){
                    localMenu.Drawself(batch);
                }
            }
        }
        return flag;
    }
    private void Drawself(SpriteBatch batch){
        int n = this.elementsList.size();
        this.elementsList.sort(Comparator.comparing(Uielement::getPri));
        for (int i = 0; i < n; i++) {
            elementsList.get(i).Draw(batch);
        }
    }

    public static boolean Updateall(){
        boolean flag = false;
        if (menuStack.isEmpty()){}else{
            menuStack.get(menuStack.size()-1).Updateself();
            for (int i = menuStack.size()-2; i >= 0; i += -1) {
                Menu localMenu = menuStack.get(i);
                if (localMenu.drawifBehind){
                    localMenu.Updateself();
                }
            }
        }
        return flag;
    }
    private void Updateself(){
        int n = this.elementsList.size();
        for (int i = n-1; i >= 0; i--) {
            elementsList.get(i).Update();
        } 
    }

    public Menu(ArrayList<Uielement> elementsInput,Passtype passInput,boolean drawifbehindInput){
        elementsList = elementsInput;
        passType = passInput;
        drawifBehind = drawifbehindInput;
        menuList.add(this);
    }
    public Menu(ArrayList<Uielement> elementsInput,Passtype passInput){
        this(elementsInput, passInput, true);
    }
    public Menu(ArrayList<Uielement> elementsInput){
        this(elementsInput, Passtype.SOME);
    }

    public boolean IsOpen(){
        return (menuStack.indexOf(this) != -1);
    }



    public void Open(){
        int tempPos;
        tempPos = menuStack.indexOf(this);
        if (tempPos >= 0){
            for (int i = menuStack.size()-1; i > tempPos; i--) {
                Menu address = menuStack.get(i);
                address.Close();
            }
        }else{
            menuStack.add(this);
        }
    } 

    public void Close(){
        int tempPos;
        tempPos = menuStack.indexOf(this);
        if (tempPos >= 0){
            menuStack.remove(tempPos);
        }
    }
}
