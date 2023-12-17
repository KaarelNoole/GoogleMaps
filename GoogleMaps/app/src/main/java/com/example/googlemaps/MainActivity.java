package com.example.googlemaps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.googlemaps.model.ImageList;
import com.example.googlemaps.model.ImageRecyclerAdapter;
import com.example.googlemaps.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private StorageTask storageTask;
    private CircleImageView circleImageView;
    private ImageRecyclerAdapter imageRecyclerAdapter;
    private List<ImageList> imageLists;
    private User userData;
    private Uri uri;
    private TextView txtFirst,txtLast, txtEmail;
    String first, last, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtFirst = findViewById(R.id.txtFirstName);
        txtLast = findViewById(R.id.txtLastName);
        txtEmail = findViewById(R.id.txtEmail);
        circleImageView = findViewById(R.id.profile_image);
        imageLists = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance("https://fir-fcd54-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(firebaseUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userData = dataSnapshot.getValue(User.class);
                assert userData != null;
                if(userData.getImageUrl().equals("default")){
                    circleImageView.setImageResource(R.mipmap.ic_launcher_round);
                } else {
                    Glide.with(getApplicationContext()).load(userData.getImageUrl()).into(circleImageView);
                }
                first = userData.getFirstname();
                last = userData.getLastname();
                email = userData.getEmail();

                txtFirst.setText(first);
                txtLast.setText(last);
                txtEmail.setText(email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        circleImageView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(true);
            View alertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.profile_select, null);
            RecyclerView recyclerView = alertView.findViewById(R.id.recyclerView);
            collectOldImages();
            recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 4));
            recyclerView.setHasFixedSize(true); // 4
            imageRecyclerAdapter = new ImageRecyclerAdapter(imageLists, MainActivity.this);
            recyclerView.setAdapter(imageRecyclerAdapter);
            imageRecyclerAdapter.notifyDataSetChanged();

            Button openProfileImage = alertView.findViewById(R.id.btnOpenProfilePicture);
            openProfileImage.setOnClickListener(view2 -> profileImage());
            builder.setView(alertView);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    private void collectOldImages() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://fir-fcd54-default-rtdb.europe-west1.firebasedatabase.app/").getReference("profile_images").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageLists.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    imageLists.add(dataSnapshot.getValue(ImageList.class));
                }
                imageRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void profileImage() {
        Intent imageIntent = new Intent();
        imageIntent.setType("image/*");
        imageIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(imageIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            uri = data.getData();
            if (storageTask != null && storageTask.isInProgress()){
                Toast.makeText(this, getString(R.string.uppright), Toast.LENGTH_SHORT).show();
            } else{
                uploadImage();
            }
        } else {
            Toast.makeText(this, getString(R.string.imageError), Toast.LENGTH_LONG).show();
        }
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.upimg));
        progressDialog.show();

        if(uri != null){
            Bitmap bitmap = null;
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException ex){
                ex.printStackTrace();
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
            byte[] imageToByte = byteArrayOutputStream.toByteArray();
            final StorageReference imageReference = storageReference.child(userData.getEmail() + System.currentTimeMillis() + ".jpg");
            storageTask = imageReference.putBytes(imageToByte);

            storageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!storageTask.isSuccessful()){
                        throw Objects.requireNonNull(task.getException());
                    }
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String downUri = downloadUri.toString();
                        Map<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageUrl", downUri);
                        databaseReference.updateChildren(hashMap);

                        final DatabaseReference profileReference = FirebaseDatabase.getInstance("https://fir-fcd54-default-rtdb.europe-west1.firebasedatabase.app/").getReference("profile_images").child(Objects.requireNonNull(mAuth.getUid()));
                        profileReference.push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    progressDialog.dismiss();
                                } else{
                                    progressDialog.dismiss();
                                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.upfail), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.signOut){
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    public void onMapOpen(View view) {
        startActivity(new Intent(getApplicationContext(), MapActivity.class));
    }

    public void onTrackedLocations(View view) {
        startActivity(new Intent(getApplicationContext(), TrackerActivity.class));
    }
}