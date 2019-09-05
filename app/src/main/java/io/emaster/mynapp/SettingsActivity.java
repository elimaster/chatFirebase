package io.emaster.mynapp;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    Toolbar actionBar;

    private FirebaseAuth mAuth;
    @Nullable
    FirebaseUser currentUser;
    @Nullable
    String currentUserID;
    DatabaseReference rootRef;

    private static final int GALLERY_PIC = 1;
    private StorageReference userProfileStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid().toString();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileStorageRef = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        initializeFields();
        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PIC);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PIC && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //TODO Set progress bar here
                //TODO message: your profile image updating...
                //...

                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileStorageRef.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            showToastMessage("profile image uploaded successfull");

                             //String downloadUrl;// = task.getResult().getUploadSessionUri().toString();
                            userProfileStorageRef.child(currentUserID+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Got the download URL for 'users/me/profile.png'
                                    // Pass it to Picasso to download, show in ImageView and caching
                                    Log.d("imageUrl__2", uri.toString());
                                    String downloadUrl = uri.toString();
                                    rootRef.child("Users").child(currentUserID).child("image")
                                            .setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        showToastMessage("profile image added to storage");
                                                    }else{
                                                        String errMessage = task.getException().getMessage();
                                                        showToastMessage(errMessage);
                                                    }
                                                }
                                            });
                                    //Picasso.get().load(uri).into(userProfileImage);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                }
                            });
                            ///////////////////////////////
                            ////task.getResult().getMetadata().getPath();

/*                            rootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                showToastMessage("profile image added to storage");
                                            }else{
                                                String errMessage = task.getException().getMessage();
                                                showToastMessage(errMessage);
                                            }
                                        }
                                    });*/
                        }else{
                            String errMessage = task.getException().getMessage().toString();
                            showToastMessage(errMessage);
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
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
        userProfileImage = findViewById(R.id.set_profile_image);
        //actionBar = getSupportActionBar();
        actionBar =  findViewById(R.id.toolbar_settings);
        setSupportActionBar(actionBar);
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        // TODO: Remove the redundant calls to getSupportActionBar()
        //       and use variable actionBar instead
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
/*        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);*/
        actionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });



        String currentUserId = currentUser.getUid();
        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("username").exists() && dataSnapshot.child("status").exists()){
                    String userNameRetrive = dataSnapshot.child("username").getValue().toString();
                    String userStatusRetrive = dataSnapshot.child("status").getValue().toString();
                    String userImageProfileRetrive;// = (String) dataSnapshot.child("image").getValue();
                    if( dataSnapshot.child("image").exists()){
                        userImageProfileRetrive =  dataSnapshot.child("image").getValue().toString();
                    }else{
                        userImageProfileRetrive ="";
                    }
                    Log.d("imageUrl", userImageProfileRetrive.toString());
                    //userProfileStorageRef = FirebaseStorage.getInstance().getReference().child("ProfileImages");


                    userProfileStorageRef.child(currentUserID+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Got the download URL for 'users/me/profile.png'
                            // Pass it to Picasso to download, show in ImageView and caching
                            Log.d("imageUrl__2", uri.toString());
                            Picasso.get().load(uri).into(userProfileImage);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });

                    //Picasso.get().load(userImageProfileRetrive).into(userProfileImage);
                    userName.setText(userNameRetrive);
                    userStatus.setText(userStatusRetrive);

                }else{
                    //sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

/*    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }*/

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        //user cannot go back to login by press arrow
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
