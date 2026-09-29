package com.example.smartpantrymanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RequiredIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all persistent storage for the app using SQLite (Section 3.2).
 * Two responsibilities live here:
 *  1. Pantry CRUD (create/read/update/delete pantry_items rows).
 *  2. Read-only access to a recipe catalogue that is seeded once on first run.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    // Pantry table
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_PANTRY_ID = "_id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QTY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    // Recipe table
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "_id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";

    // Recipe ingredient requirement table
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "_id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "ingredient_name";
    public static final String COL_RI_QTY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    // The database has three tables:
    // pantry_items - what the user owns right now (add / edit / delete)
    // recipes - the fixed recipe catalogue, seeded on first run
    // recipe_ingredients - what each recipe needs, linked to recipes by recipe_id
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PANTRY_NAME + " TEXT NOT NULL, " +
                COL_PANTRY_QTY + " REAL NOT NULL, " +
                COL_PANTRY_UNIT + " TEXT NOT NULL, " +
                COL_PANTRY_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT NOT NULL, " +
                COL_RECIPE_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_NAME + " TEXT NOT NULL, " +
                COL_RI_QTY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_RECIPE_ID + "))");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ======================= PANTRY CRUD =======================

    public long addIngredient(Ingredient ingredient) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PANTRY_NAME, ingredient.getName());
        values.put(COL_PANTRY_QTY, ingredient.getQuantity());
        values.put(COL_PANTRY_UNIT, ingredient.getUnit());
        values.put(COL_PANTRY_EXPIRY, ingredient.getExpiryDate());
        return db.insert(TABLE_PANTRY, null, values);
    }

    public int updateIngredient(Ingredient ingredient) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PANTRY_NAME, ingredient.getName());
        values.put(COL_PANTRY_QTY, ingredient.getQuantity());
        values.put(COL_PANTRY_UNIT, ingredient.getUnit());
        values.put(COL_PANTRY_EXPIRY, ingredient.getExpiryDate());
        return db.update(TABLE_PANTRY, values, COL_PANTRY_ID + "=?",
                new String[]{String.valueOf(ingredient.getId())});
    }

    public void deleteIngredient(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PANTRY, COL_PANTRY_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, null, null, null, null,
                COL_PANTRY_NAME + " ASC");
        while (cursor.moveToNext()) {
            list.add(new Ingredient(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_PANTRY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PANTRY_QTY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_UNIT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_EXPIRY))
            ));
        }
        cursor.close();
        return list;
    }

    public Ingredient getIngredientById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, COL_PANTRY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Ingredient ingredient = null;
        if (cursor.moveToFirst()) {
            ingredient = new Ingredient(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_PANTRY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PANTRY_QTY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_UNIT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_EXPIRY))
            );
        }
        cursor.close();
        return ingredient;
    }

    // ======================= RECIPE READ-ONLY ACCESS =======================

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, null, null, null, null,
                COL_RECIPE_NAME + " ASC");
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RECIPE_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_NAME));
            String steps = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_STEPS));
            Recipe recipe = new Recipe(id, name, steps);
            attachRequiredIngredients(db, recipe);
            recipes.add(recipe);
        }
        cursor.close();
        return recipes;
    }

    public Recipe getRecipeById(long recipeId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, COL_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, null);
        Recipe recipe = null;
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RECIPE_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_NAME));
            String steps = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_STEPS));
            recipe = new Recipe(id, name, steps);
            attachRequiredIngredients(db, recipe);
        }
        cursor.close();
        return recipe;
    }

    private void attachRequiredIngredients(SQLiteDatabase db, Recipe recipe) {
        Cursor riCursor = db.query(TABLE_RECIPE_INGREDIENTS, null, COL_RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipe.getId())}, null, null, null);
        while (riCursor.moveToNext()) {
            String name = riCursor.getString(riCursor.getColumnIndexOrThrow(COL_RI_NAME));
            double qty = riCursor.getDouble(riCursor.getColumnIndexOrThrow(COL_RI_QTY));
            String unit = riCursor.getString(riCursor.getColumnIndexOrThrow(COL_RI_UNIT));
            recipe.addRequiredIngredient(new RequiredIngredient(name, qty, unit));
        }
        riCursor.close();
    }

    // ======================= SEED DATA (Section 2.2: 15-20 recipes) =======================

    private void seedRecipes(SQLiteDatabase db) {
        insertRecipe(db, "Tomato Basil Pasta",
                "1. Boil pasta until al dente.\n2. Saute garlic and tomato in olive oil.\n3. Toss pasta with sauce and fresh basil.",
                new Object[][]{{"pasta", 200.0, "g"}, {"tomato", 3.0, "piece"}, {"garlic", 2.0, "piece"}, {"basil", 10.0, "g"}, {"olive oil", 2.0, "tbsp"}});

        insertRecipe(db, "Cheese Omelette",
                "1. Beat eggs with a pinch of salt.\n2. Pour into a hot buttered pan.\n3. Add cheese, fold, and serve.",
                new Object[][]{{"egg", 3.0, "piece"}, {"cheese", 50.0, "g"}, {"butter", 10.0, "g"}, {"salt", 1.0, "tsp"}});

        insertRecipe(db, "Vegetable Stir Fry",
                "1. Heat oil in a wok.\n2. Stir-fry carrot, broccoli and onion until tender-crisp.\n3. Add soy sauce and serve over rice.",
                new Object[][]{{"carrot", 2.0, "piece"}, {"broccoli", 150.0, "g"}, {"onion", 1.0, "piece"}, {"soy sauce", 2.0, "tbsp"}, {"rice", 200.0, "g"}});

        insertRecipe(db, "Chicken Sandwich",
                "1. Grill the chicken breast.\n2. Assemble with lettuce and tomato between bread slices.",
                new Object[][]{{"chicken breast", 1.0, "piece"}, {"bread", 2.0, "piece"}, {"lettuce", 20.0, "g"}, {"tomato", 1.0, "piece"}});

        insertRecipe(db, "Potato Soup",
                "1. Boil potato and onion in stock until soft.\n2. Blend until smooth.\n3. Stir in milk and season.",
                new Object[][]{{"potato", 4.0, "piece"}, {"onion", 1.0, "piece"}, {"milk", 250.0, "ml"}, {"salt", 1.0, "tsp"}});

        insertRecipe(db, "Fried Rice",
                "1. Scramble egg in a hot pan.\n2. Add cooked rice, peas and soy sauce.\n3. Stir-fry until heated through.",
                new Object[][]{{"rice", 300.0, "g"}, {"egg", 2.0, "piece"}, {"pea", 80.0, "g"}, {"soy sauce", 2.0, "tbsp"}});

        insertRecipe(db, "Banana Pancakes",
                "1. Mash banana and mix with flour, egg and milk.\n2. Cook spoonfuls on a hot griddle until golden.",
                new Object[][]{{"banana", 2.0, "piece"}, {"flour", 150.0, "g"}, {"egg", 1.0, "piece"}, {"milk", 100.0, "ml"}});

        insertRecipe(db, "Greek Salad",
                "1. Chop tomato, cucumber and onion.\n2. Toss with olives, feta cheese and olive oil.",
                new Object[][]{{"tomato", 2.0, "piece"}, {"cucumber", 1.0, "piece"}, {"onion", 0.5, "piece"}, {"feta cheese", 100.0, "g"}, {"olive oil", 1.0, "tbsp"}});

        insertRecipe(db, "Beef Tacos",
                "1. Brown the beef mince with spices.\n2. Fill taco shells with beef, lettuce and cheese.",
                new Object[][]{{"beef mince", 300.0, "g"}, {"taco shell", 4.0, "piece"}, {"lettuce", 30.0, "g"}, {"cheese", 50.0, "g"}});

        insertRecipe(db, "Mushroom Risotto",
                "1. Saute mushroom and onion in butter.\n2. Gradually stir in rice and stock until creamy.",
                new Object[][]{{"mushroom", 200.0, "g"}, {"onion", 1.0, "piece"}, {"rice", 250.0, "g"}, {"butter", 20.0, "g"}});

        insertRecipe(db, "Lentil Curry",
                "1. Saute onion, garlic and spices.\n2. Add lentil and simmer in water until soft.",
                new Object[][]{{"lentil", 200.0, "g"}, {"onion", 1.0, "piece"}, {"garlic", 2.0, "piece"}, {"curry powder", 1.0, "tbsp"}});

        insertRecipe(db, "Egg Fried Noodles",
                "1. Scramble egg, set aside.\n2. Stir-fry noodles with carrot and soy sauce, then fold egg back in.",
                new Object[][]{{"noodles", 200.0, "g"}, {"egg", 2.0, "piece"}, {"carrot", 1.0, "piece"}, {"soy sauce", 2.0, "tbsp"}});

        insertRecipe(db, "Caprese Sandwich",
                "1. Layer tomato, mozzarella and basil between bread slices.\n2. Drizzle with olive oil.",
                new Object[][]{{"tomato", 1.0, "piece"}, {"mozzarella", 80.0, "g"}, {"basil", 5.0, "g"}, {"bread", 2.0, "piece"}, {"olive oil", 1.0, "tbsp"}});

        insertRecipe(db, "Pumpkin Soup",
                "1. Simmer pumpkin and onion in stock until soft.\n2. Blend smooth and stir in cream.",
                new Object[][]{{"pumpkin", 500.0, "g"}, {"onion", 1.0, "piece"}, {"cream", 100.0, "ml"}});

        insertRecipe(db, "Garlic Butter Rice",
                "1. Saute garlic in butter.\n2. Stir through cooked rice and season with salt.",
                new Object[][]{{"rice", 250.0, "g"}, {"garlic", 3.0, "piece"}, {"butter", 30.0, "g"}, {"salt", 1.0, "tsp"}});

        insertRecipe(db, "Apple Cinnamon Oatmeal",
                "1. Cook oats in milk.\n2. Stir in chopped apple and cinnamon.",
                new Object[][]{{"oats", 100.0, "g"}, {"apple", 1.0, "piece"}, {"milk", 200.0, "ml"}, {"cinnamon", 1.0, "tsp"}});

        insertRecipe(db, "Broccoli Cheddar Bake",
                "1. Steam broccoli until tender.\n2. Top with cheese and bake until melted.",
                new Object[][]{{"broccoli", 300.0, "g"}, {"cheese", 100.0, "g"}, {"butter", 10.0, "g"}});

        insertRecipe(db, "Onion Garlic Soup",
                "1. Caramelise onion and garlic in butter.\n2. Add stock and simmer for 20 minutes.",
                new Object[][]{{"onion", 3.0, "piece"}, {"garlic", 3.0, "piece"}, {"butter", 20.0, "g"}});

        insertRecipe(db, "Carrot and Pea Rice",
                "1. Steam carrot and pea until tender.\n2. Mix through warm cooked rice with butter.",
                new Object[][]{{"carrot", 2.0, "piece"}, {"pea", 100.0, "g"}, {"rice", 250.0, "g"}, {"butter", 15.0, "g"}});

        insertRecipe(db, "Simple Cucumber Salad",
                "1. Slice cucumber and onion thinly.\n2. Toss with olive oil and salt.",
                new Object[][]{{"cucumber", 2.0, "piece"}, {"onion", 0.5, "piece"}, {"olive oil", 1.0, "tbsp"}, {"salt", 1.0, "tsp"}});
    }

    private void insertRecipe(SQLiteDatabase db, String name, String steps, Object[][] ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_RECIPE_NAME, name);
        recipeValues.put(COL_RECIPE_STEPS, steps);
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        for (Object[] ingredient : ingredients) {
            ContentValues riValues = new ContentValues();
            riValues.put(COL_RI_RECIPE_ID, recipeId);
            riValues.put(COL_RI_NAME, (String) ingredient[0]);
            riValues.put(COL_RI_QTY, (Double) ingredient[1]);
            riValues.put(COL_RI_UNIT, (String) ingredient[2]);
            db.insert(TABLE_RECIPE_INGREDIENTS, null, riValues);
        }
    }
}
