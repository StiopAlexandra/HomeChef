package com.example.homechef;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsFragment extends Fragment {

    private EditText email_edit, fullName_edit;
    private SwitchCompat switchTheme;
    private FirebaseAuth mAuth;
    private DatabaseReference mRefUser;
    private Button logout, save;
    private String uid;
    private User currentUser;
    private SharedPreferences sharedPreferences = null;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View RootView = inflater.inflate(R.layout.fragment_settings, container, false);
        email_edit= RootView.findViewById(R.id.edit2);
        fullName_edit= RootView.findViewById(R.id.edit1);
        switchTheme= RootView.findViewById(R.id.switchTheme);
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("night_mode",true)){
            switchTheme.setChecked(true);
        }

        switchTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    switchTheme.setChecked(true);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("night_mode",true);
                    editor.commit();

                }else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    switchTheme.setChecked(false);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("night_mode",false);
                    editor.commit();
                }
                Intent main = new Intent(getActivity(), MainActivity.class);
                startActivity(main);
            }
        });

        logout = RootView.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                sendToLogin();
            }
        });

        save = RootView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveProfile();
                email_edit.clearFocus();
                fullName_edit.clearFocus();
                save.requestFocus();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        mRefUser = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        mRefUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fullName = (String) dataSnapshot.child("fullName").getValue();
                String email = (String) dataSnapshot.child("email").getValue();
                currentUser = new User(fullName, email);
                email_edit.setText(currentUser.getEmail());
                fullName_edit.setText(currentUser.getFullName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return RootView;
    }

    private void saveProfile() {
        String email = email_edit.getText().toString().trim();
        String fullName = fullName_edit.getText().toString().trim();

        if(email.isEmpty()){
            email_edit.setText(currentUser.getEmail());
            email_edit.clearFocus();
            email = currentUser.getEmail();
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            email_edit.setError("Please provide valid email!");
            email_edit.requestFocus();
            return;
        }

        if(fullName.isEmpty()){
            fullName_edit.setText(currentUser.getFullName());
            fullName_edit.clearFocus();
            fullName = currentUser.getFullName();
        }

        mRefUser.child("fullName").setValue(fullName);
        mRefUser.child("email").setValue(email);
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
        startActivity(loginIntent);
        requireActivity().finish();
    }
}