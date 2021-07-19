package com.georgina.psvhailingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class VerifyCodeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase firebaseDatabase;
    private Task<Void> databaseReference;
    private String mAuthVerificationId;
    private TextView mMessageText;
    private EditText mCode;
    private String mPhoneNumber;
    private TextView mTextViewNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);
        mTextViewNo = findViewById(R.id.textView_number);
        mMessageText = findViewById(R.id.messageText);
        mCode = findViewById(R.id.v_code);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mAuthVerificationId = getIntent().getStringExtra("AuthCredentials");
        mPhoneNumber = getIntent().getStringExtra("phoneNumber");
        mTextViewNo.setText(mPhoneNumber);
    }

    public void Verify(View view) {
        String code = mCode.getText().toString();
        if(code.isEmpty()){
            mMessageText.setText(R.string.blank_number);
            mMessageText.setVisibility(View.VISIBLE);
        }
        else{
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mAuthVerificationId,code);
            signInWithPhoneAuthCredential(credential);
        }
    }
    public void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyCodeActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mCurrentUser = task.getResult().getUser();
                            String user_id = mCurrentUser.getUid();
                            storeUserData(user_id);
                            Toast.makeText(getApplicationContext(), user_id, Toast.LENGTH_LONG).show();
//                            Toast toast = Toast.makeText(getApplicationContext(),"This has worked",Toast.LENGTH_SHORT);
//                            toast.show();
                            //storeUserData();
                            sendUserToMain();


                        } else {

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                mMessageText.setVisibility(View.VISIBLE);
                                mMessageText.setText(R.string.error_verifying);
                            }
                        }
                    }
                });
    }
    protected void onStart(){
        super.onStart();
        if(mCurrentUser != null){
            sendUserToMain();
        }
    }

    private void sendUserToMain() {
        Intent mainIntent = new Intent(getApplicationContext(),PassengerMapActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    public void storeUserData(String user_id){
        Toast.makeText(getApplicationContext(), user_id, Toast.LENGTH_SHORT).show();
        String email = getIntent().getStringExtra("email");
        String fullName = getIntent().getStringExtra("fullName");
        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        if(email == "" || fullName == ""){
            Toast.makeText(getApplicationContext(), "This is just the login activity", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "This is just the signup activity", Toast.LENGTH_SHORT).show();
        }
//        firebaseDatabase = FirebaseDatabase.getInstance();
//        User user = new User(fullName, email, phoneNumber);
//        databaseReference = firebaseDatabase.getReference().child("Users").child(user_id).setValue(user);
    }




}
