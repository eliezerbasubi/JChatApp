package com.example.eliezer.jchatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import android.widget.ImageView;

public class Full_image extends AppCompatActivity {

    private Toolbar toolbar;
    private DatabaseReference mRootRef;
    private ImageView profileImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);


        String full_user_name = getIntent().getStringExtra("pass_name");
        String user_id = getIntent().getStringExtra("user_id");

        profileImageView = (ImageView)findViewById(R.id.full_profile_image);

        toolbar = (Toolbar)findViewById(R.id.full_image_layout_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(full_user_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        mRootRef.child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String image = dataSnapshot.child("image").getValue().toString();
                //Picasso.with(Full_image.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                 //       .placeholder(R.drawable.default_avatar).into(profileImageView);

                Glide
                        .with(Full_image.this)
                        .load(image).apply(new RequestOptions()
                        .placeholder(R.drawable.default_avatar)
                        .dontAnimate()
                        .dontTransform())
                        .into(profileImageView);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
