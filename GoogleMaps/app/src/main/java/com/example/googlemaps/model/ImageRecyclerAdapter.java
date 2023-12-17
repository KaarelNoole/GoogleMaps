package com.example.googlemaps.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.googlemaps.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

// Adapter klass, et kuvada viewsi recyclerviews
public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.MyViewHolder>{

    private final List<ImageList> imageLists;
    Context context;

    public ImageRecyclerAdapter(List<ImageList> imageLists, Context context) {
        this.imageLists = imageLists;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(imageLists.get(position).getImageUrl()).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("imageUrl", imageLists.get(position).getImageUrl());
                databaseReference.updateChildren(hashMap);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageLists.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profile_image);
        }
    }
}
