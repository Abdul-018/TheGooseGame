package io.github.goose_game;

import java.util.ArrayList;

public class Inventory {
    private final ArrayList<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        items.add(item);
    };

    public void removeItem(Item item) {
        items.remove(item);
    };

    public ArrayList<Item> getItems() {
        return items;
    };

    public int numberofitems() {
        return items.size();
    }

    public void PrintItems() {
        for (Item item : this.items) {
            System.out.println(
                    "ID: " + item.getId() +
                            " | Name: " + item.getType() +
                            " | Position: " + item.getPosition());
        }
    }
}
