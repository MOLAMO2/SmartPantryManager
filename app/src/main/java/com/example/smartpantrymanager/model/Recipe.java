package com.example.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe: a name, a list of required ingredients, and a method (steps).
 */
public class Recipe {

    private long id;
    private String name;
    private String steps;
    private List<RequiredIngredient> requiredIngredients = new ArrayList<>();

    public Recipe(long id, String name, String steps) {
        this.id = id;
        this.name = name;
        this.steps = steps;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSteps() {
        return steps;
    }

    public List<RequiredIngredient> getRequiredIngredients() {
        return requiredIngredients;
    }

    public void addRequiredIngredient(RequiredIngredient ingredient) {
        requiredIngredients.add(ingredient);
    }
}
