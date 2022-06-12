package com.example.homechef;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RecipeActivity extends AppCompatActivity {

    private TextView name;
    private ImageView img;
    private RecyclerView ingredients_rv, instructions_rv;
    private LinearLayout recipe;
    private DatabaseReference mRefFav, mRefShop;
    private FirebaseAuth mAuth;
    private ImageButton backButton, favButton, shopButton;
    private ProgressBar progressBar;
    private TextView emptyView;
    ArrayList<String> ingredients = new ArrayList<String>();
    ArrayList<Ingredient> shopIngr = new ArrayList<Ingredient>();
    ArrayList<String> instructions = new ArrayList<String>();
    private boolean favorite = false, shop = false;
    public static int TIMEOUT_MS = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        final Intent intent = getIntent();
        final String recipeId = Objects.requireNonNull(intent.getExtras()).getString("id");
        mAuth = FirebaseAuth.getInstance();
        final String uid = mAuth.getCurrentUser().getUid();
        mRefFav = FirebaseDatabase.getInstance().getReference("FavoriteRecipes").child(uid).child(recipeId);
        mRefShop = FirebaseDatabase.getInstance().getReference("ShoppingList").child(uid).child(recipeId);
        img = findViewById(R.id.recipe_img);
        name = findViewById(R.id.name);
        recipe = findViewById(R.id.recipe);
        progressBar = findViewById(R.id.progressBar4);
        emptyView = findViewById(R.id.empty_view);

        getRecipeData(recipeId);

        backButton = findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        favButton = findViewById(R.id.fav);
        shopButton = findViewById(R.id.shop);
        mRefFav.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    favButton.setImageResource(R.drawable.ic_baseline_favorite_border_red_24);
                    favorite = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRefShop.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    shopButton.setImageResource(R.drawable.ic_baseline_playlist_add_check_red_24);
                    shop = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favorite = !favorite;
                mRefFav.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (favorite) {
                            favButton.setImageResource(R.drawable.ic_baseline_favorite_border_red_24);
                            Map favorites = new HashMap();
                            favorites.put("img", intent.getExtras().getString("img"));
                            favorites.put("name", intent.getExtras().getString("name"));
                            mRefFav.setValue(favorites);
                        } else {
                            try {
                                favButton.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                                mRefFav.setValue(null);
                            } catch (Exception e) {
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });

        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shop = !shop;
                mRefShop.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (shop) {
                            shopButton.setImageResource(R.drawable.ic_baseline_playlist_add_check_red_24);
                            Map shopList = new HashMap();
                            shopList.put("name", intent.getExtras().getString("name"));
                            shopList.put("ingredients", shopIngr);
                            mRefShop.setValue(shopList);
                        } else {
                            try {
                                shopButton.setImageResource(R.drawable.ic_baseline_playlist_add_check_24);
                                mRefShop.setValue(null);
                            } catch (Exception e) {
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });

        ingredients_rv = findViewById(R.id.recipe_ingredients_rv);
        instructions_rv = findViewById(R.id.recipe_instructions_rv);
        ingredients_rv.setLayoutManager(new LinearLayoutManager(this));
        instructions_rv.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getRecipeData(final String recipeId) {
        recipe.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        String URL = "https://tasty.p.rapidapi.com/recipes/get-more-info?id=" + recipeId;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.optString("thumbnail_url").isEmpty()) {
                                img.setImageResource(R.drawable.nopicture);
                            } else {
                                Picasso.get().load(response.optString("thumbnail_url")).into(img);
                            }
                            name.setText(response.optString("name"));
                            JSONArray instructionsArr;
                            instructionsArr = response.getJSONArray("instructions");
                            for (int i = 0; i < instructionsArr.length(); i++) {
                                JSONObject jsonObject1;
                                jsonObject1 = instructionsArr.getJSONObject(i);
                                instructions.add(jsonObject1.optString("display_text"));
                            }
                            RecyclerViewAdapterRow adapter1 = new RecyclerViewAdapterRow(getApplicationContext(), instructions);
                            instructions_rv.setAdapter(adapter1);
                            JSONArray ingredientsArr;
                            ingredientsArr = response.getJSONArray("sections");
                            for (int i = 0; i < ingredientsArr.length(); i++) {
                                JSONObject jsonObject2;
                                jsonObject2 = ingredientsArr.getJSONObject(i);
                                JSONArray jsonArray;
                                jsonArray = jsonObject2.getJSONArray("components");
                                for (int j = 0; j < jsonArray.length(); j++) {
                                    JSONObject jsonObject3;
                                    jsonObject3 = jsonArray.getJSONObject(j);
                                    shopIngr.add(new Ingredient(jsonObject3.optString("raw_text"), false));
                                    ingredients.add(jsonObject3.optString("raw_text"));
                                }
                            }
                            RecyclerViewAdapterRow adapter2 = new RecyclerViewAdapterRow(getApplicationContext(), ingredients);
                            ingredients_rv.setAdapter(adapter2);
                            progressBar.setVisibility(View.GONE);
                            emptyView.setVisibility(View.GONE);
                            recipe.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("error", error.toString());
                        progressBar.setVisibility(View.GONE);
                        recipe.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("X-RapidAPI-Host", "tasty.p.rapidapi.com");
                params.put("X-RapidAPI-Key", "d5540d8d3bmsh12499a2ff0cfed6p13fb7bjsn5774dd02c36d");
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }
}