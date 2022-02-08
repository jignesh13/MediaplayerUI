package com.mediaplayer.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MediaActivity extends AppCompatActivity {

    private ArrayList<VideoModel> videoModels=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
       Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){

          getSupportActionBar().setTitle(bundle.getString("foldername"));
            Gson gson = new Gson();
            Type listOfdoctorType = new TypeToken<ArrayList<VideoModel>>() {}.getType();
           videoModels = gson.fromJson(bundle.getString("jsondata"),listOfdoctorType );

        }
        RecyclerView recyclerView=findViewById(R.id.mediarecyclerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        final int spacing = getResources().getDimensionPixelSize(R.dimen.spacing) / 2;

// apply spacing
        recyclerView.setPadding(spacing, spacing, spacing, spacing);
        recyclerView.setClipToPadding(false);
        recyclerView.setClipChildren(false);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(spacing, spacing, spacing, spacing);
            }
        });
        recyclerView.setAdapter(new MyMediaAdapter());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();  return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class MyMediaAdapter extends RecyclerView.Adapter<MyMediaAdapter.ViewHolder>{

        // RecyclerView recyclerView;


        @NonNull
        @Override
        public MyMediaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.videoitem, parent, false);
            MyMediaAdapter.ViewHolder viewHolder = new MyMediaAdapter.ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyMediaAdapter.ViewHolder holder, int position) {

            Glide.with(MediaActivity.this)
                    .load(videoModels.get(position).getUrl())
                    .transform(new CenterCrop(),new RoundedCorners(20))
                    .placeholder(R.drawable.placeholder)
                 .into(holder.thumbnail);
            holder.titletext.setText(videoModels.get(position).getName());
        }



        @Override
        public int getItemCount() {
            return videoModels.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView thumbnail;
            public TextView titletext;
            public ViewHolder(View itemView) {
                super(itemView);
                this.thumbnail = (ImageView) itemView.findViewById(R.id.imageView2);
                this.titletext=itemView.findViewById(R.id.textView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                     Bundle bundle =   MediaActivity.this.getIntent().getExtras();
                     if(bundle!=null){
                         Intent intent=new Intent(MediaActivity.this,MainActivity.class);
                         intent.putExtra("jsondata",bundle.getString("jsondata"));
                         intent.putExtra("pos",getAdapterPosition());
                         startActivity(intent);
                     }
                    }
                });

            }
        }
    }
}