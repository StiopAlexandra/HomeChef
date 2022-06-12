package com.example.homechef;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ShoppingListFragment extends Fragment {

    private ArrayList<ShoppingRecipe> lstShop;
    private List<Ingredient> ingredients;
    private RecyclerView myrv;
    private DatabaseReference mRefShop;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView emptyView;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View RootView = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        progressBar = RootView.findViewById(R.id.progressBar6);
        emptyView = RootView.findViewById(R.id.empty_view_shopping);
        getShoppingList(RootView);
        return RootView;
    }

    private void getShoppingList(View rootView) {
        mAuth = FirebaseAuth.getInstance();
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mRefShop = FirebaseDatabase.getInstance().getReference("ShoppingList").child(uid);
        mRefShop.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap shopping = (HashMap) dataSnapshot.getValue();
                lstShop = new ArrayList<>();
                if (shopping != null) {
                    for (Object recipe : shopping.keySet()) {
                        String name = (String) dataSnapshot.child(recipe.toString()).child("name").getValue();
                        ingredients = new ArrayList<Ingredient>();
                        for (DataSnapshot data : dataSnapshot.child(recipe.toString()).child("ingredients").getChildren()) {
                            Ingredient i = data.getValue(Ingredient.class);
                            String text = i.getText();
                            Boolean checked = i.getChecked();
                            ingredients.add(new Ingredient(text, checked));
                        }
                        lstShop.add(new ShoppingRecipe(recipe.toString(), name, ingredients));

                    }
                }
                progressBar.setVisibility(View.GONE);
                myrv = rootView.findViewById(R.id.recyclerview_shopping);
                if (lstShop.isEmpty()) {
                    myrv.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                    ParentAdapterShoppingList myAdapter = new ParentAdapterShoppingList(lstShop);
                    myrv.setAdapter(myAdapter);
                    myrv.setLayoutManager(layoutManager);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}