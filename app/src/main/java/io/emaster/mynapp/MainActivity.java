package io.emaster.mynapp;

//import androidx.fragment.app.Fragment;

//import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import io.emaster.mynapp.simple_login.LoginActivity3;
import io.emaster.mynapp.ui.login.*;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import io.emaster.mynapp.HomeFragment;

import static androidx.constraintlayout.solver.widgets.ConstraintWidget.GONE;

public class MainActivity extends AppCompatActivity {
    private TextView mTextMessage;
    BottomNavigationView navView;
    HomeFragment homeFragment;
    ProfileFragment profileFragment;
    SettingsFragment settingsFragment;

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(currentUser != null) {
            String email = currentUser.getEmail();
            getSupportActionBar().setTitle(email);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryS)));
        }

        setupBottomNavigation();

        if (savedInstanceState == null) {

            loadHomeFragment();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_find_friends_option){

        }
        if(item.getItemId() == R.id.main_settings_option){
            sendUserToSettingsActivity();
        }
        if(item.getItemId() == R.id.main_logout_option){
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        return true;
    }



    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser == null){
            sendUserToLoginActivity();
        }
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity3.class);
        startActivity(loginIntent);
    }

    private void setupBottomNavigation() {

        navView = findViewById(R.id.nav_view);

        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        loadHomeFragment();
                        return true;
                    case R.id.navigation_dashboard://profile
                        loadProfileFragment();
                        return true;
                    case R.id.navigation_notifications://settings
                        loadSettingsFragment();
                        return true;
                }
                return false;
            }
        });
    }

    private void loadHomeFragment() {



        homeFragment =  HomeFragment.newInstance();
        androidx.fragment.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, homeFragment);
        ft.commit();


    }

    private void loadProfileFragment() {

        androidx.fragment.app.FragmentManager fragmentManager =  getSupportFragmentManager();
        androidx.fragment.app.FragmentTransaction ft = fragmentManager.beginTransaction();
        profileFragment = ProfileFragment.newInstance();
        ft.replace(R.id.fragment_frame, profileFragment);
        ft.commit();
    }

    private void loadSettingsFragment() {

        settingsFragment = SettingsFragment.newInstance();
        androidx.fragment.app.FragmentManager fragmentManager =  getSupportFragmentManager();
        androidx.fragment.app.FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_frame, settingsFragment);
        ft.commit();
    }

}
