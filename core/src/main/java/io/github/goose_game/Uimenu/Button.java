package io.github.goose_game.Uimenu;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Button extends Uielement{
    float colliderScale = 1;
    public boolean handlesMouse = false;

    public Button(Texture spriteInput, float[] posInput,Align alignInput, float imgScaleInput, float colliderScaleInput, float scaleInput, int priInput){
        super(spriteInput, posInput, alignInput, imgScaleInput, scaleInput, priInput);
        colliderScale = colliderScaleInput;
    }
    public Button(Texture spriteInput, float[] posInput,Align alignInput, float imgScaleInput, float colliderScaleInput, float scaleInput){
        super(spriteInput, posInput, alignInput, imgScaleInput, scaleInput);
        colliderScale = colliderScaleInput;
    }
    public Button(Texture spriteInput, float[] posInput,Align alignInput, float imgScaleInput, float colliderScaleInput){
        super(spriteInput, posInput, alignInput, imgScaleInput);
        colliderScale = colliderScaleInput;
    }
    public Button(Texture spriteInput, float[] posInput,Align alignInput, float imgScaleInput){
        super(spriteInput, posInput, alignInput, imgScaleInput);
    }
    public Button(Texture spriteInput, float[] posInput,Align alignInput){
        super(spriteInput, posInput, alignInput);
    }
    public Button(Texture spriteInput, float[] posInput){
        super(spriteInput, posInput);
    }
    public Button(Texture spriteInput){
        super(spriteInput);
    }

    public boolean PointerOn(Viewport viewInput){
        return PointerOn(Menu.PointerpostoLocal(viewInput));
    }
    public boolean PointerOn(float [] localXY){
        float totSelfScale = scale*colliderScale;
        return (Math.abs(pos[0]-localXY[0])<=offset[0]*totSelfScale && Math.abs(pos[1]-localXY[1])<=offset[1]*totSelfScale);
    }
    public boolean OnClick(){
        System.out.println("clickedbad");
        return false;
    }
    //public boolean ProcessesKey(int passedInput) {
    //    int[] keyList = new int[]{};
    //    for (int e : keyList) {
    //        if (passedInput == e){
    //            System.out.println("key is same");
    //            return this.OnKey(int passedInput);
    //        }else{System.out.println("key is different");}
    //    }
    //    
    //    return false;
    //}
}

