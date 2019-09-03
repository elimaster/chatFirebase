package io.emaster.mynapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;



public class PhoneLoginActivity extends AppCompatActivity {
    String TAG = "PhoneLoginActivity";
    Button sendVerificationBtn;
    Button verifyBtn;
    EditText inputPhoneNumEt;
    EditText inputCodeEt;

    private FirebaseAuth mAuth;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        sendVerificationBtn = findViewById(R.id.send_verification_btn);
        verifyBtn = findViewById(R.id.verify_btn);
        inputPhoneNumEt = findViewById(R.id.phone_number_input);
        inputCodeEt = findViewById(R.id.verification_code_input);

        sendVerificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
/*                sendVerificationBtn.setVisibility(View.INVISIBLE);
                inputPhoneNumEt.setVisibility(View.INVISIBLE);
                verifyBtn.setVisibility(View.VISIBLE);
                inputCodeEt.setVisibility(View.VISIBLE);*/

                String numberPhone = inputPhoneNumEt.getText().toString();
                if(TextUtils.isEmpty(numberPhone)){
                    showToastMessage("phone number required!");
                }else{
                    //loading bar put here
                    ///...
                    // [START start_phone_auth]
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            numberPhone,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks
                    // [END start_phone_auth]
                }
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationBtn.setVisibility(View.INVISIBLE);
                inputPhoneNumEt.setVisibility(View.INVISIBLE);

                String verifycationCode = inputCodeEt.getText().toString();
                if(TextUtils.isEmpty(verifycationCode)){
                    showToastMessage("verification number required!");
                }else{
                    //loading bar put here
                    ///...
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verifycationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                showToastMessage("Invalid Phone Number!");
                showToastMessage(e.getMessage());

                sendVerificationBtn.setVisibility(View.VISIBLE);
                inputPhoneNumEt.setVisibility(View.VISIBLE);
                verifyBtn.setVisibility(View.INVISIBLE);
                inputCodeEt.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                showToastMessage("code has been sent!");
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                //change ui
                sendVerificationBtn.setVisibility(View.INVISIBLE);
                inputPhoneNumEt.setVisibility(View.INVISIBLE);
                verifyBtn.setVisibility(View.VISIBLE);
                inputCodeEt.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            showToastMessage("Sign in Success!");
                            FirebaseUser user = task.getResult().getUser();
                            sendUserToMainActivity();
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            showToastMessage("Sign in with credentials Failed!");
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        //user cannot go back to login by press arrow
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void showToastMessage(String message) {
        Toast toast= Toast.makeText(getApplicationContext(),
                message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
