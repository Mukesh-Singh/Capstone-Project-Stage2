package com.app.reddit.models;

public class Subreddit {

    private String id;
    private String displayName;
    private boolean isSelected;

    public Subreddit(String id, String name, boolean isSelected) {
        this.id = id;
        this.displayName = name;
        this.isSelected = isSelected;
    }
    public Subreddit(String id, String name, int isSelected) {
        this.id = id;
        this.displayName = name;
        setSelected(isSelected);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return displayName;
    }

    public void setName(String name) {
        this.displayName = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public int isSelectedInt() {
        if (isSelected)
            return 1;
        return 0;

    }

    public void setSelected(int isFavourite){
        if (isFavourite==0)
            setSelected(false);
        else
            setSelected(true);
    }


    public void setSelected(boolean isFavourite) {
        this.isSelected = isFavourite;
    }
}
