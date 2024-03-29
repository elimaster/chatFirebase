package io.emaster.mynapp;

//import androidx.fragment.app.Fragment;

//import androidx.fragment.app.Fragment;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import io.emaster.mynapp.simple_login.LoginActivity3;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

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
    DatabaseReference rootRef;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        //currentUser = mAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if(currentUser != null) {
            String email = currentUser.getEmail();
            if(!TextUtils.isEmpty(email)){
                getSupportActionBar().setTitle(email);
            }
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
        if(item.getItemId() == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
            //getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            //getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return true;
        }
        if(item.getItemId() == R.id.main_find_friends_option){
            sendUserToFindFriendsActivityBackEnabled();
        }
        if(item.getItemId() == R.id.main_create_group_option){
           requestNewGroup();
        }
        if(item.getItemId() == R.id.main_settings_option){
            sendUserToSettingsActivityBackEnabled();
        }
        if(item.getItemId() == R.id.main_logout_option){
            updateUserStatus("offline");
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        return true;
    }

    private void requestNewGroup() {
        AlertDialog.Builder alertDialogGroup = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        alertDialogGroup.setTitle("Enter Group Name :");
        final EditText groupNameEditText = new EditText(MainActivity.this);
        groupNameEditText.setHint("write name group");
        alertDialogGroup.setView(groupNameEditText);
        alertDialogGroup.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameEditText.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    showToastMessage("please enter group name");
                }else{
                    createNewGroup(groupName);
                }
            }

        });
        alertDialogGroup.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertDialogGroup.show();
    }

    private void createNewGroup(final String groupName) {
        rootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showToastMessage(groupName + " is created successfully");
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            sendUserToLoginActivity();
        }else{
            updateUserStatus("online");
            verifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            //updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            updateUserStatus("offline");
        }
    }

    private void verifyUserExistance() {
        String currentUserId = currentUser.getUid();
        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("username").exists()){
                    String username = dataSnapshot.child("username").getValue().toString();
                    Toast toast= Toast.makeText(getApplicationContext(),
                            "Welcome " + username, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                }else{
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        finish();
    }

    private void sendUserToSettingsActivityBackEnabled(){
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        //settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
    }

    private void sendUserToFindFriendsActivityBackEnabled(){
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        //settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(findFriendsIntent);
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity3.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
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
        ft.replace(R.id.fragment_frame, homeFragment, "homeFragment");
        //ft.attach(homeFragment);
        ft.addToBackStack(null);
        ft.commit();


    }

    private void loadProfileFragment() {

        androidx.fragment.app.FragmentManager fragmentManager =  getSupportFragmentManager();
        androidx.fragment.app.FragmentTransaction ft = fragmentManager.beginTransaction();
        profileFragment = ProfileFragment.newInstance();
        ft.replace(R.id.fragment_frame, profileFragment, "profileFragment");
        //ft.attach(profileFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void loadSettingsFragment() {

        settingsFragment = SettingsFragment.newInstance();
        androidx.fragment.app.FragmentManager fragmentManager =  getSupportFragmentManager();
        androidx.fragment.app.FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_frame, settingsFragment, "settingsFragment");//   .add(R.id.fragment_container, firstFragment)
        //ft.attach(settingsFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void showToastMessage(String message) {
        Toast toast= Toast.makeText(getApplicationContext(),
                message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    private void updateUserStatus(String state){
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar =  Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("time", saveCurrentTime);
        onlineState.put("date", saveCurrentDate);
        onlineState.put("state", state);

        currentUserID =mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineState);

    }

}
