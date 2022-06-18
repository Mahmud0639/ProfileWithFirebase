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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.manuni.profilewithfirebaseneatrootsassignment.databinding.ActivitySignUpBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SignUpActivity extends AppCompatActivity {
    private static final int OPEN_CAMERA_CODE = 1;
    private static final int GALLERY_IMAGE_PICK = 2;
    ActivitySignUpBinding binding;
    private String name;
    private String email;
    private String password;
    private ProgressDialog dialog;
    private Uri imgUri;
    private Bitmap imgBitmap;
    StorageReference storageReference, mStorageRef;
    private UploadTask uploadTask;
    private String downloadUrl = "";

    FirebaseAuth auth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        dialog = new ProgressDialog(this);
        dialog.setMessage("Progressing...");
        dialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();


        storageReference = FirebaseStorage.getInstance().getReference();

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                name = binding.nameTxt.getText().toString().trim();
                email = binding.emailET.getText().toString().trim();
                password = binding.passET.getText().toString().trim();

                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            dialog.dismiss();
                            User user = new User(name,email,password);
                            databaseReference.child("User").child(auth.getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    gotoStorage();
                                    Toast.makeText(SignUpActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                    finishAffinity();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(SignUpActivity.this, "Something went  wrong!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else {
                            dialog.dismiss();
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(SignUpActivity.this,new String[]{Manifest.permission.CAMERA},2);
                }
                Intent openCam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(openCam,OPEN_CAMERA_CODE);
            }
        });
        binding.circleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imgPick = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(imgPick,GALLERY_IMAGE_PICK);
            }
        });

        binding.linearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this,MainActivity.class));
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OPEN_CAMERA_CODE && resultCode == RESULT_OK){
           imgBitmap = (Bitmap) data.getExtras().get("data");
           binding.circleImage.setImageBitmap(imgBitmap);

        }else {
            imgUri = data.getData();

            try {
                imgBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imgUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            binding.circleImage.setImageBitmap(imgBitmap);
        }
    }
  /*  private String getFileExtension(Uri uri){
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(getContentResolver().getType(uri));
    }*/
    private void gotoStorage(){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imgBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] finalImage = byteArrayOutputStream.toByteArray();

        mStorageRef = storageReference.child("profile_images").child(finalImage+"."+"jpg");
        uploadTask = mStorageRef.putBytes(finalImage);
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
                                    gotoDatabase(downloadUrl);
                                }
                            });
                        }
                    });
                }
            }
        });

    }
    private void gotoDatabase(String downloadUrl){
        String key = auth.getUid();
        User imUser = new User(downloadUrl,key);
        databaseReference.child("category").child(key).setValue(imUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(SignUpActivity.this, "Data saved properly.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}