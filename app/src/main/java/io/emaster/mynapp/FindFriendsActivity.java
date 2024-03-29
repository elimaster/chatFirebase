package io.emaster.mynapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import io.emaster.mynapp.model.Contact;
import io.emaster.mynapp.simple_login.LoginActivity3;


public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView findfriendsRecyclerView;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        findfriendsRecyclerView = findViewById(R.id.find_friends_recycler_list);
        findfriendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(usersRef, Contact.class)
                        .build();

        FirebaseRecyclerAdapter<Contact, findFriendViewHolder> firebaseAdapter =
                new FirebaseRecyclerAdapter<Contact, findFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull findFriendViewHolder findFriendViewHolder, final int i, @NonNull Contact contact) {
                        findFriendViewHolder.userNameTV.setText(contact.getUsername());
                        findFriendViewHolder.userStatusTV.setText(contact.getStatus());
                        /////////////////////////
                        Log.d("message", contact.getImage() +"\n\n");
                        Picasso.get().load(contact.getImage()).placeholder(R.drawable.headshot_7).into(findFriendViewHolder.userImage);

                        findFriendViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String visit_user_id = getRef(i).getKey();
                                Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);
                                finish();

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public findFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        findFriendViewHolder viewHolder = new findFriendViewHolder(view);
                        return viewHolder;
                    }
                };
        findfriendsRecyclerView.setAdapter(firebaseAdapter);
        firebaseAdapter.startListening();
    }

    public static class findFriendViewHolder extends RecyclerView.ViewHolder{
        TextView userNameTV, userStatusTV;
        CircleImageView userImage;
        public findFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTV = (TextView) itemView.findViewById(R.id.user_profile_name);
            userStatusTV = (TextView) itemView.findViewById(R.id.user_profile_status);
            userImage = (CircleImageView) itemView.findViewById(R.id.users_profile_image);
        }
    }
}
