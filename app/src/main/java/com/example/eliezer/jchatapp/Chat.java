package com.example.eliezer.jchatapp;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class Chat extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mChatDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;


    public Chat() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chat, container, false);

        mConvList = (RecyclerView) mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

        mChatDatabase = FirebaseDatabase.getInstance().getReference().child("Chat");

        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);


        // Inflate the layout for this fragment
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Conv, ConvViewHolder> firebaseConvAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(
                Conv.class,
                R.layout.users_single_layout,
                ConvViewHolder.class,
                conversationQuery
        ) {
            @Override
            protected void populateViewHolder(final ConvViewHolder convViewHolder, final Conv conv, int i) {



                final String list_user_id = getRef(i).getKey();

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                mMessageDatabase.keepSynced(true);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String data = dataSnapshot.child("message").getValue().toString();
                        String messageType = dataSnapshot.child("type").getValue().toString();
                        convViewHolder.setMessage(data, conv.isSeen()); //Set if the message is seen or not

                        //IF THE USER SENT AN IMAGE IT SHOULD NOT DISPLAY IT BUT IT SHOULD DISPLAY A TEXT
                        if (!messageType.equals("text")){
                            convViewHolder.setMessage("Sent an attachment",conv.isSeen());
                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //COUNT UNREAD MESSAGES
                mChatDatabase.child(list_user_id).child(mCurrent_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        /*String numberUnreadMessages =  dataSnapshot.child("counter").getValue().toString();
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("unread_messages",numberUnreadMessages);
                        editor.commit();*/
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mUsersDatabase.keepSynced(true);
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String userThumb = dataSnapshot.child("thumbnail").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            convViewHolder.setUserOnline(userOnline);

                        }

                        convViewHolder.setName(userName);
                        convViewHolder.setUserImage(userThumb, getContext());

                        //IF THE MESSAGE IS IMAGE TYPE IT SHOULD DISPLAY USER SENT AN IMAGE


                        convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);

                                //SEND RECEIVER ID TO MAIN ACTIVITY
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("unread_messages",list_user_id);
                                editor.commit();

                            }
                        });

                        //WHEN THE USER CLICKS ON FRIENDS IMAGE IT SHOULD DISPLAY IT AS AN DIALOG BOX
                        convViewHolder.userImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                convViewHolder.CreateAlertBox(userThumb,list_user_id,userName);
                            }
                        });

                        //WHEN  USER CLICKS ON FRIENDS ICON IT SHOULD OPEN FRIENDS FRAGEMENT


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mConvList.setAdapter(firebaseConvAdapter);

    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public CircleImageView userImageView;
        public ImageView popupImage,friendsView,messageView,profileView;
        public TextView userStatusView;


        public ConvViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, boolean isSeen){

            userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(message);

            if(!isSeen){
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }

        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx){

             userImageView = (CircleImageView) mView.findViewById(R.id.user_single_picture);
            //Picasso.with(ctx).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(userImageView);

            Glide
                    .with(ctx)
                    .load(thumb_image).apply(new RequestOptions()
                    .placeholder(R.drawable.default_avatar)
                    .dontAnimate()
                    .dontTransform())
                    .into(userImageView);

        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }

        public void CreateAlertBox(String userThumb, final String userID, final String user_name){

            final AlertDialog.Builder alertadd = new AlertDialog.Builder(mView.getContext());
            LayoutInflater factory = LayoutInflater.from(mView.getContext());



            final View view = factory.inflate(R.layout.popup_layout, null);

            alertadd.setView(view);

            final  AlertDialog dialog = alertadd.create();

            //alertadd.show();

            popupImage = (ImageView)view.findViewById(R.id.dialog_imageview);
            messageView = (ImageView)view.findViewById(R.id.send_to_messages);
            friendsView = (ImageView)view.findViewById(R.id.send_to_friends);
            profileView = (ImageView)view.findViewById(R.id.send_to_profile);

           // Picasso.with(popupImage.getContext()).load(userThumb).placeholder(R.drawable.default_avatar).into(popupImage);
            Glide
                    .with(popupImage.getContext())
                    .load(userThumb).apply(new RequestOptions()
                    .placeholder(R.drawable.default_avatar)
                    .dontAnimate()
                    .dontTransform())
                    .into(popupImage);

            messageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent chatIntent = new Intent(mView.getContext(), ChatActivity.class);
                    chatIntent.putExtra("user_id", userID);
                    chatIntent.putExtra("user_name", user_name);
                    mView.getContext().startActivity(chatIntent);
                    dialog.dismiss();
                }
            });


            friendsView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.viewPager.arrowScroll(View.FOCUS_RIGHT);
                    dialog.dismiss();


                }
            });

            profileView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent profileIntent = new Intent(mView.getContext(),ProfileActivity.class);
                    profileIntent.putExtra("user_id",userID);
                    mView.getContext().startActivity(profileIntent);
                    dialog.dismiss();

                    //Toast.makeText(mView.getContext(),"Profiles",Toast.LENGTH_SHORT).show();
                }
            });

            popupImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent chatIntent = new Intent(mView.getContext(), Full_image.class);
                    chatIntent.putExtra("user_id", userID);
                    chatIntent.putExtra("pass_name", user_name);
                    mView.getContext().startActivity(chatIntent);
                    dialog.dismiss();
                }
            });

            dialog.show();

        }


    }



}
