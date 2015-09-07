package com.shenkar.aroundme;

/**
 * setting item class with a name and an image to represent the setting
 */
public class SettingItem {
    private String name;
    private int image;

    public SettingItem(String name, int image) {
        this.name = name;
        this.image = image;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


