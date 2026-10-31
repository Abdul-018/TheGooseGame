package io.github.goose_game;
import com.badlogic.gdx.math.Rectangle;

public class Wall {
    
    public int xPos;
    public int yPos;
    public int width;
    public int height;
    public Rectangle wall;

    public Wall(int newxPos, int newyPos, int newWidth, int newHeight){
        this.xPos = newxPos;
        this.yPos = newyPos;
        this.width = newWidth;
        this.height = newHeight;

        wall = new Rectangle(xPos, yPos, width, height);

    }

}

Array<Rectangle> walls = new Array<>();

wall1 = new Wall(100,100,200,20); // set these to wherever there's a building/ wall on the game map
wall2 = new Wall(300,200,20,150);
wall3 = new Wall(50,300,200,20);

walls.add(wall1);
walls.add(wall2);
walls.add(wall3);

float oldX = player.getX();
float oldY= player.getY();

for (Rectangle wall : walls){

    if (player.getBoundingRectangle().overlaps(wall)){
        player.setPosition(oldX, oldY);
        break;
    }

}
