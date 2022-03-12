package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText edtEmail,edtUsername,edtPassword;
    private Button btnSignUp,btnSignIn;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtEmail=findViewById(R.id.edtEmail);
        edtUsername=findViewById(R.id.edtUsername);
        edtPassword=findViewById(R.id.edtPassword);
        btnSignIn=findViewById(R.id.btnSignIn);
        btnSignUp=findViewById(R.id.btnSignUp);

        mAuth = FirebaseAuth.getInstance();

       btnSignUp.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               SignUp();
           }
       });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            //transition to next activity
            transitionToSocialMediaActivity();
        }
    }
    public void SignUp(){
        mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "sign up successful", Toast.LENGTH_SHORT).show();
                            FirebaseDatabase.getInstance().getReference().child("my_users").child(task.getResult().getUser().getUid())
                                    .child("username").setValue(edtUsername.getText().toString());

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(edtUsername.getText().toString())
                                  //  .setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                                    .build();

                            FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                               // Log.d(TAG, "User profile updated.");
                                                Toast.makeText(MainActivity.this, "display name is updated", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            transitionToSocialMediaActivity();
                        }else
                            Toast.makeText(MainActivity.this,"Sign up failed",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void signIn(){
        mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "sign in successful", Toast.LENGTH_SHORT).show();
                    transitionToSocialMediaActivity();

                }
                else
                    Toast.makeText(MainActivity.this, "sign in failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void transitionToSocialMediaActivity(){
        Intent intent=new Intent(MainActivity.this,SocialMediaActivity.class);
        startActivity(intent);
    }
}