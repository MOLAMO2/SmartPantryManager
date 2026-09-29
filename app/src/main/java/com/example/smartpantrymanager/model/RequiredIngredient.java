package com.example.smartpantrymanager.model;

/**
 * One ingredient requirement belonging to a Recipe
 * (e.g. "2 pieces of tomato").
 */
public class RequiredIngredient {

    private String name;
    private double quantity;
    private String unit;

    public RequiredIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }
}
