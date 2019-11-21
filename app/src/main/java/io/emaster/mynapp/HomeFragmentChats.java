package io.emaster.mynapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import io.emaster.mynapp.model.Contact;

public class HomeFragmentChats extends Fragment {

    View privateChatView;
    RecyclerView privateChatRV;
    DatabaseReference privateChatsRef;
    DatabaseReference usersRef;
    FirebaseAuth mAuth;
    String currentUserID;

    public static HomeFragmentChats newInstance() {

        return new HomeFragmentChats();
    }

    // @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, /*@Nullable*/ ViewGroup container, Bundle savedInstanceState) {
        privateChatView = inflater.inflate(R.layout.fragment_home_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        privateChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        privateChatRV = privateChatView.findViewById(R.id.private_chats_recycler_view);
        privateChatRV.setLayoutManager(new LinearLayoutManager(getContext()));

        privateChatView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        return privateChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(privateChatsRef, Contact.class)
                        .build();
        FirebaseRecyclerAdapter<Contact,privateChatViewHolder> myPrivateChatAdapter
                = new FirebaseRecyclerAdapter<Contact, privateChatViewHolder>(options) {

            @NonNull
            @Override
            public privateChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                privateChatViewHolder viewHolder = new privateChatViewHolder(view);
                return viewHolder;
            }

            @Override
            protected void onBindViewHolder(@NonNull final privateChatViewHolder privateChatViewHolder, int i, @NonNull Contact contact) {
                final String usersIDs = getRef(i).getKey();
                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    String profileImage = "default_image";
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {

                            if (dataSnapshot.hasChild("image")) {
                                //String profileName = dataSnapshot.child("username").getValue().toString();
                                //String profileStatus = dataSnapshot.child("status").getValue().toString();
                                profileImage = dataSnapshot.child("image").getValue().toString();

                                //privateChatViewHolder.nameUserTV.setText(profileName);
                                //privateChatViewHolder.statusUserTV.setText(profileStatus);
                                Picasso.get().load(profileImage).placeholder(R.drawable.headshot_7).into(privateChatViewHolder.imageUserIV);
                            }

                            final String profileName = dataSnapshot.child("username").getValue().toString();
                            String profileStatus = dataSnapshot.child("status").getValue().toString();
                            //String profileImage = dataSnapshot.child("image").getValue().toString();

                            privateChatViewHolder.nameUserTV.setText(profileName);
                            privateChatViewHolder.statusUserTV.setText(profileStatus);
                            //Picasso.get().load(profileImage).placeholder(R.drawable.headshot_7).into(contactsViewHolder.imageUserIV);

                            if(dataSnapshot.hasChild("userState")){
                                String state  = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date  = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time  = dataSnapshot.child("userState").child("time").getValue().toString();

                                if(state.equals("online")){
                                    privateChatViewHolder.statusUserTV.setText("online");
                                }else if(state.equals("offline")){
                                    privateChatViewHolder.statusUserTV.setText("last seen: "+  date +" "+ time);
                                }
                            }else{
                                privateChatViewHolder.statusUserTV.setText("offline");
                            }


                            privateChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                        chatIntent.putExtra("visit_user_id", usersIDs);
                                        chatIntent.putExtra("visit_user_name", profileName);
                                        chatIntent.putExtra("visit_user_image", profileImage);
                                        startActivity(chatIntent);
                                    }
                                });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };
        privateChatRV.setAdapter(myPrivateChatAdapter);
        myPrivateChatAdapter.startListening();
    }

     static class privateChatViewHolder extends RecyclerView.ViewHolder{
        TextView nameUserTV;
        TextView statusUserTV;
        CircleImageView imageUserIV;

        public privateChatViewHolder(@NonNull View itemView) {
            super(itemView);

            nameUserTV = (TextView) itemView.findViewById(R.id.user_profile_name);
            statusUserTV = (TextView) itemView.findViewById(R.id.user_profile_status);
            imageUserIV = (CircleImageView) itemView.findViewById(R.id.users_profile_image);
        }
    }
}
