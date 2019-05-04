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
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivicty extends AppCompatActivity {
    private TextInputLayout fname ;
    private  TextInputLayout uname;
    private TextInputLayout email;
    private TextInputLayout pass;
    private TextInputLayout conf_pass;
    private Button reg_btn;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ProgressDialog progressDialog;
    private DatabaseReference firebaseDatabase;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_activicty);

        mAuth = FirebaseAuth.getInstance();

        fname = (TextInputLayout)findViewById(R.id.fullname);
        uname = (TextInputLayout)findViewById(R.id.username);
        email = (TextInputLayout)findViewById(R.id.email);
        pass = (TextInputLayout)findViewById(R.id.user_password);
        conf_pass = (TextInputLayout)findViewById(R.id.confirm_password);
        reg_btn = (Button)findViewById(R.id.register_btn);
        mToolbar = (Toolbar)findViewById(R.id.register_app_bar);

        //PROGRESS BAR WHEN A NEW USER IS ADDED
        progressDialog = new ProgressDialog(this);



        //ADD TOOLBAR
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Register an Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editFname = fname.getEditText().getText().toString();
                String editUname= uname.getEditText().getText().toString();
                String editEmail = email.getEditText().getText().toString();
                String editpassword = pass.getEditText().getText().toString();
                String editConfirm_pass= conf_pass.getEditText().getText().toString();

                if(TextUtils.isEmpty(editFname) || TextUtils.isEmpty(editUname) || TextUtils.isEmpty(editEmail) || TextUtils.isEmpty(editpassword) || TextUtils.isEmpty(editConfirm_pass)){
                    if (editConfirm_pass.equals(editpassword)){
                        Toast.makeText(RegisterActivicty.this,"Password not matched",Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(RegisterActivicty.this,"Fill all Fields",Toast.LENGTH_SHORT).show();
                }else {
                 progressDialog.setTitle("Registration in progress");
                 progressDialog.setMessage("Wait until your registration is done ...");
                 progressDialog.setCanceledOnTouchOutside(true);
                 progressDialog.show();
                    register_user(editUname, editEmail, editpassword);
                }
            }
        });
    }

    public void register_user(final String username, String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //ADD USER
                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            //ADD COMPLEX DATA INTO FIREBASE DATABASE
                            HashMap<String,String>userMap = new HashMap <>();
                            userMap.put("fullname",fname.getEditText().getText().toString());
                            userMap.put("name",username);
                            userMap.put("status","Hey, come chill with me on JChat");
                            userMap.put("image","Default");
                            userMap.put("thumbnail","Default");
                            userMap.put("device_token",deviceToken);

                            firebaseDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener <Void>() {
                                @Override
                                public void onComplete(@NonNull Task <Void> task) {
                                    if(task.isSuccessful()){

                                        progressDialog.dismiss();
                                        Intent mainIntent = new Intent(RegisterActivicty.this,MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });


                            // Sign in success, update UI with the signed-in user's information
                            ///Log.d(TAG, "createUserWithEmail:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            progressDialog.hide();
                            Toast.makeText(RegisterActivicty.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }
}
