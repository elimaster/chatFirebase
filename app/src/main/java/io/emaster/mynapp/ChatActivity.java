package io.emaster.mynapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.emaster.mynapp.adapter.MessageAdapter;
import io.emaster.mynapp.model.Messages;

public class ChatActivity extends AppCompatActivity {
    String messageReceiverID;
    String messageReceiverName;
    String messageReceiverImage;
    String messageSenderID;

    TextView custom_profile, custom_last_seen;
    CircleImageView custom_image;

    Toolbar chatCustomToolbar;
    ImageButton sendPrivateMessageBtn;
    EditText inputPrivateChatET;

    FirebaseAuth mAuth;
    DatabaseReference rootRef;

    List<Messages> messagesList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;
    RecyclerView recyclerViewPrivateMessagesList;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid(); //CURRENT USER
        rootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID =  getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        if(getIntent().getExtras().get("visit_user_image") != null) {
            messageReceiverImage = getIntent().getExtras().get("visit_user_image").toString();
        }
        showToastMessage(messageReceiverName);

        initializeControllers();
        custom_profile.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.headshot_7).into(custom_image);

        sendPrivateMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();

                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                //myScrollView.fullScroll(View.FOCUS_DOWN);
            }


        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //updateUserStatus("online");

        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        recyclerViewPrivateMessagesList.smoothScrollToPosition(
                                recyclerViewPrivateMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage() {
        String messageText = inputPrivateChatET.getText().toString();

        if(TextUtils.isEmpty(messageText)){
            showToastMessage("first write your message ...");
        }else{
            String messegeSenderRef = "Messages/" +  messageSenderID + "/" + messageReceiverID;
            String messegeReceiverRef = "Messages/"   + messageReceiverID + "/" +  messageSenderID;
            DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                    .child(messageSenderID)
                    .child(messageReceiverID)
                    .push();
            String messagePushID = userMessageKeyRef.getKey();
            Map messageTextBody = new HashMap();
            messageTextBody.put("message" , messageText);
            messageTextBody.put("type" , "text");
            messageTextBody.put("from" , messageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messegeSenderRef + "/"+ messagePushID, messageTextBody);
            messageBodyDetails.put(messegeReceiverRef + "/"+ messagePushID, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        showToastMessage("message sent successfully!!!");
                    }else{
                        showToastMessage("error!!!");
                    }
                    inputPrivateChatET.setText("");
                }
            });
        }
    }

    private void initializeControllers() {

         chatCustomToolbar = findViewById(R.id.private_chat_toolbar);
         setSupportActionBar(chatCustomToolbar);
         ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.castom_chat_bar, null );
        actionBar.setCustomView(actionBarView);

        custom_profile = findViewById(R.id.custom_profile_name);
        custom_last_seen = findViewById(R.id.custom_user_last_seen);
        custom_image = findViewById(R.id.castom_image_bar);

        sendPrivateMessageBtn = findViewById(R.id.send_private_message_btn);
        inputPrivateChatET = findViewById(R.id.input_private_chat);

        messageAdapter = new MessageAdapter(messagesList);
        recyclerViewPrivateMessagesList = findViewById(R.id.rv_private_message_list);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewPrivateMessagesList.setLayoutManager(linearLayoutManager);
        recyclerViewPrivateMessagesList.setAdapter(messageAdapter);
        recyclerViewPrivateMessagesList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                recyclerViewPrivateMessagesList.smoothScrollToPosition(
                        recyclerViewPrivateMessagesList.getAdapter().getItemCount());
            }
        });
        DisplayLastSeen();
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

    private void DisplayLastSeen(){
        rootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild("userState")){
                            String state  = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date  = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time  = dataSnapshot.child("userState").child("time").getValue().toString();

                            if(state.equals("online")){
                                custom_last_seen.setText("online");
                            }else if(state.equals("offline")){
                                custom_last_seen.setText("last seen: "+  date +" "+ time);
                            }
                        }else{
                            custom_last_seen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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
