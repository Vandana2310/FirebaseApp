package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewSendPostActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    private ListView postListView;
    private ArrayAdapter adapter;
    private ArrayList<String> usernames;
    private FirebaseAuth firebaseAuth;
    private ImageView sendPostImg;
    private TextView txtSendPost;
    private ArrayList<DataSnapshot>  dataSnapshots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_send_post);

        sendPostImg=findViewById(R.id.sendPostImg);
        txtSendPost=findViewById(R.id.txtDescription);

        firebaseAuth=FirebaseAuth.getInstance();

        usernames =new ArrayList<>();
        postListView=findViewById(R.id.postListView);
        adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1, usernames);
        postListView.setAdapter(adapter);
        dataSnapshots=new ArrayList<>();

        postListView.setOnItemClickListener(this);

        FirebaseDatabase.getInstance().getReference().child("my_users").child(firebaseAuth.getCurrentUser().getUid()).child("received_posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                dataSnapshots.add(snapshot);
                String fromWhomUsername=(String) snapshot.child("fromWhom").getValue();
                usernames.add(fromWhomUsername);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DataSnapshot myDataSnapshot= dataSnapshots.get(position);
        String downloadLink= (String) myDataSnapshot.child("imageLink").getValue();

        Picasso.get().load(downloadLink).into(sendPostImg);
        txtSendPost.setText((String)myDataSnapshot.child("Des").getValue());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }
}