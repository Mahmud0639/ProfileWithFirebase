package com.manuni.profilewithfirebaseneatrootsassignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.manuni.profilewithfirebaseneatrootsassignment.databinding.ActivityMainBinding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int IMAGE_CODE_GALERY = 1;
    ActivityMainBinding binding;
    private String email;
    private String password;
    FirebaseAuth auth;
    private ProgressDialog dialog;
    private Uri imgUri;
    private Bitmap imgBitmap;
    StorageReference storageReference,mStorageRef;
    private UploadTask uploadTask;
    private String downloadUrl = "";
    DatabaseReference databaseReference;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        dialog = new ProgressDialog(this);
        dialog.setMessage("Processing...");
        dialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("category");

        binding.profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });



        binding.signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                email = binding.emailET.getText().toString().trim();
                password = binding.passET.getText().toString().trim();

                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            gotoStorage();
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "Logged in.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        }else {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        binding.donthaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,SignUpActivity.class));
            }
        });


    }
    private void openGallery(){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }
        Intent imagePick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(imagePick,IMAGE_CODE_GALERY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CODE_GALERY && resultCode == RESULT_OK){
            imgUri = data.getData();
            try {
                imgBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imgUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            binding.profilePic.setImageBitmap(imgBitmap);

        }
    }
    private String getFileExtension(Uri uri){
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(getContentResolver().getType(uri));
    }
    private void gotoStorage(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imgBitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
        byte[] myImage = baos.toByteArray();

       mStorageRef = storageReference.child(auth.getUid()).child(myImage+"."+getFileExtension(imgUri)+System.currentTimeMillis());
        uploadTask = mStorageRef.putBytes(myImage);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUrl = String.valueOf(uri);
                                    gotoDatabase();
                                }
                            });
                        }
                    });
                }
            }
        });


    }
    private void gotoDatabase(){
        key = auth.getUid();
       User user = new User(downloadUrl,key);
        databaseReference.child(key).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(MainActivity.this, "Data saved.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser()!=null){
            startActivity(new Intent(MainActivity.this,ProfileActivity.class));
            finishAffinity();
        }
    }
}