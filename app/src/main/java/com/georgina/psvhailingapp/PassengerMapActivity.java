package com.georgina.psvhailingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.service.controls.actions.FloatAction;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class PassengerMapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawer;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    GoogleSignInClient googleSignInClient;
    private NavigationView navigationView;
    private TextView profileFullName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_map);
        //Initialize firebase auth
        mAuth = FirebaseAuth.getInstance();
        //Initialize firebase user
        mCurrentUser = mAuth.getCurrentUser();
        mDrawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        profileFullName = header.findViewById(R.id.profile_fullname);
        if(mCurrentUser != null){
            //Toast.makeText(getApplicationContext(), mCurrentUser.getDisplayName(), Toast.LENGTH_SHORT).show();
            profileFullName.setText(mCurrentUser.getDisplayName());
        }

        //Initialize sign in client
        googleSignInClient = GoogleSignIn.getClient(PassengerMapActivity.this,
                GoogleSignInOptions.DEFAULT_SIGN_IN);
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new PassengerMapsFragment()).commit();
            navigationView.setCheckedItem(R.id.map);
        }
    }
    public void openDrawer(View view) {
        mDrawer.openDrawer(GravityCompat.START);
    }
    public void onBackPressed(){
        if(mDrawer.isDrawerOpen(GravityCompat.START)){
            mDrawer.closeDrawer(GravityCompat.START);
        }
    }



    public void logout(View view) {
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                //Check condition
                if(task.isSuccessful()){
                    //Sign out from firebase
                    mAuth.signOut();
                    //display toast
                    Toast.makeText(getApplicationContext(), "Logout Successful", Toast.LENGTH_SHORT).show();
                    //finish activity
                    finish();
                }
            }
        });
        sendUserToRegister();
    }

    private void sendUserToRegister() {
        mAuth.signOut();
        Intent mainIntent = new Intent(PassengerMapActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    public void sendToDriverDetails(View view) {
        Intent intent = new Intent(getApplicationContext(),DriverDetailsActivity.class );
        startActivity(intent);
        finish();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.map:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new DriverMapsFragment()).commit();
                break;
        }
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }
}