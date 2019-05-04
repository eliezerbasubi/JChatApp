package com.example.eliezer.jchatapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import id.zelory.compressor.Compressor;
//import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbarView;
    private String mChatUser;
    private String mUsername;
    private TextView mTitleView;
    private TextView mLastSeenView;
    private EmojiconEditText mMessageInput;
    private CircleImageView profileImageView;
    private ImageButton sendMessageBtn;
    private ImageButton addFeatures,emojiButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private DatabaseReference mMessagesReference;
    private String mCurrentUserId;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private static final int GALLERY_PICK = 1;

    // Storage Firebase
    private StorageReference mImageStorage;


    //New Solution
    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";

    private  byte thumb_byte [];


    private int counterMsg= 0;
    private DatabaseReference mRootRef;


    EmojIconActions emojIconActions;
    View rootView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }


        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("user_id");
        mUsername = getIntent().getStringExtra("user_name");

        mToolbarView = (Toolbar)findViewById(R.id.messages_app_bar_layout);
        setSupportActionBar(mToolbarView);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        //getSupportActionBar().setTitle(mUsername);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);



        mTitleView = (TextView)findViewById(R.id.custom_chat_username);
        mLastSeenView = (TextView)findViewById(R.id.last_seen_display);
        profileImageView = (CircleImageView)findViewById(R.id.custom_bar_image);

        //mAdapter = new MessageAdapter(messagesList);

        sendMessageBtn = (ImageButton)findViewById(R.id.send_message);
        addFeatures = (ImageButton)findViewById(R.id.add_feature);
        emojiButton =(ImageButton)findViewById(R.id.add_emoji);
        mMessageInput = (EmojiconEditText) findViewById(R.id.input_message);
        rootView = (RelativeLayout)findViewById(R.id.activity_chat);

        //SET UP EMOJIS
        emojIconActions = new EmojIconActions(getApplicationContext(),rootView,mMessageInput,emojiButton);
        emojIconActions.ShowEmojIcon();


       // mMessagesList = (RecyclerView)findViewById(R.id.messages_list);
        // =(SwipeRefreshLayout)findViewById(R.id.message_swipe_layout);
        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mAdapter = new MessageAdapter(this,mCurrentUserId,messagesList);

        mMessagesList.setAdapter(mAdapter);

        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().getReference();

        //SET THE CHAT TO TRUE IF THE MESSAGE IS SEEN

        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("counter").setValue(0);

        //CHANGE MESSAGE STATUS TO READ

        loadMessages();


        mTitleView.setText(mUsername);


        mRootRef.keepSynced(true);
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                //Picasso.with(ChatActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                      //  .placeholder(R.drawable.default_avatar).into(profileImageView);
                Glide
                        .with(ChatActivity.this)
                        .load(image).apply(new RequestOptions()
                        .placeholder(R.drawable.default_avatar)
                        .dontAnimate()
                        .dontTransform())
                        .into(profileImageView);

                if(online.equals("true")) {

                    mLastSeenView.setText("online");

                } else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    mLastSeenView.setText(lastSeenTime);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //CREATE CHAT FOR EACH USER

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("counter",counterMsg);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //SEND MESSAGE TO FIREBASE WHEN CLICKED
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        //REFRESH MESSAGES EACH TIME USER LOADS OLD MESSAGES

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                itemPos = 0;

                loadMoreMessages();


            }
        });


        //SEND PICTURES AND OTHER FEATURES
        addFeatures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Upload Profile"),GALLERY_PICK);*/
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(ChatActivity.this);

            }
        });

    }// END OF ONCREATE METHOD

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {



                Uri imageUri = result.getUri();

                final File thumbFilePath = new File(imageUri.getPath());

                if (thumbFilePath != null) {

                    try {
                        Bitmap thumb_file = new Compressor(ChatActivity.this)
                                .setMaxWidth(200)
                                .setMaxHeight(200)
                                .setQuality(55)
                                .compressToBitmap(thumbFilePath);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        thumb_file.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        thumb_byte = baos.toByteArray();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
                final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

                DatabaseReference user_message_push = mRootRef.child("messages")
                        .child(mCurrentUserId).child(mChatUser).push();

                final String push_id = user_message_push.getKey();


                final StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");


                filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener <UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task <UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            //String download_url = task.getResult().getDownloadUrl().toString();
                            final String download_link = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = filepath.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener <UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task <UploadTask.TaskSnapshot> task) {

                                    if (task.isSuccessful()) {
                                        Map messageMap = new HashMap();
                                        messageMap.put("message", download_link);
                                        messageMap.put("seen", false);
                                        messageMap.put("type", "image");
                                        messageMap.put("time", ServerValue.TIMESTAMP);
                                        messageMap.put("from", mCurrentUserId);

                                        Map messageUserMap = new HashMap();
                                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                                        mMessageInput.setText("");


                                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                                if (databaseError != null) {

                                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                                    Toast.makeText(ChatActivity.this, "Image has been successfully sent", Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                                    } else {
                                        Toast.makeText(ChatActivity.this, "The image is not sent ", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }

                    }
                });
            }//End of crop image
        }

    }

    //LOAD MORE MESSAGES / OLD MESSAGES
    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){

                    messagesList.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;
                    mRefreshLayout.setEnabled(false);
                }


                if(itemPos == 1) {

                    mLastKey = messageKey;

                }


                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10, 0);

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

    }


    //LOAD DATA MESSAGES
    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if(itemPos == 1){

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);

                mRefreshLayout.setRefreshing(false);

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

    }

    //Send messages

    private void sendMessage() {


        String message = mMessageInput.getText().toString();

        if (!TextUtils.isEmpty(message)) {

            int getCounter =counterMsg++;

            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mMessageInput.setText("");


            //SENDER
            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("counter").setValue(getCounter);
            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            //RECEIVER
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("counter").setValue(counterMsg);
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    }

                }
            });

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

        }else{
            mUserRef.child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

}
