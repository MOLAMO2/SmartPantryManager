package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.Ingredient;

import java.util.List;

/**
 * Binds a list of Ingredient objects (loaded from SQLite) to the pantry
 * RecyclerView (Section 3.1: "at least one RecyclerView ... with a custom Adapter").
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    public interface Listener {
        void onEditClicked(Ingredient ingredient);
        void onDeleteClicked(Ingredient ingredient);
    }

    private final List<Ingredient> items;
    private final Listener listener;

    public PantryAdapter(List<Ingredient> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        Ingredient ingredient = items.get(position);
        holder.name.setText(ingredient.getName());

        String qtyText = formatQuantity(ingredient.getQuantity()) + " " + ingredient.getUnit();
        holder.quantity.setText(qtyText);

        if (ingredient.getExpiryDate() != null && !ingredient.getExpiryDate().isEmpty()) {
            holder.expiry.setVisibility(View.VISIBLE);
            holder.expiry.setText("Expires: " + ingredient.getExpiryDate());
        } else {
            holder.expiry.setVisibility(View.GONE);
        }

        holder.editButton.setOnClickListener(v -> listener.onEditClicked(ingredient));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClicked(ingredient));
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((int) quantity);
        }
        return String.valueOf(quantity);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView name, quantity, expiry;
        ImageButton editButton, deleteButton;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_ingredient_name);
            quantity = itemView.findViewById(R.id.text_ingredient_qty);
            expiry = itemView.findViewById(R.id.text_ingredient_expiry);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}
