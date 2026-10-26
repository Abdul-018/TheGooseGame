package io.github.goose_game;

public class Entity {

    public int health;

    public void collision(Entity entity_1, Entity entity_2){

        if ((entity_1 instanceof Player && entity_2 instanceof Goose) || (entity_1 instanceof Goose && entity_2 instanceof Player)){
            //TODO do something

        }else if (entity_1 instanceof Item){
            ((Item)entity_1).onCollision(entity_2);
        }else if (entity_2 instanceof Item){
            ((Item)entity_2).onCollision(entity_1);
        }

    }


    
}
