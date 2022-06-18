package com.manuni.profilewithfirebaseneatrootsassignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.profilewithfirebaseneatrootsassignment.databinding.ActivityProfileBinding;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding binding;
    String name, email,password;
    private DatabaseReference databaseReference;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();



        databaseReference = FirebaseDatabase.getInstance().getReference();



        databaseReference.child("User").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User userData = snapshot.getValue(User.class);
                    binding.name.setText(userData.getName());
                    binding.emailETA.setText(userData.getEmail());
                    binding.passETA.setText(userData.getPassword());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        databaseReference.child("category").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User myUser = snapshot.getValue(User.class);
                    Glide.with(getApplicationContext()).load(myUser.getImage()).placeholder(R.drawable.mpl6).into(binding.imageView2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.button.setText("Updating");
            }
        });
        binding.eUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.eUpdate.setText("Updating");
            }
        });
        binding.button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.button3.setText("Updating");
            }
        });

        binding.signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.button.setText("Update");
                binding.eUpdate.setText("Update");
                binding.button3.setText("Update");
                //User user = new User(name,email,password);
                name = binding.name.getText().toString().trim();
                email = binding.emailETA.getText().toString().trim();
                password = binding.passETA.getText().toString().trim();

                String key = auth.getUid();

                HashMap data = new HashMap();
                data.put("name",name);
                data.put("email",email);
                data.put("password",password);

                databaseReference.child("User").child(key).updateChildren(data).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        startActivity(new Intent(ProfileActivity.this,AnotherActivity.class));
                        Toast.makeText(ProfileActivity.this, "Updated.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });




    }
}