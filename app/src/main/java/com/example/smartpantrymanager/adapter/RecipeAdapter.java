package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.Recipe;

import java.util.List;

/**
 * Displays either the strict "Suggested" list or the bonus "Almost There"
 * list, depending on which the calling screen passes in. missingCounts maps
 * recipe id -> number of missing ingredients (0 for a full strict match).
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface Listener {
        void onRecipeClicked(Recipe recipe);
    }

    private final List<Recipe> recipes;
    private final java.util.Map<Long, Integer> missingCounts;
    private final Listener listener;

    public RecipeAdapter(List<Recipe> recipes, java.util.Map<Long, Integer> missingCounts, Listener listener) {
        this.recipes = recipes;
        this.missingCounts = missingCounts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.name.setText(recipe.getName());

        int missing = missingCounts.containsKey(recipe.getId()) ? missingCounts.get(recipe.getId()) : 0;
        if (missing == 0) {
            holder.status.setText(R.string.matched_format);
        } else {
            holder.status.setText(holder.itemView.getContext()
                    .getString(R.string.missing_count_format, missing));
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClicked(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView name, status;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_recipe_name);
            status = itemView.findViewById(R.id.text_recipe_status);
        }
    }
}
