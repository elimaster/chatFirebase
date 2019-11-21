package io.emaster.mynapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class HomeFragmentContacts  extends Fragment {

    View contactsView;
    RecyclerView myContactsRecyclerView;

    DatabaseReference contactsRef;
    DatabaseReference usersRef;
    FirebaseAuth mAuth;
    String currentUserID;

    public static HomeFragmentContacts  newInstance() {

        return new HomeFragmentContacts();
    }

    // @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, /*@Nullable*/ ViewGroup container, Bundle savedInstanceState) {
        contactsView = inflater.inflate(R.layout.fragment_home_contacts, container, false);

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        //currentUserID = mAuth.getCurrentUser().getUid();

        myContactsRecyclerView = contactsView.findViewById(R.id.contacts_recycler_view);
        myContactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        contactsView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUserID = mAuth.getCurrentUser().getUid();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(contactsRef, Contact.class)
                .build();

        FirebaseRecyclerAdapter<Contact, contactsViewHolder> myContactsAdapter
                = new FirebaseRecyclerAdapter<Contact, contactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final contactsViewHolder contactsViewHolder, int i, @NonNull Contact contact) {
                String usersIDs = getRef(i).getKey();
                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(dataSnapshot.hasChild("image")){
                                String profileName = dataSnapshot.child("username").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();
                                String profileImage = dataSnapshot.child("image").getValue().toString();

                                contactsViewHolder.nameUserTV.setText(profileName);
                                contactsViewHolder.statusUserTV.setText(profileStatus);
                                Picasso.get().load(profileImage).placeholder(R.drawable.headshot_7).into(contactsViewHolder.imageUserIV);
                            }else{
                                String profileName = dataSnapshot.child("username").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();
                                //String profileImage = dataSnapshot.child("image").getValue().toString();

                                contactsViewHolder.nameUserTV.setText(profileName);
                                contactsViewHolder.statusUserTV.setText(profileStatus);
                                //Picasso.get().load(profileImage).placeholder(R.drawable.headshot_7).into(contactsViewHolder.imageUserIV);
                            }

                            if(dataSnapshot.hasChild("userState")){
                                String state  = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date  = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time  = dataSnapshot.child("userState").child("time").getValue().toString();

                                if(state.equals("online")){
                                    contactsViewHolder.onlineIcon.setVisibility(View.VISIBLE);
                                }else if(state.equals("offline")){
                                    contactsViewHolder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }else{
                                contactsViewHolder.onlineIcon.setVisibility(View.INVISIBLE);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public contactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                contactsViewHolder viewHolder = new contactsViewHolder(view);
                return viewHolder;
            }
        };

        myContactsRecyclerView.setAdapter(myContactsAdapter);
        myContactsAdapter.startListening();

    }

    private static class contactsViewHolder extends RecyclerView.ViewHolder{
        TextView nameUserTV;
        TextView statusUserTV;
        CircleImageView imageUserIV;
        ImageView onlineIcon;

        public contactsViewHolder(@NonNull View itemView) {
            super(itemView);

            nameUserTV = (TextView) itemView.findViewById(R.id.user_profile_name);
            statusUserTV = (TextView) itemView.findViewById(R.id.user_profile_status);
            imageUserIV = (CircleImageView) itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }


}
