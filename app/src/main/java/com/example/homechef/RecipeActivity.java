package com.example.homechef;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private ImageButton backButton, favButton, shopButton;
    private ProgressBar progressBar;
    private JSONArray ingredientsArr, instructionsArr, arr;
    ArrayList<String> ingredients = new ArrayList<String>();
    ArrayList<String> instructions = new ArrayList<String>();
    private boolean like = false, isFavorite = false;
    public static int TIMEOUT_MS = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        final Intent intent = getIntent();
        final String recipeId = Objects.requireNonNull(intent.getExtras()).getString("id");
//        final String recipeName = Objects.requireNonNull(intent.getExtras()).getString("name");
//        final String recipeImg = Objects.requireNonNull(intent.getExtras()).getString("img");
        mAuth = FirebaseAuth.getInstance();
        final String uid = mAuth.getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference().child(uid).child(recipeId);
        img = findViewById(R.id.recipe_img);
        name = findViewById(R.id.name);
        recipe = findViewById(R.id.recipe);
        progressBar = findViewById(R.id.progressBar4);
//        if (recipeImg.isEmpty()) {
//            img.setImageResource(R.drawable.nopicture);
//        } else {
//            Picasso.get().load(recipeImg).into(img);
//        }
//        name.setText(recipeName);

        getRecipeData(recipeId);

        backButton = findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        favButton = findViewById(R.id.fav);
        mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("mRootRef", String.valueOf(dataSnapshot));
                if (dataSnapshot.getValue() != null) {
                    favButton.setImageResource(R.drawable.ic_baseline_favorite_border_red_24);
                    like = true;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                like = !like;
                mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (like) {
                            favButton.setImageResource(R.drawable.ic_baseline_favorite_border_red_24);
                            Map favorites = new HashMap();
                            favorites.put("img", intent.getExtras().getString("img"));
                            favorites.put("name", intent.getExtras().getString("name"));
                            mRootRef.setValue(favorites);
                        } else {
                            try {
                                favButton.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                                mRootRef.setValue(null);
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

//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switch (v.getId()) {
//                    case R.id.fav:
//                        break;
//                    case R.id.shop:
//                        break;
//                }
//            }
//        });

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
                            //Log.i("recipe:", String.valueOf(response));
                            //arr = (JSONArray) response.get("results");
                            //JSONObject jsonObject = arr.getJSONObject(0);
                            if (response.optString("thumbnail_url").isEmpty()) {
                                img.setImageResource(R.drawable.nopicture);
                            } else {
                                Picasso.get().load(response.optString("thumbnail_url")).into(img);
                            }
                            name.setText(response.optString("name"));
                            instructionsArr = response.getJSONArray("instructions");
                            for (int i = 0; i < instructionsArr.length(); i++) {
                                JSONObject jsonObject1;
                                jsonObject1 = instructionsArr.getJSONObject(i);
                                instructions.add(jsonObject1.optString("display_text"));
                            }
                            RecyclerViewAdapterRow adapter1 = new RecyclerViewAdapterRow(getApplicationContext(), instructions);
                            instructions_rv.setAdapter(adapter1);
                            ingredientsArr = response.getJSONArray("sections");
                            JSONObject jsonObject2;
                            jsonObject2 = ingredientsArr.getJSONObject(0);
                            JSONArray jsonArray;
                            jsonArray = jsonObject2.getJSONArray("components");
                            for (int j = 0; j < jsonArray.length(); j++) {
                                JSONObject jsonObject3;
                                jsonObject3 = jsonArray.getJSONObject(j);
                                ingredients.add(jsonObject3.optString("raw_text"));
                            }

                            RecyclerViewAdapterRow adapter2 = new RecyclerViewAdapterRow(getApplicationContext(), ingredients);
                            ingredients_rv.setAdapter(adapter2);
                            progressBar.setVisibility(View.GONE);
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
                       // recyclerView.setAlpha(0);
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.recipe_toolbar, menu);
//        MenuItem favItem = menu.findItem(R.id.fav);
//        //set different icon when isFavorite is true.
//        if (isFavorite){
//            favItem.setIcon(R.drawable.ic_baseline_favorite_border_red_24);
//        }
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.fav:
//                like = !like;
//                mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (like) {
//                            item.setIcon(R.drawable.ic_baseline_favorite_border_red_24);
//                            Map favorites = new HashMap();
//                            favorites.put("img", intent.getExtras().getString("img"));
//                            favorites.put("title", intent.getExtras().getString("title"));
//                            mRootRef.setValue(favorites);
//                        } else {
//                            try {
//                                item.setIcon(R.drawable.ic_baseline_favorite_border_24);
//                                mRootRef.setValue(null);
//                            } catch (Exception e) {
//                            }
//                        }
//                    }
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                    }
//                });
//            case R.id.shop:
//                //addfav (heart icon) was clicked, Insert your after click code here.
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
}