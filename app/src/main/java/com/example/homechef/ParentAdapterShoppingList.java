package com.example.homechef;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class ParentAdapterShoppingList extends RecyclerView.Adapter<ParentAdapterShoppingList.ParentViewHolder> {

    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private List<ShoppingRecipe> itemList;

    ParentAdapterShoppingList(List<ShoppingRecipe> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ParentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.parent_view_shopping, viewGroup, false);
        return new ParentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParentViewHolder parentViewHolder, int position) {
        ShoppingRecipe parentItem = itemList.get(position);
        parentViewHolder.tvTitle.setText(parentItem.getName());
        LinearLayoutManager layoutManager = new LinearLayoutManager(parentViewHolder.rvIngredients.getContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setInitialPrefetchItemCount(parentItem.getIngredients().size());
        ChildAdapterShoppingList childAdapter = new ChildAdapterShoppingList(parentItem.getIngredients(), parentItem.getId());
        parentViewHolder.rvIngredients.setLayoutManager(layoutManager);
        parentViewHolder.rvIngredients.setAdapter(childAdapter);
        parentViewHolder.rvIngredients.setRecycledViewPool(viewPool);
        parentViewHolder.buttonList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("ShoppingList").child(uid);
                String id = parentItem.getId();
                Query query = dbref.child(id);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().removeValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class ParentViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle;
        private ImageButton buttonList;
        private RecyclerView rvIngredients;

        ParentViewHolder(final View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.recipeName);
            buttonList = itemView.findViewById(R.id.buttonList);
            rvIngredients = itemView.findViewById(R.id.recyclerViewIngredients);
        }
    }
}

