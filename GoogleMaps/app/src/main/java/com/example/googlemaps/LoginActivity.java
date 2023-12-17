package com.example.googlemaps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class LoginActivity extends AppCompatActivity {

    public static final int PERMISSIONS_FINE_LOCATION = 0;
    private TextInputEditText etEmail, etPassword;
    private FirebaseAuth mAuth;
    private String email, password;
    private String TAG = "registerDebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPasswordField);
        mAuth = FirebaseAuth.getInstance();
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Log.i("onCreate: ", getString(R.string.gspPerm));
        } else {
            requestGPSPermission();
        }
    }

    private void requestGPSPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), getString(R.string.locReq), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(android.R.string.ok, new GpsSnackListener());
            snackbar.show();
        }else{
            Snackbar.make(getWindow().getDecorView().getRootView(), getString(R.string.gpsNoGo), Snackbar.LENGTH_SHORT);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }

    public class GpsSnackListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_FINE_LOCATION) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.i(TAG, "onRequestPermissionsResult: " + getString(R.string.maPerm));
            }
        }else{
            Snackbar.make(getWindow().getDecorView().getRootView(), getString(R.string.NoWork), Snackbar.LENGTH_INDEFINITE);
        }
    }

    public void onLogin(View view) {
        if(fieldValidation()) return;
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if (Objects.requireNonNull(mAuth.getCurrentUser()).isEmailVerified()){
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.signSuc), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            } else {
                Log.i("debug", "onComplete: Sign in failed!");
                Toast.makeText(LoginActivity.this, getString(R.string.signFail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onRegister(View view) {
        email = etEmail.getText().toString().trim();
        if(TextUtils.isEmpty(email)){
            etEmail.setError(getString(R.string.error));
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, getString(R.string.emailincorrect), Toast.LENGTH_SHORT).show();
        } else {
            Intent register = new Intent(getApplicationContext(), RegisterActivity.class);
            register.putExtra("register", email);
            startActivity(register);
            finish();
        }
    }

    public void onForgot(View view) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.i("debuggg", "onComplete: Email was successfully sent!");
                    Toast.makeText(LoginActivity.this, getString(R.string.emsuc), Toast.LENGTH_SHORT).show();
                } else{
                    Log.i("debuggg", "onComplete: Error sending the email!");
                    Toast.makeText(LoginActivity.this, getString(R.string.emfail), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if(currentUser != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    private boolean fieldValidation() {
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
            etEmail.setError(getString(R.string.error));
            etPassword.setError(getString(R.string.error));
            return true;
        } else if(TextUtils.isEmpty(email)){
            etEmail.setError(getString(R.string.error));
            return true;
        } else if(TextUtils.isEmpty(password)){
            etPassword.setError(getString(R.string.error));
            return true;
        }
        return false;
    }
}