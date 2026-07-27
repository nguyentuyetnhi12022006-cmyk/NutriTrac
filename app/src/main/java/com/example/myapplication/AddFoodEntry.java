package com.example.myapplication;

public class AddFoodEntry {
    public String name;
    public String icon;
    public String mealType;
    public int gram;
    public int calo;
    public double protein;
    public double carb;
    public double fat;

    public AddFoodEntry(String name, String icon, String mealType, int gram, int calo, double protein, double carb, double fat) {
        this.name = name;
        this.icon = icon;
        this.mealType = mealType;
        this.gram = gram;
        this.calo = calo;
        this.protein = protein;
        this.carb = carb;
        this.fat = fat;
    }
}