package io.emaster.mynapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;


public class GroupChatActivity extends AppCompatActivity {
    Toolbar groupChatToolbar;
    ImageButton sendMessageButton;
    EditText sendMessageEditText;
    ScrollView groupChatScrollView;
    TextView displayTextMessages;
    String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        groupName = getIntent().getExtras().get("groupName").toString();
        initializeFields();
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
}
