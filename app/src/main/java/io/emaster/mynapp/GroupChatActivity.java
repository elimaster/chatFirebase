package io.emaster.mynapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import io.emaster.mynapp.adapter.MessageListAdapter;


public class GroupChatActivity extends AppCompatActivity {
    Toolbar groupChatToolbar;
    ImageButton sendMessageButton;
    EditText sendMessageEditText;
    ScrollView groupChatScrollView;
    TextView displayTextMessages;
    ScrollView myScrollView;
    String groupName;
    String currentUserId, currentUserName;
    String currentDate, currentTime;
    FirebaseAuth mAuth;
    DatabaseReference usersRef, groupsRef;
    DatabaseReference groupMessageKeyRef;

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;

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
                //hide keyboard
/*                getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                );*/
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                myScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    displayMessages(dataSnapshot);
                }
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

    private void displayMessages(DataSnapshot dataSnapshot) {
        Handler h = new Handler(getApplicationContext().getMainLooper());
       Iterator iterator =  dataSnapshot.getChildren().iterator();
       while(iterator.hasNext()){
           final String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
           final String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
           final String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();
           final String chatUsername = (String) ((DataSnapshot) iterator.next()).getValue();

           //displayTextMessages.append(chatUsername +" : \n"+ chatMessage + "\n"+ chatTime + "   "+ chatDate+ "\n\n\n");
           Log.d("message", chatUsername +" : \n"+ chatMessage + "\n"+ chatTime + "   "+ chatDate+ "\n\n\n");


           h.post(new Runnable() {
               @Override
               public void run() {
                   displayTextMessages.append(chatUsername +" : \n"+ chatMessage + "\n"+ chatTime + "   "+ chatDate+ "\n\n\n");
               }
           });

           myScrollView.fullScroll(View.FOCUS_DOWN);
           myScrollView.post(new Runnable() {
               @Override
               public void run() {
                   myScrollView.fullScroll(View.FOCUS_DOWN);
               }
           });
       }
        myScrollView.post(new Runnable() {
            @Override
            public void run() {
                myScrollView.fullScroll(View.FOCUS_DOWN);
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
        myScrollView = findViewById(R.id.group_chat_scroll_view);
        myScrollView.fullScroll(View.FOCUS_DOWN);
        myScrollView.post(new Runnable() {
            @Override
            public void run() {
                myScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });


        //displayTextMessages.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        //mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        //mMessageAdapter = new MessageListAdapter(this, messageList);
        //mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));

    }

    private void showToastMessage(String message) {
        Toast toast= Toast.makeText(getApplicationContext(),
                message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
