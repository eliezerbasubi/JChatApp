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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout password;
    private TextInputLayout email;
    private Button login;
    private Toolbar mToolbar;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        email = (TextInputLayout)findViewById(R.id.user_email);
        password =(TextInputLayout)findViewById(R.id.user_password);
        login = (Button)findViewById(R.id.loginBtn);
        mToolbar = (Toolbar)findViewById(R.id.login_app_bar);

        progressDialog = new ProgressDialog(this);



        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign into JChat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String my_email = email.getEditText().getText().toString().trim();
                String mypassword = password.getEditText().getText().toString().trim();

                if(!TextUtils.isEmpty(my_email) || !TextUtils.isEmpty(mypassword)){
                    progressDialog.setTitle("Signing In");
                    progressDialog.setMessage("Checking for network...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    loginUser(my_email,mypassword);
                }
            }
        });
    }

    private void loginUser(String email, String password){

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            progressDialog.dismiss();

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            String current_user_id = mAuth.getCurrentUser().getUid();

                            databaseReference.child(current_user_id).child("device_token").setValue(deviceToken)
                                    .addOnSuccessListener(new OnSuccessListener <Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Intent loginIntent = new Intent(LoginActivity.this,MainActivity.class);

                                    //Keeps the user online once he has logged in
                                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(loginIntent);
                                    finish();
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            progressDialog.hide();
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }
}
