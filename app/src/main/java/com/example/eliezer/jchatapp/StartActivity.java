package com.example.eliezer.jchatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button regBtn;
    private Button loginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);



        loginBtn  = (Button)findViewById(R.id.signBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login_Intent = new Intent(StartActivity.this,LoginActivity.class);
                startActivity(login_Intent);
            }
        });

        regBtn = (Button)findViewById(R.id.register_btn);

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register_Intent = new Intent(StartActivity.this,RegisterActivicty.class);
                startActivity(register_Intent);
            }
        });
    }
}
