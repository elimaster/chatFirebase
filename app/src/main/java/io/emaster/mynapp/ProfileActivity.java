package io.emaster.mynapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiverUserID;
    String currentUserID;
    CircleImageView userProfileImage;
    TextView userProfileName;
    TextView userProfileStatus;
    Button sendMessageRequestButton;
    Button declineMessageRequestButton;
    String current_state;

    FirebaseAuth mAuth;
    DatabaseReference userRef;
    DatabaseReference chatRequestRef;
    DatabaseReference contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("ChatRequests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        //showToastMessage("user id: "+ receiverUserID);
        currentUserID = mAuth.getCurrentUser().getUid();

        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_user_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_btn);
        declineMessageRequestButton = findViewById(R.id.decline_message_request_btn);

        retrieveUserInfo();
    }

    private void retrieveUserInfo() {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("image")){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("username").getValue().toString();
                    String userProfile = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.headshot_7).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userProfile);

                    current_state = "new";
                    manageChatRequests();
                }else{

                    String userName = dataSnapshot.child("username").getValue().toString();
                    String userProfile = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userProfile);

                    current_state = "new";
                    manageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequests() {
        chatRequestRef.child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiverUserID)){
                            String request_type =  dataSnapshot.child(receiverUserID).child("request_type")
                                    .getValue().toString();
                            if(request_type.equals("sent")){
                                current_state = "request_sent";
                                sendMessageRequestButton.setText("Cancel chat request");
                            }
                            else if(request_type.equals("received")){
                                current_state = "request_received";
                                sendMessageRequestButton.setText("Accept chat request");
                                declineMessageRequestButton.setEnabled(true);
                                declineMessageRequestButton.setText("Decline chat request");
                                declineMessageRequestButton.setVisibility(View.VISIBLE);
                                declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }else{//lets check if now friends
                            contactsRef.child(currentUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(receiverUserID)){
                                                current_state = "friends";
                                                sendMessageRequestButton.setText("remove contact");

                                                //declineMessageRequestButton.setEnabled(false);
                                                //declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if(!currentUserID.equals(receiverUserID)){
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessageRequestButton.setEnabled(false);
                    if(current_state.equals("new")){
                        sendChatRequest();
                    }
                    if(current_state.equals("request_sent")){
                        cancelChatRequest();
                    }
                    if(current_state.equals("request_received")){
                        acceptChatRequest();
                    }
                    if(current_state.equals("friends")){
                        removeFriendContact();
                    }
                }
            });
        }else{
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeFriendContact() {
        contactsRef.child(currentUserID).child(receiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    contactsRef.child(receiverUserID).child(currentUserID)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendMessageRequestButton.setEnabled(true);
                                current_state = "new";
                                sendMessageRequestButton.setText("send message");

                                declineMessageRequestButton.setEnabled(false);
                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptChatRequest() {
        contactsRef.child(currentUserID).child(receiverUserID)
                .child("contact").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsRef.child(receiverUserID).child(currentUserID)
                                    .child("contact").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                chatRequestRef.child(currentUserID).child(receiverUserID)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            chatRequestRef.child(receiverUserID).child(currentUserID)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        sendMessageRequestButton.setEnabled(true);
                                                                        current_state = "friends";
                                                                        sendMessageRequestButton.setText("remove contact");

                                                                        declineMessageRequestButton.setEnabled(false);
                                                                        declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelChatRequest() {
        chatRequestRef.child(currentUserID).child(receiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    chatRequestRef.child(receiverUserID).child(currentUserID)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendMessageRequestButton.setEnabled(true);
                                current_state = "new";
                                sendMessageRequestButton.setText("send message");

                                declineMessageRequestButton.setEnabled(false);
                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest() {
        chatRequestRef.child(currentUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            chatRequestRef.child(receiverUserID).child(currentUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendMessageRequestButton.setEnabled(true);
                                                current_state = "request_sent";
                                                sendMessageRequestButton.setText("cancel chat request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void showToastMessage(String message) {
        Toast toast= Toast.makeText(getApplicationContext(),
                message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
