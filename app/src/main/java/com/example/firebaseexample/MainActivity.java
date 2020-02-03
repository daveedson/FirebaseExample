package com.example.firebaseexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private EditText edtEmail,edtUsername,edtPassword;
    private Button btnSignUp,btnLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        mAuth = FirebaseAuth.getInstance();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();

            }
        });


    }

    @Override
    public void onStart() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        super.onStart();
        if (currentUser!=null){

            // Transition to next activity
            transitionToSocialMediaActivity();
        }


    }

    private void signUp(){


        if (edtEmail.getText().toString().equals("")||edtPassword.getText().toString().equals("")
                ||edtUsername.getText().toString().equals("")){

            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();


        }else {

            mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString()
                    ,edtPassword.getText().toString()).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){

                        Toast.makeText(MainActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();

                        FirebaseDatabase .getInstance().getReference().child("my_users").child(task.getResult().getUser()
                                .getUid()).child("username").
                                setValue(edtUsername.getText().toString());
                    }else{

                        Toast.makeText(MainActivity.this, "Sign Up failed", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }



    }

    private void Login(){


        if (edtUsername.getText().toString().equals("")||edtEmail.getText().toString().equals("")
                ||edtPassword.getText().toString().equals("")){

            Toast.makeText(this, " Enter all Fields ", Toast.LENGTH_SHORT).show();
        }else{


            mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){

                                transitionToSocialMediaActivity();
                                Toast.makeText(MainActivity.this, "Signin Successful", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(MainActivity.this, "Signin failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }
    private void transitionToSocialMediaActivity(){

        Intent intent = new Intent(MainActivity.this,SocialMediaActivity.class);
        startActivity(intent);
    }

}
