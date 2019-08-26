package io.emaster.mynapp.simple_login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.emaster.mynapp.MainActivity;
import io.emaster.mynapp.R;

public class RegisterActivity3 extends AppCompatActivity {
    private static final String TAG = "RegisterActivity3";
    private static final int REQUEST_SIGNUP = 0;
    private FirebaseAuth mAuth;
    DatabaseReference rootRef;
     EditText usernameEditText;
     EditText passwordEditText;
     TextView progressBarMessage;
    ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register3);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();//"Users");

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.loading);
        final TextView linkToRegister = findViewById(R.id.link_signup);
        //progressBarMessage = findViewById(R.id.progressBarMessage);

        usernameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        linkToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), LoginActivity3.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }


        });
    }

    private void CreateNewAccount() {
        String email = usernameEditText.getText().toString();
        String pass = passwordEditText.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "please enter email", Toast.LENGTH_LONG);
        }
        if(TextUtils.isEmpty(pass)){
            Toast.makeText(this, "please enter password", Toast.LENGTH_LONG);
        }
        else {
            loadingProgressBar.setVisibility(View.VISIBLE);
            //progressBarMessage.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String userEmail = mAuth.getCurrentUser().getEmail();
                                String currentUserId = mAuth.getCurrentUser().getUid();
                                //String userId = rootRef.push().getKey();


                                rootRef.child("Users").child(currentUserId).setValue(userEmail);
                                //rootRef.child("Users").child(currentUserId).setValue("");

                                loadingProgressBar.setVisibility(View.GONE);
                                //progressBarMessage.setVisibility(View.GONE);
                                Toast toast= Toast.makeText(getApplicationContext(),
                                        "Account Created Successfully!", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();

                                //sendToLoginActivity();
                                sendToMainActivity();
                            }else {
                                String errorMessage = task.getException().toString();
                                Toast toast= Toast.makeText(getApplicationContext(),
                                        errorMessage, Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();
                            }
                        }
                    });
        }


    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        //user cannot go back to login by press arrow
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendToLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity3.class);
        startActivity(loginIntent);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
