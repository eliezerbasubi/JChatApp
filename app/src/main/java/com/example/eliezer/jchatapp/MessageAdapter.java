package com.example.eliezer.jchatapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import android.text.format.DateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

/**
 * Created by AkshayeJH on 24/07/17.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    Context context;
    String mUserId;

    public MessageAdapter(Context context,String mUserId,List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

        this.mUserId = mUserId;

        this.context = context;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.message_single_layout, parent, false);

        MessageViewHolder viewHolder = new MessageViewHolder(contactView);
        return viewHolder;

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView displayTime,displayTimeSender;
        public CircleImageView profileImage,sendImageProfile,receiverImageProfile;
        public EmojiconTextView displaySenderMessage,messageText;
        public ImageView receiverImageMessage,senderImageMessage, imageOther;
        public LinearLayout sender,receiver,receiverImageLayout,senderImageLayout;

        public MessageViewHolder(View view) {
            super(view);

            //Receiver part
            messageText = (EmojiconTextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            displayTime = (TextView) view.findViewById(R.id.time_text_layout);

            //Receiver Image part
            receiverImageProfile = (CircleImageView)view.findViewById(R.id.receiverImageProfile);
            receiverImageMessage = (ImageView)view.findViewById(R.id.img_chat_receiver);

            //Sender part
            displaySenderMessage = (EmojiconTextView) view.findViewById(R.id.sender_messages_display);
            displayTimeSender = (TextView)view.findViewById(R.id.rec_time_layout);
            imageOther = (ImageView)view.findViewById(R.id.receiver_display_image_layout);

            //Sender Image part
            sendImageProfile = (CircleImageView)view.findViewById(R.id.senderImageProfile);
            senderImageMessage = (ImageView)view.findViewById(R.id.img_chat_sender);

            //Layouts
            sender = (LinearLayout)view.findViewById(R.id.layout_receiver);
            receiver = (LinearLayout)view.findViewById(R.id.layout_sender);

            //Image layouts
            receiverImageLayout = (LinearLayout)view.findViewById(R.id.imageSenderLinear);
            senderImageLayout = (LinearLayout)view.findViewById(R.id.imageReceiverLinear);

        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {
        Messages message = mMessageList.get(i);
        String type = message.getType();
        final boolean isMe = message.getFrom() != null && !message.getFrom().equals(mUserId);

        if (isMe) {

            //Me the sender of the message
            if (type.equals("text")) {
                //If type of the message is text it should display
                viewHolder.sender.setVisibility(LinearLayout.VISIBLE);
                viewHolder.receiver.setVisibility(LinearLayout.GONE);
            }else{
                //else it should display the image and hide the text
                viewHolder.senderImageLayout.setVisibility(LinearLayout.VISIBLE);
                viewHolder.sender.setVisibility(LinearLayout.GONE);
                viewHolder.receiver.setVisibility(LinearLayout.GONE);
            }
        } else {
                //same applies here
            if (type.equals("text")) {
                viewHolder.receiver.setVisibility(LinearLayout.VISIBLE);
                viewHolder.sender.setVisibility(LinearLayout.GONE);
            }else{
                viewHolder.receiverImageLayout.setVisibility(LinearLayout.VISIBLE);
                viewHolder.receiver.setVisibility(LinearLayout.GONE);
                viewHolder.sender.setVisibility(LinearLayout.GONE);
            }

        }



        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();
        String message_type = c.getType();



        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumbnail").getValue().toString();
               // viewHolder.displayName.setText(name);

                Picasso.with(viewHolder.profileImage.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);

                Picasso.with(viewHolder.imageOther.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar).into(viewHolder.imageOther);

                Picasso.with(viewHolder.sendImageProfile.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar).into(viewHolder.sendImageProfile);

                Picasso.with(viewHolder.receiverImageProfile.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar).into(viewHolder.receiverImageProfile);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

            //String mCurrentUser = mAuth.getCurrentUser().getUid();
            //Receiver display details
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.displayTime.setText(DateFormat.format("h:m a",c.getTime()));

            //load image message of receiver
           // Picasso.with(viewHolder.receiverImageMessage.getContext()).load(c.getMessage()).resize(100,100).into(viewHolder.receiverImageMessage);

        Glide
                .with(viewHolder.receiverImageMessage.getContext())
                .load(c.getMessage()).apply(new RequestOptions()
                .placeholder(R.drawable.default_avatar)
                .dontAnimate()
                .dontTransform())
                .into(viewHolder.receiverImageMessage);


            //load image message of sender
            //Picasso.with(viewHolder.senderImageMessage.getContext()).load(c.getMessage()).resize(100,100).into(viewHolder.senderImageMessage);
        Glide
                .with(viewHolder.senderImageMessage.getContext())
                .load(c.getMessage()).apply(new RequestOptions()
                .placeholder(R.drawable.default_avatar)
                .dontAnimate()
                .dontTransform())
                .into(viewHolder.senderImageMessage);


        //Sender display details
        viewHolder.displaySenderMessage.setText(c.getMessage());
        viewHolder.displayTimeSender.setText(DateFormat.format("h:m a",c.getTime()));

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }








}
