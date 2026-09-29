package com.example.smartpantrymanager.util;

import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RequiredIngredient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the "strict-matching rule" described in the assignment brief
 * (Section 2.3): a recipe only counts as "suggested" if EVERY required
 * ingredient is present in the pantry in at least the required quantity.
 *
 * To avoid a naive exact-string match that breaks on trivial differences
 * (e.g. "tomato" vs "tomatoes"), ingredient names are normalised before
 * comparison, and compatible units (weight / volume) are converted to a
 * common base unit before quantities are compared.
 */
public class IngredientMatcher {

    // --- Unit conversion tables -------------------------------------------------
    // Everything is converted to a base unit per "family" so that, e.g.,
    // a pantry entry of "1 kg" correctly satisfies a recipe that needs "500 g".
    private static final Map<String, Double> WEIGHT_TO_GRAMS = new HashMap<>();
    private static final Map<String, Double> VOLUME_TO_ML = new HashMap<>();
    // Units that cannot be meaningfully converted (counted items).
    private static final String COUNT_UNIT = "piece";

    static {
        WEIGHT_TO_GRAMS.put("g", 1.0);
        WEIGHT_TO_GRAMS.put("kg", 1000.0);
        WEIGHT_TO_GRAMS.put("oz", 28.3495);
        WEIGHT_TO_GRAMS.put("lb", 453.592);

        VOLUME_TO_ML.put("ml", 1.0);
        VOLUME_TO_ML.put("l", 1000.0);
        VOLUME_TO_ML.put("tsp", 4.9289);
        VOLUME_TO_ML.put("tbsp", 14.7868);
        VOLUME_TO_ML.put("cup", 236.588);
        VOLUME_TO_ML.put("fl oz", 29.5735);
    }

    /**
     * Normalises an ingredient name so that simple real-world messiness
     * (case, whitespace, and common plurals) does not break matching.
     * This is a lightweight heuristic, not a full NLP solution, as permitted
     * by the brief.
     */
    public static String normalizeName(String rawName) {
        if (rawName == null) return "";
        String name = rawName.trim().toLowerCase();
        // Collapse extra internal whitespace.
        name = name.replaceAll("\\s+", " ");
        return singularize(name);
    }

    private static String singularize(String word) {
        if (word.endsWith("ies") && word.length() > 3) {
            // "berries" -> "berry"
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("oes") && word.length() > 3) {
            // "tomatoes" -> "tomato", "potatoes" -> "potato"
            return word.substring(0, word.length() - 2);
        }
        if ((word.endsWith("shes") || word.endsWith("ches") || word.endsWith("xes") || word.endsWith("ses"))
                && word.length() > 4) {
            // "dishes" -> "dish", "boxes" -> "box"
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("s") && !word.endsWith("ss") && word.length() > 2) {
            // "eggs" -> "egg", but keep "grass" as-is
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    /**
     * Converts a quantity+unit pair to a common base unit where possible.
     * Returns null if the unit is not convertible (e.g. "piece"), in which
     * case the caller must compare raw quantities of the same unit type.
     */
    private static Double toBaseUnit(double quantity, String unit) {
        if (unit == null) return null;
        String u = unit.trim().toLowerCase();
        if (WEIGHT_TO_GRAMS.containsKey(u)) {
            return quantity * WEIGHT_TO_GRAMS.get(u);
        }
        if (VOLUME_TO_ML.containsKey(u)) {
            return quantity * VOLUME_TO_ML.get(u);
        }
        return null; // count-based unit like "piece", "clove", "unit"
    }

    private static boolean isWeightUnit(String unit) {
        return unit != null && WEIGHT_TO_GRAMS.containsKey(unit.trim().toLowerCase());
    }

    private static boolean isVolumeUnit(String unit) {
        return unit != null && VOLUME_TO_ML.containsKey(unit.trim().toLowerCase());
    }

    /**
     * Determines whether the pantry has ENOUGH of a single required ingredient.
     */
    public static boolean pantryCoversRequirement(List<Ingredient> pantry, RequiredIngredient requirement) {
        String requiredNameNorm = normalizeName(requirement.getName());

        // A single required ingredient could be split across multiple pantry
        // rows (e.g. two separate "onion" entries), so we sum matching stock.
        double totalAvailableInRequirementUnits = 0.0;
        boolean foundAnyNameMatch = false;

        for (Ingredient pantryItem : pantry) {
            if (!normalizeName(pantryItem.getName()).equals(requiredNameNorm)) {
                continue;
            }
            foundAnyNameMatch = true;

            Double pantryBase = toBaseUnit(pantryItem.getQuantity(), pantryItem.getUnit());
            Double requiredBase = toBaseUnit(requirement.getQuantity(), requirement.getUnit());

            boolean sameFamily =
                    (isWeightUnit(pantryItem.getUnit()) && isWeightUnit(requirement.getUnit())) ||
                    (isVolumeUnit(pantryItem.getUnit()) && isVolumeUnit(requirement.getUnit()));

            if (pantryBase != null && requiredBase != null && sameFamily) {
                // Both convertible and same family (weight-weight or volume-volume):
                // accumulate in the base unit, compare later.
                totalAvailableInRequirementUnits += pantryBase; // base unit (g or ml)
            } else {
                // Count-based units, or mismatched families (weight vs volume) which
                // cannot be reliably converted without ingredient density -- fall back
                // to comparing raw quantities directly. This is a documented limitation.
                totalAvailableInRequirementUnits += pantryItem.getQuantity();
            }
        }

        if (!foundAnyNameMatch) {
            return false;
        }

        Double requiredBase = toBaseUnit(requirement.getQuantity(), requirement.getUnit());
        double requiredAmount = (requiredBase != null && isWeightUnitOrVolume(requirement.getUnit()))
                ? requiredBase
                : requirement.getQuantity();

        // If we accumulated in base units but requirement wasn't converted (shouldn't
        // normally happen given the branch above), fall back safely to raw comparison.
        return totalAvailableInRequirementUnits + 1e-6 >= requiredAmount;
    }

    private static boolean isWeightUnitOrVolume(String unit) {
        return isWeightUnit(unit) || isVolumeUnit(unit);
    }

    /**
     * Returns true only if the pantry satisfies EVERY required ingredient of the
     * recipe (the strict-matching rule). This is the exact rule the brief tests.
     */
    public static boolean canMakeStrictly(Recipe recipe, List<Ingredient> pantry) {
        for (RequiredIngredient requirement : recipe.getRequiredIngredients()) {
            if (!pantryCoversRequirement(pantry, requirement)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Counts how many required ingredients are missing from the pantry.
     * Used for the optional "Almost There" bonus list (Section 8): recipes
     * missing exactly one ingredient.
     */
    public static int countMissingIngredients(Recipe recipe, List<Ingredient> pantry) {
        int missing = 0;
        for (RequiredIngredient requirement : recipe.getRequiredIngredients()) {
            if (!pantryCoversRequirement(pantry, requirement)) {
                missing++;
            }
        }
        return missing;
    }
}
