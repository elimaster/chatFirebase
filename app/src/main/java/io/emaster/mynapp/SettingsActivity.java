package io.emaster.mynapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

        initializeFields();
        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings();
            }
        });

    }

    private void updateSettings() {
        String userId = currentUser.getUid();
        String username = userName.getText().toString();
        String userstatus = userStatus.getText().toString();
        String message;
        if (TextUtils.isEmpty(username)) {
            message = "Please provide username";
            showToastMessage(message);
        }else if(TextUtils.isEmpty(userstatus)){
            message = "Please provide user status";
            showToastMessage(message);
        }else{
            HashMap<String, String> profileUserMap = new HashMap<>();
            profileUserMap.put("uid", userId);
            profileUserMap.put("username", username);
            profileUserMap.put("status", userstatus);
            rootRef.child("Users").child(userId).setValue(profileUserMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String message;
                            if(task.isSuccessful()){
                                message = "Profile Updated Successfully";
                                showToastMessage(message);
                                sendToMainActivity();
                            }else{
                                message = task.getException().getMessage();
                                showToastMessage(message);
                            }
                        }
                    });
        }
    }

    private void showToastMessage(String message) {
        Toast toast= Toast.makeText(getApplicationContext(),
                message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    private void initializeFields() {
        userProfileImage = findViewById(R.id.set_profile_image);
        userName = findViewById(R.id.set_profile_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        updateAccountSettings = findViewById(R.id.update_settings_button);

    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        //user cannot go back to login by press arrow
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
