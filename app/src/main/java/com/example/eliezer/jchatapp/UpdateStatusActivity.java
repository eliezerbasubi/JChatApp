package com.example.eliezer.jchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class UpdateStatusActivity extends AppCompatActivity {

    private Toolbar mToolbar ;
    private EmojiconEditText new_status;
    private Button save_status;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private ProgressDialog progressDialog;
    private ImageView emojiButton;
    EmojIconActions emojIcon;
    View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_status);


        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String current_user = currentUser.getUid();


        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user);

        mToolbar = (Toolbar)findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Update Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new_status = (EmojiconEditText) findViewById(R.id.input_status);
        emojiButton = (ImageView)findViewById(R.id.implement_emoji);
        save_status = (Button)findViewById(R.id.save_changes);
        rootView = (RelativeLayout)findViewById(R.id.update_activity);

        emojIcon =new EmojIconActions(getApplicationContext(),rootView,new_status,emojiButton);
        emojIcon.ShowEmojIcon();

        //GET VALUES SENT FROM SETTINGS ACTIVITY
        String status_value = getIntent().getStringExtra("status_value");

        //INITIALIZE THE VALUE OF THE INPUT STATUS
        new_status.setText(status_value);

        save_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value = new_status.getText().toString();

                progressDialog = new ProgressDialog(UpdateStatusActivity.this);
                progressDialog.setTitle("Saving Changes");
                progressDialog.setMessage("Wait, your status is changing...");
                progressDialog.show();
                if(!TextUtils.isEmpty(status_value)) {
                    databaseReference.child("status").setValue(status_value).addOnCompleteListener(new OnCompleteListener <Void>() {
                        @Override
                        public void onComplete(@NonNull Task <Void> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                Intent settingsIntent = new Intent(UpdateStatusActivity.this, SettingsActivity.class);
                                startActivity(settingsIntent);
                            } else {
                                Toast.makeText(UpdateStatusActivity.this, "Status update failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(UpdateStatusActivity.this,"Write something",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if (currentUser != null) {
            databaseReference.child("online").setValue(true);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentUser != null) {
            databaseReference.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }


}
