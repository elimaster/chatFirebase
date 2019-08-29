package io.emaster.mynapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class GroupChatActivity extends AppCompatActivity {
    Toolbar groupChatToolbar;
    ImageButton sendMessageButton;
    EditText sendMessageEditText;
    ScrollView groupChatScrollView;
    TextView displayTextMessages;
    String groupName;
    String currentUserId, currentUserName;
    String currentDate, currentTime;
    FirebaseAuth mAuth;
    DatabaseReference usersRef, groupsRef;
    DatabaseReference groupMessageKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        groupName = getIntent().getExtras().get("groupName").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);


        initializeFields();
        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMessageInfoToDatabase();
                sendMessageEditText.setText("");
            }
        });
    }

    private void saveMessageInfoToDatabase() {
        String message = sendMessageEditText.getText().toString();
        String messageKey = groupsRef.push().getKey();
        if(TextUtils.isEmpty(message)){
            showToastMessage("message empty");
        }else{
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupsRef.updateChildren(groupMessageKey);
            groupMessageKeyRef = groupsRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("username", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            groupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void getUserInfo() {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("username").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields() {
        groupChatToolbar = findViewById(R.id.group_chat_toolbar);
        setSupportActionBar(groupChatToolbar);
        getSupportActionBar().setTitle(groupName);
        sendMessageButton = findViewById(R.id.send_message_btn);
        sendMessageEditText = findViewById(R.id.input_group_chat);
        groupChatScrollView = findViewById(R.id.group_chat_scroll_view);
        displayTextMessages = findViewById(R.id.group_chat_text_display);
    }

    private void showToastMessage(String message) {
        Toast toast= Toast.makeText(getApplicationContext(),
                message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
