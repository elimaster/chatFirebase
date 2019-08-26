package io.emaster.mynapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeFields();

    }

    private void initializeFields() {
        userProfileImage = findViewById(R.id.set_profile_image);
        userName = findViewById(R.id.set_profile_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        updateAccountSettings = findViewById(R.id.update_settings_button);

    }
}
