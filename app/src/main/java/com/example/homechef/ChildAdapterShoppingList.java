package com.example.homechef;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class ChildAdapterShoppingList extends RecyclerView.Adapter<ChildAdapterShoppingList.ChildViewHolder> {

    private List<Ingredient> ingredients;
    private String parentId;

    public ChildAdapterShoppingList(List<Ingredient> ingredients, String parentId) {
        this.ingredients = ingredients;
        this.parentId = parentId;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.child_view_shopping, viewGroup, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder childViewHolder, int position) {

        Ingredient childItem = ingredients.get(position);
        childViewHolder.tv_ingr.setText(childItem.getText());
        childViewHolder.cb_ingr.setChecked(childItem.getChecked());
        childViewHolder.cb_ingr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                childItem.setChecked(isChecked);
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("ShoppingList").child(uid);
                dbref.child(parentId).child("ingredients").setValue(ingredients);
            }
        });

    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    class ChildViewHolder extends RecyclerView.ViewHolder {

        TextView tv_ingr;
        CheckBox cb_ingr;

        ChildViewHolder(View itemView) {
            super(itemView);
            tv_ingr = itemView.findViewById(R.id.text);
            cb_ingr = itemView.findViewById(R.id.checkBox);
        }
    }
}
