package com.example.homechef;

public class Ingredient {

    private String text;
    private Boolean checked;

    public Ingredient(String text, Boolean checked) {
        this.text = text;
        this.checked = checked;
    }

    public Ingredient() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }
}
