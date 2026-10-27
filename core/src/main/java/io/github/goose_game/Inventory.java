package io.github.goose_game;

import java.util.ArrayList;

public class Inventory {
    private final ArrayList<Item> items = new ArrayList<>();
    private int currItemIndex = 0;

    public void selectNextItem() {
        if (!items.isEmpty()) {
            if (currItemIndex == items.size() - 1) {
                currItemIndex = 0;
            } else {
                currItemIndex++;
            }
        }
    }

    public void selectPrevItem() {
        if (!items.isEmpty()) {
            if (currItemIndex == 0) {
                currItemIndex = items.size() - 1;
            } else {
                currItemIndex--;
            }
        }
    }

    public Item getSelected() {
        if (items.isEmpty()) {
            return null;
        }
        return items.get(currItemIndex);
    }

    public void addItem(Item item) {
        items.add(item);
    };

    public void removeItem(Item item) {
        items.remove(item);
        if (currItemIndex >= items.size()) {
            currItemIndex = Math.max(0, items.size() - 1);
        }
    }

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
