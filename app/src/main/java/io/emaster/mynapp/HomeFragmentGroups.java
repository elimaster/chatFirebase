package io.emaster.mynapp;

import android.icu.text.Edits;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class HomeFragmentGroups  extends Fragment {
    private View groupFragmentView;
    private ListView groupListView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> arrayList = new ArrayList<>();

    DatabaseReference groupRef;

    public static HomeFragmentGroups  newInstance() {

        return new HomeFragmentGroups();
    }

    // @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, /*@Nullable*/ ViewGroup container, Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.fragment_home_groups, container, false);

        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        initializeFields();
        retriveAndDisplayGroups();

        groupFragmentView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        return groupFragmentView;
    }

    private void retriveAndDisplayGroups() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                arrayList.clear();
                arrayList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields() {
        groupListView = (ListView) groupFragmentView.findViewById(R.id.list_view_groups);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arrayList);
        groupListView.setAdapter(arrayAdapter);
    }
}
