package com.example.eliezer.jchatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    public static ViewPager viewPager;
    private SessionsPageerAdapter sessionsPageerAdapter;
    private TabLayout tabLayout;
    private DatabaseReference mUserRef;
    private DatabaseReference readeMessagedRef;
    private TextView readMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            readeMessagedRef = FirebaseDatabase.getInstance().getReference().child("messages").child(mAuth.getCurrentUser().getUid());

        }
        mToolbar =(Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("JChat Messenger");

        viewPager = (ViewPager)findViewById(R.id.tabPager);
        tabLayout = (TabLayout)findViewById(R.id.main_tabs);


        sessionsPageerAdapter = new SessionsPageerAdapter(getSupportFragmentManager());

        sessionsPageerAdapter.addFragments(new Requests(),"");
        sessionsPageerAdapter.addFragments(new Chat(), "");
        sessionsPageerAdapter.addFragments(new Friends(), "");

        viewPager.setAdapter(sessionsPageerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(1);


        tabLayout.getTabAt(0).setCustomView(R.layout.request_seen_show_layout);
        tabLayout.getTabAt(1).setCustomView(R.layout.status_update_layout);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_people_outline_black_24dp);


        RelativeLayout rl = (RelativeLayout) findViewById(R.id.tab_messages_layout);
        View vi = getLayoutInflater().inflate(R.layout.status_update_layout, null);
        readMessages = (TextView)findViewById(R.id.show_new_messages_count);
         rl.addView(vi);

         Conv conv = new Conv();
            if (conv.getCounter() > 0){
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

                //String countMessages = prefs.getString("unread_messages",null);

                //readMessages.setVisibility(View.VISIBLE);
                //readMessages.setText(countMessages);
            }else{

            }



    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            sendToHome();
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

    private void sendToHome() {
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);

         getMenuInflater().inflate(R.menu.main_menu,menu);

         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout){
            FirebaseAuth.getInstance().signOut();

            sendToHome();
        }
        if (item.getItemId() == R.id.account_settings){
            Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if (item.getItemId() == R.id.all_users){
            Intent usersIntent = new Intent(MainActivity.this,AllUsersActivity.class);
            startActivity(usersIntent);
        }

        return  true;
    }
}
