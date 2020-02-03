package com.example.firebaseexample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class SocialMediaActivity extends AppCompatActivity {
    private ImageView postImageView;
    private Button btnPostImage;
    private EditText edtdes;
    private ListView UserListView;
    private FirebaseAuth mAuth;

    private Bitmap bitmap;

    private String ImageIdentifier;

    private ArrayList<String> usernames;
    private ArrayAdapter Adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media);

        mAuth = FirebaseAuth.getInstance();

        postImageView = findViewById(R.id.imgpostImagview);
        btnPostImage = findViewById(R.id.btncreatePost);
        edtdes = findViewById(R.id.edtdes);
        UserListView = findViewById(R.id.UserListview);

        usernames = new ArrayList<>();
        Adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,usernames);

        UserListView.setAdapter(Adapter);
        postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectImage();



            }

        });


        btnPostImage.setOnClickListener(new View.OnClickListener() {
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

            case R.id.LogoutItem:
                mAuth.signOut();
                finish();


                break;

        }



        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        logOut();

    }


    private void  selectImage(){

        if (Build.VERSION.SDK_INT < 23){

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,1000);

        }else if (Build.VERSION.SDK_INT >= 23){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                requestPermissions(new  String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1000);

            }else{

                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1000);
            }
        }


    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == 1000){
            if (grantResults.length> 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                selectImage();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1000 && resultCode == RESULT_OK && data!=null){
            Uri chosenImageData = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),chosenImageData);
                postImageView.setImageBitmap(bitmap);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void logOut(){
        mAuth.signOut();
        finish();
    }



    //Uploading Image to server

    private void uploadImageToServer(){

        if (bitmap!=null ) {



            final ProgressDialog progressDialog = new ProgressDialog(SocialMediaActivity.this);
            progressDialog.setMessage("Uploading..");
            progressDialog.show();

            // Get the data from an ImageView as bytes
            postImageView.setDrawingCacheEnabled(true);
            postImageView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) postImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            ImageIdentifier = UUID.randomUUID().toString() + ".png";


            UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("my_images").child(ImageIdentifier).putBytes(data);


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
                    // ...

                    Toast.makeText(SocialMediaActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                    edtdes.setVisibility(View.VISIBLE);


                    //reteriving  users from database
                    FirebaseDatabase.getInstance().getReference().child("my_users").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            String username = (String) dataSnapshot.child("username").getValue();
                            usernames.add(username);
                            Adapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }




            });


        }


    }
}
