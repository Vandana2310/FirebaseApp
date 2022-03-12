package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SocialMediaActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private FirebaseAuth mAuth;
    ImageView imgPost;
    Button btnSendPost;
    EditText edtDes;
    ListView listView;

    private Bitmap bitmap;
    private String imageIdentifier;

    private ArrayList<String> usernames;
    private ArrayAdapter adapter;
    private ArrayList<String> uids;

    private String imageDownloadLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media);
        mAuth = FirebaseAuth.getInstance();

        imgPost=findViewById(R.id.imgPost);
        btnSendPost=findViewById(R.id.btnPostImage);
        edtDes=findViewById(R.id.edtDes);
        listView=findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        usernames= new ArrayList<>();
        adapter= new ArrayAdapter(this, android.R.layout.simple_list_item_1,usernames);
        listView.setAdapter(adapter);
        uids=new ArrayList<>();


        imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btnSendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               uploadImageToServer();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.logoutItem:
                mAuth.signOut();
                logout();
                break;

            case R.id.viewPosts:
                Intent intent=new Intent(SocialMediaActivity.this,ViewSendPostActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        logout();
    }

    private void logout(){
        mAuth.signOut();
        finish();
    }

    private void selectImage() {
        if (Build.VERSION.SDK_INT < 23) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1000);
        } if (Build.VERSION.SDK_INT >= 23)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=  PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);

            } else {

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1000);
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            selectImage();

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
            Uri chosenImageData = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), chosenImageData);
                imgPost.setImageBitmap(bitmap);


            } catch (Exception e) {

                e.printStackTrace();
            }

        }

    }

    private void uploadImageToServer(){

        if(bitmap!=null) {
            // Get the data from an ImageView as bytes
            imgPost.setDrawingCacheEnabled(true);
            imgPost.buildDrawingCache();
            //  Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap(); // we already have this
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            imageIdentifier = UUID.randomUUID() + ".png"; //for  automatically  generating unique identifier for the image
            UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("my_images").child(imageIdentifier).putBytes(data); //mountainsRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(SocialMediaActivity.this, exception.toString(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    Toast.makeText(SocialMediaActivity.this, "upload successful", Toast.LENGTH_SHORT).show();

                    edtDes.setVisibility(View.VISIBLE);

                    FirebaseDatabase.getInstance().getReference().child("my_users").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            uids.add(snapshot.getKey());
                            String username=(String) snapshot.child("username").getValue();
                            usernames.add(username);
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
                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            if(task.isSuccessful()){
                                imageDownloadLink=task.getResult().toString();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        HashMap<String,String> dataMap= new HashMap<>();
        dataMap.put("fromWhom",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        dataMap.put("imageIdentifier",imageIdentifier);
        dataMap.put("imageLink",imageDownloadLink);
        dataMap.put("Des",edtDes.getText().toString());

        FirebaseDatabase.getInstance().getReference().child("my_user")
                .child(uids.get(position)).child("received_posts").push().setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    Toast.makeText(SocialMediaActivity.this, "data sent", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}