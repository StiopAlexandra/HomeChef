package com.example.homechef;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

public class FavoritesFragment extends Fragment {

    private ArrayList<Recipe> lstFavorites;
    private RecyclerView myrv;
    private DatabaseReference mRefFav;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View RootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        progressBar = RootView.findViewById(R.id.progressBar5);
        emptyView= RootView.findViewById(R.id.empty_view_favorites);
        getFavorites(RootView);
        return RootView;
    }

    private void getFavorites(final View rootView) {
        mAuth = FirebaseAuth.getInstance();
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mRefFav = FirebaseDatabase.getInstance().getReference("FavoriteRecipes").child(uid);
        mRefFav.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lstFavorites = new ArrayList<>();
                HashMap favorites = (HashMap) dataSnapshot.getValue();
                if (favorites != null) {
                    for (Object recipe : favorites.keySet()) {
                        String name = (String) dataSnapshot.child(recipe.toString()).child("name").getValue();
                        String img = (String) dataSnapshot.child(recipe.toString()).child("img").getValue();
                        lstFavorites.add(new Recipe(recipe.toString(), name, img, "", ""));
                    }
                }
                progressBar.setVisibility(View.GONE);
                myrv = rootView.findViewById(R.id.recyclerview_favorites);
                if(lstFavorites.isEmpty()){
                    myrv.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
                else{
                    myrv.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                    RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(getContext(), lstFavorites);
                    myrv.setAdapter(myAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}