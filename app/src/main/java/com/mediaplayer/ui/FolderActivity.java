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
import android.content.Intent;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class FolderActivity extends AppCompatActivity {
    private MyListAdapter myListAdapter;
    private HashMap<String, List<VideoModel>> videodata;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        RecyclerView folderrecycleview=findViewById(R.id.folderrecyclerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        folderrecycleview.setLayoutManager(layoutManager);

        myListAdapter=new MyListAdapter(null);
        folderrecycleview.setAdapter(myListAdapter);
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
            videodata=getAllMedia();
           String[] keyset= (String[]) videodata.keySet().toArray(new String[videodata.size()]);
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
            //permission accept
            videodata=getAllMedia();
            String[] keyset= (String[]) videodata.keySet().toArray(new String[videodata.size()]);
            myListAdapter.setListdata(keyset);
            myListAdapter.notifyDataSetChanged();
        }
        else {
            //permission denied
        }
    }
    public HashMap<String, List<VideoModel>> getAllMedia() {
        HashMap<String, List<VideoModel>> listHashMap=new HashMap<>();

        String[] projection = { MediaStore.Video.VideoColumns.DATA ,MediaStore.Video.Media.DISPLAY_NAME,MediaStore.Video.Media._ID,MediaStore.Video.Media.WIDTH,MediaStore.Video.Media.HEIGHT};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        try {
            cursor.moveToFirst();
            do{
                int columnIndex = cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int id = cursor.getInt(columnIndex);
                //Bitmap bitmap= MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(),id,MediaStore.Video.Thumbnails.MICRO_KIND,null);
                String path= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
               boolean islandscape=cursor.getInt(3)>cursor.getInt(4);

                String[] divide=path.split("/");
                String foldername=divide[divide.length-2];

                VideoModel videoModel=new VideoModel(path,id,cursor.getString(1),islandscape);
                if(listHashMap.containsKey(foldername)){
                    listHashMap.get(foldername).add(videoModel);
                }
                else {
                    ArrayList<VideoModel> modelArrayList=new ArrayList<>();
                    modelArrayList.add(videoModel);
                    listHashMap.put(foldername,modelArrayList);
                }

            }while(cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listHashMap;
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

            holder.foldernameview.setText(listdata[position]);
            holder.noofvideoview.setText(videodata.get(listdata[position]).size()+" videos");

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
            public ViewHolder(View itemView) {
                super(itemView);
                this.foldernameview = (TextView) itemView.findViewById(R.id.foldername);
                this.noofvideoview = (TextView) itemView.findViewById(R.id.noofvideo);
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


