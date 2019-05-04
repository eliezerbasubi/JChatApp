package com.example.eliezer.jchatapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView Userslist;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        toolbar = (Toolbar)findViewById(R.id.all_users_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All JChat Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Userslist = (RecyclerView)findViewById(R.id.users_list);
        Userslist.setHasFixedSize(true);
        Userslist.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter <Users, UsersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setImage(model.getThumbnail(),getApplicationContext());

                //WHEN YOU CLICK ON SOMEONE'S NAME IT OPENS HIS PROFILE

                final String user_id = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(AllUsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };

        Userslist.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends  RecyclerView.ViewHolder{

        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name){

            TextView userNameView = (TextView)mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setStatus(String status){

            TextView userNameView = (TextView)mView.findViewById(R.id.user_single_status);
            userNameView.setText(status);
        }

        public void setImage(String thumbnail, Context context){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_picture);
            //Picasso.with(context).load(thumbnail).placeholder(R.drawable.default_avatar).into(userImageView);
            Glide
                    .with(context)
                    .load(thumbnail).apply(new RequestOptions()
                    .placeholder(R.drawable.default_avatar)
                    .dontAnimate()
                    .dontTransform())
                    .into(userImageView);
        }
    }
}
