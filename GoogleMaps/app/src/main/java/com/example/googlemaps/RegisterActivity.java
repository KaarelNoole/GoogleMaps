package com.example.googlemaps;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etEmail, etPasswordFirst, etPasswordConfirm;
    private String TAG = "registerDebug";
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPasswordFirst = findViewById(R.id.etPasswordField);
        etPasswordConfirm = findViewById(R.id.etConf);
        etEmail.setText(getIntent().getExtras().getString("register"));
    }

    public void onCreate(View view) {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password1 = etPasswordFirst.getText().toString().trim();
        String password2 = etPasswordConfirm.getText().toString().trim();
        if(password1.length()>= 8 && password1.matches(password2)){
            mAuth.createUserWithEmailAndPassword(email, password1)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Log.i(TAG, "onComplete: User data saved and verification email sent!");
                                            Toast.makeText(RegisterActivity.this, "We have sent a verification email.", Toast.LENGTH_LONG).show();

                                            saveUserData(user, firstName, lastName, email);
                                        } else {
                                            Log.i(TAG, "onComplete: Failure sending verification email.");
                                            Toast.makeText(RegisterActivity.this, "Verification failed, try again.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure ", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, getString(R.string.registerPasswordFail), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserData(FirebaseUser user, String firstName, String lastName, String email) {
        String userID = user.getUid();
        databaseReference = FirebaseDatabase.getInstance("https://project-14f8f-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(userID);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("userId", userID);
        hashMap.put("firstName", firstName);
        hashMap.put("lastName", lastName);
        hashMap.put("email", email);
        hashMap.put("imageUrl", "default");
        databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    new CountDownTimer(3000, 1000){
                        @Override
                        public void onTick(long l) {}
                        @Override
                        public void onFinish() {
                            mAuth.signOut();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                    }.start();
                }
            }
        });
    }
}