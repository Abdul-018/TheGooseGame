package io.github.goose_game.Uimenu;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Uielement {
    public enum Align{
        LEFT,
        MIDDLE,
        RIGHT
    }
    public Align align;
    public Sprite sprite;
    public float scale = 1;
    public float imgScale = 1;
    private Texture texture;
    public int pri = 0;
    public float[] pos = {640f,360f};
    public float[] offset;

    public int getPri(){
        return this.pri;
    }

    public Uielement(Texture spriteInput){
        this(spriteInput,new float[]{640f,360f});
    }
    public Uielement(Texture spriteInput, float[] posInput){
        this(spriteInput,posInput,Align.LEFT);
    }
    public Uielement(Texture spriteInput, float[] posInput,Align alignInput){
        this(spriteInput,posInput,alignInput,1f);
    }
    public Uielement(Texture spriteInput, float[] posInput,Align alignInput, float imgScaleInput){
        this(spriteInput,posInput,alignInput,imgScaleInput,1f);
    }
    public Uielement(Texture spriteInput, float[] posInput,Align alignInput, float imgScaleInput, float scaleInput){
        this(spriteInput,posInput,alignInput,imgScaleInput,scaleInput,0);
    }
    public Uielement(Texture spriteInput, float[] posInput,Align alignInput, float imgScaleInput, float scaleInput, int priInput){
        texture = spriteInput;
        align = alignInput;
        sprite = new Sprite(spriteInput);
        sprite.setSize(spriteInput.getWidth(), spriteInput.getHeight());
        pos = posInput;
        if (alignInput == Uielement.Align.MIDDLE){
            pos[0] += Menu.screenWH*360-640;
        }else if (alignInput == Uielement.Align.RIGHT) {
            pos[0] += Menu.screenWH*720-1280;
        }
        imgScale = imgScaleInput;
        scale = scaleInput;
        offset = new float[]{spriteInput.getWidth()/2f,spriteInput.getHeight()/2f};
        pri = priInput;
    }
    
    public void setTexture(Texture spriteInput){
        texture = spriteInput;
        offset = new float[]{spriteInput.getWidth()/2f,spriteInput.getHeight()/2f};
        sprite = new Sprite(spriteInput);
    }

    public void Update(){
    }
    public boolean OnKey(int passedInput){
        return false;
    }
    public boolean ProcessesKey(int passedInput){
        return false;
    }
    public void Draw(SpriteBatch batch){
        float totSelfScale = scale*imgScale;
        float totGlobScale = Menu.globalScale*totSelfScale;
        sprite.setSize(texture.getWidth()*totGlobScale, texture.getHeight()*totGlobScale);
        sprite.setX(Menu.globalScale*pos[0]-totGlobScale*offset[0]);
        sprite.setY(Menu.globalScale*pos[1]-totGlobScale*offset[1]);
        sprite.draw(batch);
        //System.out.println("drawing"+String.valueOf(pos[0])+" "+String.valueOf(pos[1]));
    }
}
