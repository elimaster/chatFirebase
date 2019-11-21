package io.emaster.mynapp.simple_login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.iid.FirebaseInstanceId;

import com.shaishavgandhi.loginbuttons.BaseButton;
import com.shaishavgandhi.loginbuttons.GoogleButton;

import io.emaster.mynapp.MainActivity;
import io.emaster.mynapp.PhoneLoginActivity;
import io.emaster.mynapp.R;

public class LoginActivity3  extends AppCompatActivity  {
    private static final String TAG = "LoginActivity3";
    private static final int REQUEST_SIGNUP = 0;
    EditText usernameEditText;
    EditText passwordEditText;
    ProgressBar loadingProgressBar;

    GoogleButton round_btn_1;
/*    com.shaishavgandhi.loginbuttons.FacebookButton  round_btn_2;
    com.shaishavgandhi.loginbuttons.TwitterButton  round_btn_3;
    com.shaishavgandhi.loginbuttons.LinkedInButton round_btn_4;*/

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login3);

         round_btn_1 = findViewById(R.id.round_btn_1);

         //round_btn_1.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_home_black_24dp));
         //round_btn_1.setBackgroundResource(R.drawable.ic_home_black_24dp);
        //round_btn_1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_home_black_24dp, 0, 0, 0);


        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        //currentUser = mAuth.getCurrentUser();
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.loading);
        final TextView linkToRegister = findViewById(R.id.link_signup);


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
                Intent intent = new Intent(getApplicationContext(), RegisterActivity3.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();
            }
        });

        round_btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity3.this, PhoneLoginActivity.class);
                startActivity(intent);
            }
        });

    }

    private void AllowUserToLogin() {
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
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String email = "";
                                if(currentUser != null) {
                                    email = currentUser.getEmail();
                                }
                                loadingProgressBar.setVisibility(View.GONE);
                                //progressBarMessage.setVisibility(View.GONE);
                                Toast toast= Toast.makeText(getApplicationContext(),
                                        "hello user: "+ email, Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();

                                final String currentUserID = mAuth.getCurrentUser().getUid();
                                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( LoginActivity3.this,  new OnSuccessListener<InstanceIdResult>() {
                                    @Override
                                    public void onSuccess(InstanceIdResult instanceIdResult) {
                                        String newToken = instanceIdResult.getToken();
                                        Log.e("newToken",newToken);

                                        usersRef.child(currentUserID).child("device_token")
                                                .setValue(newToken)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            sendUserToMainActivity();
                                                        }
                                                    }
                                                });

                                    }
                                });


                                //FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this) { instanceIdResult ->
                                //        val deviceToken = instanceIdResult.token
                                //}
                                //sendUserToMainActivity();
                            }else {
                                String errorMessage = task.getException().toString();
                                Toast toast= Toast.makeText(getApplicationContext(),
                                        errorMessage, Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();
                                Log.e("Login", ""+ errorMessage);
                            }
                        }
                    });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        //user cannot go back to login by press arrow
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }




/*    class FCMMessageReceiverService extends FirebaseMessagingService() {

        override fun onNewToken(token: String?) {
            super.onNewToken(token)
            // Perform your task
        }

        override fun onMessageReceived(remoteMessage: RemoteMessage?) {
            // Create notification
        }
    }*/
}
