package com.example.eliezer.jchatapp;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Requests extends Fragment {

    private RecyclerView requestList;
    private View mView;
    private FirebaseAuth mAuth;
    private DatabaseReference mRequestDatabase;
    private DatabaseReference mUsersDatabase;
    private  String mCurrentUser;

    public Requests() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_requests, container, false);

        requestList = (RecyclerView)mView.findViewById(R.id.requests_list);

        mAuth = FirebaseAuth.getInstance();

        mCurrentUser = mAuth.getCurrentUser().getUid().toString();

        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friends_request").child(mCurrentUser);
        mRequestDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        requestList.setHasFixedSize(true);
        requestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return  mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<RequestClass,RequestsViewHolder> requestsRecyclerAdapter = new FirebaseRecyclerAdapter <RequestClass, RequestsViewHolder>(
                RequestClass.class,
                R.layout.request_single_layout,
                RequestsViewHolder.class,
                mRequestDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestsViewHolder viewHolder, RequestClass model, int position) {

                final String list_request = getRef(position).getKey();

                mUsersDatabase.child(list_request).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String name = dataSnapshot.child("name").getValue().toString();
                        String notification = name+" sent you a friend request";
                        String requestThumb = dataSnapshot.child("thumbnail").getValue().toString();


                        viewHolder.setProfile(requestThumb,getContext());
                        viewHolder.setName(name);
                        viewHolder.setStatusNotification(notification);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

        };
        requestList.setAdapter(requestsRecyclerAdapter);
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageView request_prof;
        public RequestsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name){
            TextView request_username = (TextView)mView.findViewById(R.id.request_username);
            request_username.setText(name);
        }
        public void setProfile(String thumb, Context ctx){
            request_prof = (ImageView)mView.findViewById(R.id.request_image_profile);
            //Picasso.with(ctx).load(thumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(request_prof);
            Glide
                    .with(ctx)
                    .load(thumb).apply(new RequestOptions()
                    .placeholder(R.drawable.default_avatar)
                    .dontAnimate()
                    .dontTransform())
                    .into(request_prof);
        }

        public void setStatusNotification(String notification){
            TextView status = (TextView)mView.findViewById(R.id.request_notifications);
            status.setText(notification);
        }
    }
}
