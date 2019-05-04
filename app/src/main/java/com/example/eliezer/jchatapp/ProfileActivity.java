package com.example.eliezer.jchatapp;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView user_profile;
    private TextView display_name,numFriends,numMutual;
    private TextView display_status;
    private Button send_request;
    private Button decline_request;
    private DatabaseReference mUserDatabase;
    private DatabaseReference friendsRequestReference;
    private DatabaseReference friendsDatabase;
    private DatabaseReference notificationDatabase,request0ne,requestTwo;
    private DatabaseReference mUserRef;
    private DatabaseReference mRootRef;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    private String current_state;

    private  long numberOffriends;

    private List<Object>l1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user_profile = (ImageView) findViewById(R.id.user_profile);
        display_name = (TextView)findViewById(R.id.user_display_name);
        display_status = (TextView)findViewById(R.id.user_display_status);
        numFriends = (TextView)findViewById(R.id.number_of_friends);
        numMutual =(TextView)findViewById(R.id.number_of_mutual_friends);
        send_request = (Button)findViewById(R.id.send_request);
        decline_request = (Button)findViewById(R.id.decline_request);

        decline_request.setVisibility(View.INVISIBLE);
        decline_request.setEnabled(false);

        current_state = "not_friends";

        mAuth = FirebaseAuth.getInstance();

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());



        final String user_id = getIntent().getStringExtra("user_id").toString();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Profile Loading");
        progressDialog.setMessage("Fetching user profile ....");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mUserDatabase.keepSynced(true);

        friendsRequestReference = FirebaseDatabase.getInstance().getReference().child("Friends_request");
        friendsRequestReference.keepSynced(true);

        friendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsDatabase.keepSynced(true);

        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        notificationDatabase.keepSynced(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        request0ne = FirebaseDatabase.getInstance().getReference().child("Friends").child(mAuth.getCurrentUser().getUid());
        request0ne.keepSynced(true);

        requestTwo = FirebaseDatabase.getInstance().getReference().child("Friends").child(user_id);
        requestTwo.keepSynced(true);


        final List <String> mutualFriend_current = new ArrayList <>();
        final List <String> mutualFriend_from_friends = new ArrayList <>();
       // final List <String> mutualFriend_result = new ArrayList <>();

        request0ne.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                        mutualFriend_current.add(dataSnapshot1.getKey());
                    }
                 }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        requestTwo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    mutualFriend_from_friends.add(snapshot.getKey());

                    //numMutual.setText( String.valueOf(mutualFriend_result));
                   //mutualFriend_from_friends.retainAll(mutualFriend_current);
                   //int res = mutualFriend_from_friends.size();

                    numMutual.setText(String.valueOf(countMatch(mutualFriend_current,mutualFriend_from_friends)));
               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //DISPLAY NUMBER OF FRIENDS AND MUTUAL FRIENDS
        friendsDatabase.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                numberOffriends = dataSnapshot.getChildrenCount();

                numFriends.setText(String.valueOf(numberOffriends));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //COUNT MUTUAL FRIENDS FOR EACH USER



        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name_display = dataSnapshot.child("name").getValue().toString();
                String status_display = dataSnapshot.child("status").getValue().toString();
                String image_display = dataSnapshot.child("image").getValue().toString();

                display_name.setText(name_display);
                display_status.setText(status_display);
                //Picasso.with(ProfileActivity.this).load(image_display).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(user_profile);

                Glide
                        .with(ProfileActivity.this)
                        .load(image_display).apply(new RequestOptions()
                        .placeholder(R.drawable.default_avatar)
                        .dontAnimate()
                        .dontTransform())
                        .into(user_profile);

                //--- ACCEPTING FRIENDS REQUESTS / AND FRIEND LIST ---//


                friendsRequestReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received")){

                                current_state = "req_received";

                                send_request.setText("ACCEPT REQUEST");

                                decline_request.setVisibility(View.VISIBLE);
                                decline_request.setEnabled(true);

                            }else if(req_type.equals("sent")){
                                current_state = "req_sent";

                                send_request.setText("CANCEL REQUEST");

                                decline_request.setVisibility(View.INVISIBLE);
                                decline_request.setEnabled(false);

                            }


                            progressDialog.dismiss();

                        }else{
                            friendsDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){

                                        current_state = "friends";

                                        send_request.setText("DELETE USER");

                                        decline_request.setVisibility(View.INVISIBLE);
                                        decline_request.setEnabled(false);

                                    }

                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        send_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_request.setEnabled(false);

                //-----NOT FRIENDS, WHEN USERS ARE NOT FRIENDS ---
                if (current_state.equals("not_friends")){

                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(user_id).push();

                    String newNotificationId = newNotificationRef.getKey();

                    Map requestMap = new HashMap();


                    HashMap<String, String> hashMapNotifications = new HashMap <>();
                    hashMapNotifications.put("from",currentUser.getUid());
                    hashMapNotifications.put("type","request");

                    requestMap.put("Friends_request/"+currentUser.getUid() +"/"+user_id +"/request_type","sent");
                    requestMap.put("Friends_request/"+user_id +"/"+currentUser.getUid() +"/request_type","received");
                    requestMap.put("Notifications/"+user_id+"/"+newNotificationId, hashMapNotifications);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null){
                                Toast.makeText(ProfileActivity.this,"Sorry,there was an error",Toast.LENGTH_SHORT).show();
                            }

                            send_request.setEnabled(true);
                            current_state = "req_sent";
                            send_request.setText("CANCEL REQUEST");
                        }
                    });
                }

                //--- WHEN USERS ARE ALREADY FRIENDS, CANCEL SENT REQUEST---

                if (current_state.equals("req_sent")){
                    friendsRequestReference.child(currentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener <Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendsRequestReference.child(user_id).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener <Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    send_request.setEnabled(true);

                                    current_state = "not_friends";

                                    send_request.setText("SEND REQUEST");
                                }
                            });
                        }
                    });
                }


                //-- REQUEST RECEIVED STATE --//

                if (current_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/"+currentUser.getUid() +"/"+user_id +"/date",currentDate);
                    friendsMap.put("Friends/"+user_id +"/"+currentUser.getUid() +"/date",currentDate);


                    friendsMap.put("Friends_request/"+currentUser.getUid() +"/"+user_id ,null); //U SENT A REQUEST FRIEND TO
                    friendsMap.put("Friends_request/"+user_id +"/"+currentUser.getUid() ,null); //U HAVE A REQUEST FRIEND FROM



                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null){


                                send_request.setEnabled(true);
                                current_state = "friends";
                                send_request.setText("DELETE USER");

                                decline_request.setVisibility(View.INVISIBLE);
                                decline_request.setEnabled(false);

                            }else{
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }


                /*----- UNFRIENDS USER */
                if (current_state.equals("friends")){
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/"+currentUser.getUid() +"/"+user_id,null);
                    unfriendMap.put("Friends/"+user_id +"/"+currentUser.getUid(),null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null){


                                current_state = "not_friends";
                                send_request.setText("SEND REQUEST");

                                decline_request.setVisibility(View.INVISIBLE);
                                decline_request.setEnabled(false);

                            }else{
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }

                            send_request.setEnabled(true);

                        }
                    });
                }
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserRef.child("online").setValue(true);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    public int countMatch(List <String> a, List <String> b) {
        List<String> list1 = new ArrayList(Arrays.asList(a));
        List<String> list2 = new ArrayList(Arrays.asList(b));
        //List<String> list2 = new ArrayList <>(Arrays.asList(b));
        list1.retainAll(list2);
        return list1.size();
    }
}
