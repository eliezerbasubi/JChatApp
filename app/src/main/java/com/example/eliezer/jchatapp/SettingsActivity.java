package com.example.eliezer.jchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private DatabaseReference mUserRef;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;
    private CircleImageView user_profile;
    private TextView user_name;
    private TextView user_status;
    private TextView user_email;
    private TextView full_name;
    private ImageButton editStatus;
    private ImageButton change_profile;
    private final int GALLERY_PICK = 1;
    private ProgressDialog progressDialog;
    private Toolbar toolbar;
    private byte[] thumb_byte;


    //STORAGE REFERENCE TO DATABASE
    private StorageReference mStorageRef;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //DECLARE TOOLBAR
        toolbar = (Toolbar)findViewById(R.id.settings_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("General Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //DECLARE VARIABLES
        user_profile = (CircleImageView)findViewById(R.id.user_profile_picture);
        user_name = (TextView)findViewById(R.id.display_user_settings);
        user_status = (TextView)findViewById(R.id.user_settings_status);
        user_email = (TextView)findViewById(R.id.user_settings_email);
        full_name = (TextView)findViewById(R.id.user_full_name_settings);
        editStatus = (ImageButton)findViewById(R.id.update_status_btn);
        change_profile = (ImageButton)findViewById(R.id.change_profile_picture);

        //DECLARE CURRENT USER AND USER ID
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mAuth = FirebaseAuth.getInstance();

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        mStorageRef = FirebaseStorage.getInstance().getReference();

        String current_Id = firebaseUser.getUid();
        final String current_Email = firebaseUser.getEmail();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_Id);

        //ADD OFFLINE CAPABILITIES TO VARIABLES

        databaseReference.keepSynced(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            //WHEN DATA IS ADDED OR CHANGED

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(SettingsActivity.this,dataSnapshot.toString(),Toast.LENGTH_LONG).show();

                String fullname = dataSnapshot.child("fullname").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();

                //SET NEW VALUES INTO SETTINGS ACCOUNT
                user_name.setText(name);
                user_status.setText(status);
                user_email.setText(current_Email);
                full_name.setText(fullname);


                //LOAD PROFILE IMAGE
                if(!image.equals("Default")) {
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(user_profile, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        // IF IMAGE IS NOT STORED OFFLINE
                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(user_profile);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //WHEN USER CLICKS ON STATUS TEXT VIEW
        editStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value = user_status.getText().toString();
                Intent updateIntent = new Intent(SettingsActivity.this,UpdateStatusActivity.class);
                updateIntent.putExtra("status_value",status_value);
                startActivity(updateIntent);
            }
        });


        //CHANGE IMAGE PROFILE WHEN CLICKED
        change_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Upload Profile"),GALLERY_PICK);*/
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);
            }
        });

    }//end onCreate method

    //ADD CROPPED IMAGE INTO RESULT SET

      @Override
      public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog = new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Uploading Profile");
                progressDialog.setMessage("Please wait until upload process is done");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();


                Uri resultUri = result.getUri();

                final File thumbFilePath= new File(resultUri.getPath());

                try {
                    Bitmap thumb_file = new Compressor(SettingsActivity.this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumbFilePath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_file.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thumb_byte = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String current_user_id = firebaseUser.getUid();

                StorageReference filePath = mStorageRef.child("profile_images").child(current_user_id+".jpg");
                final StorageReference filePathThumb = mStorageRef.child("profile_images").child("thumbs").child(current_user_id+".jpg");


                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener <UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            final String download_link = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = filePathThumb.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener <UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task <UploadTask.TaskSnapshot> thumbnail_task) {

                                    String thumbDownloadUri = thumbnail_task.getResult().getDownloadUrl().toString();

                                    if (thumbnail_task.isSuccessful()){

                                        //UPLOAD AND DISPLAY IMAGE
                                        Map update_Hashmap = new HashMap <>();
                                        update_Hashmap.put("image",download_link);
                                        update_Hashmap.put("thumbnail",thumbDownloadUri);

                                        databaseReference.updateChildren(update_Hashmap).addOnCompleteListener(new OnCompleteListener <Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task <Void> task) {
                                                if(task.isSuccessful()){
                                                    progressDialog.dismiss();

                                                    Toast.makeText(SettingsActivity.this,"Profile is successfully uploaded",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }else{
                                        Toast.makeText(SettingsActivity.this,"Thumbnail cannot be uploading",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });



                        }else{
                            Toast.makeText(SettingsActivity.this,"Image not uploaded",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
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




    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(100);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
