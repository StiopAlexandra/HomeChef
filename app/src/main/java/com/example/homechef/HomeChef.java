package com.example.homechef;

import android.app.Application;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeChef extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //enable firebase offline capabilities
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.keepSynced(true);
        DatabaseReference mRefFav = FirebaseDatabase.getInstance().getReference("FavoriteRecipes");
        mRefFav.keepSynced(true);
        DatabaseReference mRefShop = FirebaseDatabase.getInstance().getReference("ShoppingList");
        mRefShop.keepSynced(true);
    }
}
