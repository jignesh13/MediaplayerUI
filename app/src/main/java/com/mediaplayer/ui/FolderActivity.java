package com.mediaplayer.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.MediaItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class FolderActivity extends AppCompatActivity {
    private MyListAdapter myListAdapter;
    private HashMap<String, List<VideoModel>> videodata;
    private HashMap<String,List<String>> latestvideo=new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        findViewById(R.id.privacytext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(FolderActivity.this,PrivacyActivity.class);
                startActivity(intent);
            }
        });
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        RecyclerView folderrecycleview=findViewById(R.id.folderrecyclerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        folderrecycleview.setLayoutManager(layoutManager);

        myListAdapter=new MyListAdapter(null);
        folderrecycleview.setAdapter(myListAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,100);
    }

    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] { permission }, requestCode);
        }
        else {
            //permission already accept
            MediaModel mediaModel=Utility.getAllMedia(this);
            videodata=mediaModel.getListHashMap();
            String[] keyset= (String[]) videodata.keySet().toArray(new String[videodata.size()]);

            latestvideo = Utility.checklatest(this,videodata,mediaModel.getIdlist());
            Arrays.sort(keyset, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.toLowerCase().compareTo(s2.toLowerCase());
                }
            });
            Log.e("latestinfo",keyset.toString());

            myListAdapter.setListdata(keyset);
            myListAdapter.notifyDataSetChanged();
        }
    }


    // This function is called when user accept or decline the permission.
// Request Code is used to check which permission called this function.
// This request code is provided when user is prompt for permission.


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==100&&grantResults[0] == PackageManager.PERMISSION_GRANTED){
            MediaModel mediaModel=Utility.getAllMedia(this);
            videodata=mediaModel.getListHashMap();
            String[] keyset= (String[]) videodata.keySet().toArray(new String[videodata.size()]);
            latestvideo = Utility.checklatest(this,videodata,mediaModel.getIdlist());
            Arrays.sort(keyset, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.toLowerCase().compareTo(s2.toLowerCase());
                }
            });


            myListAdapter.setListdata(keyset);
            myListAdapter.notifyDataSetChanged();
        }
        else {
            //permission denied
        }
    }


    public static int getExifOrientation(String filepath) {// YOUR MEDIA PATH AS STRING
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

            }
        }
        return degree;
    }
    class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{
        private String[] listdata;

        // RecyclerView recyclerView;
        public MyListAdapter(String[] listdata) {
            this.listdata = listdata;

        }

        public void setListdata(String[] listdata) {
            this.listdata = listdata;

        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.folderitem, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            String foldername = listdata[position];
            String capatilize=foldername.substring(0,1).toUpperCase()+foldername.substring(1).toLowerCase();
            holder.foldernameview.setText(capatilize);
            holder.noofvideoview.setText(videodata.get(listdata[position]).size()+" videos");
            if(latestvideo.containsKey(listdata[position])){
                holder.badgeview.setText(latestvideo.get(listdata[position]).size()+"");
                holder.badgeview.setVisibility(View.VISIBLE);

            }
            else {
                holder.badgeview.setVisibility(View.INVISIBLE);
            }

//            final MyListData myListData = listdata[position];
//            holder.textView.setText(listdata[position].getDescription());
//            holder.imageView.setImageResource(listdata[position].getImgId());
//            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Toast.makeText(view.getContext(),"click on item: "+myListData.getDescription(),Toast.LENGTH_LONG).show();
//                }
//            });
        }


        @Override
        public int getItemCount() {
            return listdata==null?0:listdata.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView foldernameview;
            public TextView noofvideoview;
            public TextView badgeview;
            public ViewHolder(View itemView) {
                super(itemView);
                this.foldernameview = (TextView) itemView.findViewById(R.id.foldername);
                this.noofvideoview = (TextView) itemView.findViewById(R.id.noofvideo);
                this.badgeview=itemView.findViewById(R.id.badgeview);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(FolderActivity.this,MediaActivity.class);
                        intent.putExtra("foldername",listdata[getAdapterPosition()]);
                        Gson gson = new Gson();
                        String json=gson.toJson(videodata.get(listdata[getAdapterPosition()]));
                        intent.putExtra("jsondata",json);
                        startActivity(intent);
                    }
                });
            }
        }
    }
}

