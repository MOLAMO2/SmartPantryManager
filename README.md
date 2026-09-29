# Smart Pantry Manager

An Android app (Java) that helps reduce food waste by tracking pantry ingredients
and suggesting **only** the recipes a user can make right now — no shopping trip
required. Built for Mobile App Development 700.

## Why SQLite?

This app uses **SQLite via `SQLiteOpenHelper`** for local, on-device persistence:

- No network/backend dependency — the strict-matching feature needs to work
  fully offline against the user's own pantry.
- The data model is small and relational (pantry items; recipes; recipe
  ingredient requirements), which maps cleanly onto SQL tables and a `JOIN`-style
  query pattern.
- Easy to seed a fixed recipe catalogue on first run via `onCreate()`.

*(Update this section with your own reasoning if you choose Firebase or
PostgreSQL instead — see Section 3.2 of the brief.)*

## Features

- Pantry CRUD: add, edit, delete ingredients (name, quantity, unit, optional expiry).
- 20 seeded recipes, each with required ingredients and a method.
- **Strict-matching suggestions**: a recipe only appears in "Suggested" if the
  pantry covers every required ingredient in at least the required quantity.
  See `util/IngredientMatcher.java`.
- Optional "Almost There" tab: recipes missing exactly one ingredient.
- Settings screen: expiring-soon alert toggle, metric/imperial preference.
- Bottom navigation between Pantry / Recipes / Settings, wired with Intents.

## Project Structure

```
app/src/main/java/com/example/smartpantrymanager/
├── PantryListActivity.java          # Screen 1 — pantry list + CRUD entry point
├── AddEditIngredientActivity.java   # Screen 2 — add/edit form with validation
├── SuggestedRecipesActivity.java    # Screen 3 — strict-match + almost-there tabs
├── RecipeDetailActivity.java        # Screen 4 — full recipe view
├── SettingsActivity.java            # Screen 5 — preferences
├── BaseActivity.java                # shared bottom-navigation wiring
├── model/                           # Ingredient, Recipe, RequiredIngredient
├── db/DatabaseHelper.java           # SQLite schema, CRUD, seed data
├── util/IngredientMatcher.java      # the strict-matching algorithm
└── adapter/                         # PantryAdapter, RecipeAdapter (RecyclerView)
```

## Setup / Run Instructions

1. Open Android Studio (Hedgehog or later recommended).
2. **File > Open** and select this project folder. The Gradle wrapper
   (`gradlew`, `gradlew.bat`, `gradle/wrapper/`) is already included, so
   Android Studio will download Gradle 8.4 and sync automatically.
3. Run on an emulator or physical device with API 21+.
4. On first launch the app seeds its recipe catalogue automatically — no setup
   needed.

## Known Limitation

Unit matching converts between compatible weight units (g/kg/oz/lb) and
compatible volume units (ml/l/tsp/tbsp/cup) automatically. Matching quantities
across families (e.g. grams of flour vs. cups of flour) would require
ingredient-specific density data and is out of scope; in that edge case the
matcher falls back to comparing raw numbers.

## Database Choice Justification

(Write 2–3 sentences here in your own words for the report / README, e.g. why
SQLite fit this assignment's offline, single-user use case better than Firebase
or PostgreSQL for you specifically.)
