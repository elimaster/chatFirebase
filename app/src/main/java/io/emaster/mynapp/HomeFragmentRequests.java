package io.emaster.mynapp;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import io.emaster.mynapp.model.Contact;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragmentRequests.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragmentRequests#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragmentRequests extends Fragment {

    View requestFragmentView;
    RecyclerView myRequestsRecyclerView;
    DatabaseReference chatRequestsRef;
    DatabaseReference usersRef;
    DatabaseReference contactsRef;
    FirebaseAuth mAuth;
    String currentUserID;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public HomeFragmentRequests() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragmentRequests.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragmentRequests newInstance(String param1, String param2) {
        HomeFragmentRequests fragment = new HomeFragmentRequests();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("ChatRequests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        requestFragmentView = inflater.inflate(R.layout.fragment_home_requests, container, false);
        myRequestsRecyclerView = requestFragmentView.findViewById(R.id.requests_recycler_view);
        myRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return  requestFragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

/*    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onStart() {
        super.onStart();

        final FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(chatRequestsRef.child(currentUserID), Contact.class)
                        .build();

        FirebaseRecyclerAdapter<Contact, HomeFragmentRequests.requestsViewHolder> myContactsAdapter
                = new FirebaseRecyclerAdapter<Contact, HomeFragmentRequests.requestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final HomeFragmentRequests.requestsViewHolder contactsViewHolder, int i, @NonNull Contact contact) {
                contactsViewHolder.cancelBtn.setVisibility(View.VISIBLE);
                contactsViewHolder.acceptBtn.setVisibility(View.VISIBLE);

                final String usersIDs = getRef(i).getKey();
                DatabaseReference getTypeRef = getRef(i).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Log.d("DATA", dataSnapshot.toString());
                           String get_type =  dataSnapshot.getValue().toString();
                            Log.d("DATA1", get_type);
                           if(get_type.equals("received")){
                                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Log.d("DATA_2", dataSnapshot.toString());
                                        if(dataSnapshot.hasChild("image")){
                                            //String profileName = dataSnapshot.child("username").getValue().toString();
                                            //String profileStatus = dataSnapshot.child("status").getValue().toString();
                                            String profileImage = dataSnapshot.child("image").getValue().toString();

                                            //contactsViewHolder.nameUserTV.setText(profileName);
                                            //contactsViewHolder.statusUserTV.setText(profileStatus);
                                            Picasso.get().load(profileImage).placeholder(R.drawable.headshot_7).into(contactsViewHolder.imageUserIV);
                                        }
                                            String profileName = dataSnapshot.child("username").getValue().toString();
                                            String profileStatus = dataSnapshot.child("status").getValue().toString();
                                            //String profileImage = dataSnapshot.child("image").getValue().toString();

                                            contactsViewHolder.nameUserTV.setText(profileName);
                                            contactsViewHolder.statusUserTV.setText(profileStatus);
                                            //Picasso.get().load(profileImage).placeholder(R.drawable.headshot_7).into(contactsViewHolder.imageUserIV);


                                        contactsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence optionsChar[] = new CharSequence[]{
                                                        "Accept",
                                                        "Cancel"
                                                };
                                                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                                                        .setTitle("Delete entry")
                                                        .setItems(optionsChar, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                if(i == 0){//accept
                                                                    contactsRef.child(currentUserID).child(usersIDs)
                                                                            .child("contact").setValue("saved")
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        contactsRef.child(usersIDs).child(currentUserID)
                                                                                                .child("contact").setValue("saved")
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful()){
                                                                                                            chatRequestsRef.child(currentUserID).child(usersIDs)
                                                                                                                    .removeValue()
                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                            if(task.isSuccessful()){
                                                                                                                                chatRequestsRef.child(usersIDs).child(currentUserID)
                                                                                                                                        .removeValue()
                                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                            @Override
                                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                                if(task.isSuccessful()){
                                                                                                                                                    showToastMessage("new contact saved");
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
                                                                if(i == 1){//cancel
                                                                    contactsRef.child(currentUserID).child(usersIDs)
                                                                            .child("contact").setValue("saved")
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        contactsRef.child(usersIDs).child(currentUserID)
                                                                                                .child("contact").setValue("saved")
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful()){
                                                                                                            chatRequestsRef.child(currentUserID).child(usersIDs)
                                                                                                                    .removeValue()
                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                            if(task.isSuccessful()){
                                                                                                                                chatRequestsRef.child(usersIDs).child(currentUserID)
                                                                                                                                        .removeValue()
                                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                            @Override
                                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                                if(task.isSuccessful()){
                                                                                                                                                    showToastMessage("contact deleted");
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
                                                            }
                                                        });
                                                dialog.show();
                                            }


                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
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
            public HomeFragmentRequests.requestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                HomeFragmentRequests.requestsViewHolder viewHolder = new HomeFragmentRequests.requestsViewHolder(view);
                return viewHolder;
            }
        };

        myRequestsRecyclerView.setAdapter(myContactsAdapter);
        myContactsAdapter.startListening();
    }

    private static class requestsViewHolder extends RecyclerView.ViewHolder{
        TextView nameUserTV;
        TextView statusUserTV;
        CircleImageView imageUserIV;
        Button acceptBtn;
        Button cancelBtn;

        public requestsViewHolder(@NonNull View itemView) {
            super(itemView);

            nameUserTV = (TextView) itemView.findViewById(R.id.user_profile_name);
            statusUserTV = (TextView) itemView.findViewById(R.id.user_profile_status);
            imageUserIV = (CircleImageView) itemView.findViewById(R.id.users_profile_image);
            acceptBtn = itemView.findViewById(R.id.accept_request_btn);
            cancelBtn = itemView.findViewById(R.id.accept_cancel_btn);
        }
    }

    private void showToastMessage(String message) {
        Toast toast= Toast.makeText(getContext(),
                message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
