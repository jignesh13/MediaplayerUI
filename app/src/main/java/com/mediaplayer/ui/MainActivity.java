package com.mediaplayer.ui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.video.VideoSize;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private float downy;
    private float endheight;
    private float diffheight;
    private int currentprogress;
    private int currentseek;
    private Handler handler;
    private float lastx;
    private float putx, puty;
    private float trackx;
    private float finalwidth;
    private int lastprogress;
    private long lasttime;
    private int selected = 0;
    private boolean isshow;
    private PlayerView playerView;
    private boolean first = true, second = true, third = true;
    private ExoPlayer player;
    private SeekBar dragseek;
    private int currentitem;
    private long currentitemseek;
    private boolean isdonebyus;
    private int ontouchpos;
    private static boolean isplaybackground;
    private static boolean isorientionchange;
    private static int resizemode=0;
    private static int anInt=0;
    private TextView videotitle;
  private  Handler hidehandler;
  private static boolean islock;
  private static int playbackspeed=5;
    private ArrayList<VideoModel> videoModels = new ArrayList<>();

    private String[] aspectmode={"FIT","FILL","ZOOM","FIXED HEIGHT","FIXED WIDTH"};
    private int[] resource={R.drawable.ic_fit_to_screen,R.drawable.ic_baseline_crop_landscape_24,R.drawable.ic_baseline_crop_portrait_24,R.drawable.ic_scale_fit,R.drawable.ic_baseline_fullscreen_24};
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            currentitem = bundle.getInt("pos", 0);
            Gson gson = new Gson();
            Type listOfdoctorType = new TypeToken<ArrayList<VideoModel>>() {
            }.getType();
            videoModels = gson.fromJson(bundle.getString("jsondata"), listOfdoctorType);
        }
        if (savedInstanceState != null) {
            currentitem = savedInstanceState.getInt("currentitem", 0);
            currentitemseek = savedInstanceState.getLong("currentitemseek", 0);

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            // getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;



        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hideSystemUI();
            }
        },50);

        Glide.with(MainActivity.this)
                .asBitmap()
                .load(videoModels.get(currentitem).getUrl())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Log.e("resource", resource.getWidth() + "," + resource.getHeight());
                        boolean islandscape = resource.getWidth() > resource.getHeight();
                        int orientation = MainActivity.this.getResources().getConfiguration().orientation;
                        if(isorientionchange){
                            initview();
                            isorientionchange=false;
                        }
                        else {
                            isorientionchange=false;
                            if (islandscape && orientation == Configuration.ORIENTATION_PORTRAIT) {
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                                return;

                            }
                            else if (!islandscape && orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                                return;

                            }
                            else{

                                initview();
                            }
                        }

                    }
                });

        // startPlayer();

    }

    public void initview(){

        setContentView(R.layout.activity_main);

        Log.e("oncreate", "oncreate");

        View speedView=findViewById(R.id.speedview);
        isshow = false;
        hidehandler=new Handler();


         videotitle=findViewById(R.id.videotitle);

         ImageView unlockbtn=findViewById(R.id.unlockbtn);
        View toolbar=findViewById(R.id.toolbar);
        dragseek = findViewById(R.id.dragseek);
        ImageView playpausebutton = findViewById(R.id.imageButton);
        TextView currentprogresslbl = findViewById(R.id.currentprogress);
        TextView endprogresslbl = findViewById(R.id.endprogress);
        View bottomview = findViewById(R.id.bottomview);
        View touchview = findViewById(R.id.toucher);
        View seeklay = findViewById(R.id.seeklay);
        TextView seektime = findViewById(R.id.seektime);
        TextView seekdelay = findViewById(R.id.seekdelay);
        ImageView aspectbtn = findViewById(R.id.aspectbtn);
        handler = new Handler();
        player = new ExoPlayer.Builder(this).build();

        playerView = findViewById(R.id.player);

        playerView.setPlayer(player);

//http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4


        playpausebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player.isPlaying()) player.pause();
                else player.play();
            }
        });

        findViewById(R.id.rotateview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int orientation = MainActivity.this.getResources().getConfiguration().orientation;
                isorientionchange=true;

                if(orientation==Configuration.ORIENTATION_PORTRAIT){

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
                else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                }
            }
        });
        findViewById(R.id.imageButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentitem+1<videoModels.size()) {
                    currentitem++;
                    Glide.with(MainActivity.this)
                            .asBitmap()
                            .load(videoModels.get(currentitem).getUrl())
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    Log.e("resource", resource.getWidth() + "," + resource.getHeight());
                                    boolean islandscape = resource.getWidth() > resource.getHeight();
                                    int orientation = MainActivity.this.getResources().getConfiguration().orientation;
                                    if (islandscape && orientation == Configuration.ORIENTATION_PORTRAIT) {
                                        player.stop();
                                        player.release();
                                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                                        return;

                                    }
                                    if(islandscape && orientation == Configuration.ORIENTATION_LANDSCAPE){
                                        player.stop();
                                        intializePlayer();
                                        startPlayer();
                                        return;
                                    }
                                    if (!islandscape && orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                        player.stop();
                                        player.release();
                                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                                        return;

                                    }
                                    if (!islandscape && orientation == Configuration.ORIENTATION_PORTRAIT) {
                                        player.stop();
                                        intializePlayer();
                                        startPlayer();
                                        return;

                                    }
                                }
                            });


                }
            }
        });
        findViewById(R.id.imageButton3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentitem-1>=0) {

                    currentitem--;
                    Glide.with(MainActivity.this)
                            .asBitmap()
                            .load(videoModels.get(currentitem).getUrl())
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    boolean islandscape = resource.getWidth() > resource.getHeight();
                                    int orientation = MainActivity.this.getResources().getConfiguration().orientation;
                                    if (islandscape && orientation == Configuration.ORIENTATION_PORTRAIT) {
                                        player.stop();
                                        player.release();
                                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                                        return;

                                    }
                                    if(islandscape && orientation == Configuration.ORIENTATION_LANDSCAPE){
                                        player.stop();
                                        intializePlayer();
                                        startPlayer();
                                        return;
                                    }
                                    if (!islandscape && orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                        player.stop();
                                        player.release();
                                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                                        return;

                                    }
                                    if (!islandscape && orientation == Configuration.ORIENTATION_PORTRAIT) {
                                        player.stop();

                                        intializePlayer();
                                        startPlayer();
                                        return;

                                    }
                                }
                            });
                }
            }
        });
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(player==null)return;
                if (!isdonebyus) dragseek.setProgress((int) player.getCurrentPosition());
                //  currentprogresslbl.setText(milltominute(player.getCurrentPosition()));
                handler.postDelayed(this::run, 1000);
            }
        };
        Runnable hiderunnable=new Runnable() {
            @Override
            public void run() {
                if(player==null)return;
                if (player.isPlaying()) {
                    hideSystemUI();
                    bottomview.setVisibility(View.GONE);
                    toolbar.setVisibility(View.GONE);
                }
            }
        };
        aspectbtn.setImageResource(resource[resizemode%5]);
        TextView aspecttext=findViewById(R.id.aspecttext);
        aspectbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resizemode++;
                aspecttext.setVisibility(View.VISIBLE);
                aspecttext.setText(aspectmode[resizemode%5]);
                aspectbtn.setImageResource(resource[resizemode%5]);
                playerView.setResizeMode(resizemode % 5);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        aspecttext.setVisibility(View.GONE);

                    }
                },300);
                hidehandler.removeCallbacks(hiderunnable);
                hidehandler.postDelayed(hiderunnable,4000);
            }
        });
        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    playpausebutton.setImageResource(R.drawable.ic_baseline_pause_24);
                    endprogresslbl.setText(milltominute(player.getDuration()));
                    dragseek.setMax((int) player.getDuration());
                    handler.postDelayed(runnable, 0);
                } else {
                    playpausebutton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    handler.removeCallbacks(runnable);
                }
            }


            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if(playbackState==Player.STATE_ENDED){
                    Log.e("Ended","end");
                    if(currentitem+1<videoModels.size()){
                        currentitem++;
                        Glide.with(MainActivity.this)
                                .asBitmap()
                                .load(videoModels.get(currentitem).getUrl())
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        Log.e("resource", resource.getWidth() + "," + resource.getHeight());
                                        boolean islandscape = resource.getWidth() > resource.getHeight();
                                        int orientation = MainActivity.this.getResources().getConfiguration().orientation;
                                        if (islandscape && orientation == Configuration.ORIENTATION_PORTRAIT) {
                                            player.stop();
                                            player.release();
                                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                                            return;

                                        }
                                        if(islandscape && orientation == Configuration.ORIENTATION_LANDSCAPE){
                                            player.stop();
                                            intializePlayer();
                                            startPlayer();
                                            return;
                                        }
                                        if (!islandscape && orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                            player.stop();
                                            player.release();
                                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                                            return;

                                        }
                                        if (!islandscape && orientation == Configuration.ORIENTATION_PORTRAIT) {
                                            player.stop();

                                            intializePlayer();
                                            startPlayer();
                                            return;

                                        }
                                    }
                                });

                    }
                }
            }


        });
        player.setSeekParameters(SeekParameters.CLOSEST_SYNC);
        dragseek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if (b) {

                    player.seekTo(i);
                    seektime.setText(milltominute(i));
                    seekdelay.setText("[" + milltominute(i - ontouchpos) + "]");
                }
                currentprogresslbl.setText(milltominute(i));


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                ontouchpos = seekBar.getProgress();
                seeklay.setVisibility(View.VISIBLE);
                player.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seeklay.setVisibility(View.GONE);
                player.play();
            }
        });
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        SoundView soundView = findViewById(R.id.volumeview);
        TextView progresstext = findViewById(R.id.progresstext);
        ImageView volumeview = findViewById(R.id.volumeicon);
        View volumecontainerView = findViewById(R.id.volumecontainer);
        View muteview=findViewById(R.id.muteview);
        muteview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(soundView.getProgress()==0){
                    muteview.setBackgroundResource(R.drawable.roundbg);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 7, 0);
                    soundView.setProgress(7);
                }
                else {
                    muteview.setBackgroundResource(R.drawable.colorroundbg);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                    soundView.setProgress(0);
                }


            }
        });
        soundView.setOnsoundProgressChangeListner(new SoundProgressChangeListner() {
            @Override
            public void onchange(int progress) {
                Log.e("change", progress + "");
                progresstext.setText(progress + "");
                if (progress == 0) {
                    volumeview.setImageResource(R.drawable.ic_baseline_volume_off_24);

                } else {
                    volumeview.setImageResource(R.drawable.ic_baseline_volume_up_24);
                    muteview.setBackgroundResource(R.drawable.roundbg);
                }


            }
        });
        View headsetview=findViewById(R.id.headsetview);

        if(isplaybackground){
            headsetview.setBackgroundResource(R.drawable.colorroundbg);
        }
        else {
            headsetview.setBackgroundResource(R.drawable.roundbg);
        }
        headsetview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isplaybackground = !isplaybackground;
                if(isplaybackground){
                    headsetview.setBackgroundResource(R.drawable.colorroundbg);
                }
                else {
                    headsetview.setBackgroundResource(R.drawable.roundbg);
                }
            }
        });

        touchview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    lastx = motionEvent.getX();
                    downy = motionEvent.getY();
                    putx = motionEvent.getX();
                    puty = motionEvent.getY();
                    lasttime = System.currentTimeMillis();
                    endheight = downy - getResources().getDimensionPixelSize(R.dimen.widthmeasure);
                    diffheight = endheight - downy;
                    currentprogress = soundView.getProgress();
                    first = true;
                    second = true;
                    third = true;
                    selected = 0;
                    isdonebyus = false;
                    trackx = motionEvent.getX();
                    currentseek = dragseek.getProgress();
                    finalwidth = motionEvent.getX() + touchview.getWidth();
                    Log.e("width", touchview.getWidth() + "");

                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                   if(islock)return false;
                    Log.e("myrb", "x=" + lastx + "y=" + downy + "");
                    Log.e("myrb", "x=" + motionEvent.getX() + "y=" + motionEvent.getY() + "," + motionEvent.getAction());
                    float xdistance = motionEvent.getX() - lastx;
                    float ydistance = motionEvent.getY() - downy;
                    if (first && Math.abs(xdistance) == 0 && Math.abs(ydistance) == 0) {


                    } else if ((second && Math.abs(xdistance) < Math.abs(ydistance)) || selected == 1) {
                        if (selected == 0) {
                            selected = 1;
                            first = false;
                            second = true;
                            third = false;
                            volumecontainerView.setVisibility(View.VISIBLE);
                        }
                        float tempwidth = endheight - motionEvent.getY();
                        float progress = (tempwidth * soundView.getMaxprogess()) / diffheight;
                        Log.e("progress", (soundView.getMaxprogess() - progress) + "");
                        int jprogress = (int) (soundView.getMaxprogess() - progress);
                        int prog = currentprogress + jprogress;

                        if (prog > soundView.getMaxprogess())
                            soundView.setProgress(soundView.getMaxprogess());
                        else if (prog < 0) soundView.setProgress(0);
                        else {
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, prog, 0);
                            soundView.setProgress(prog);
                        }

                        Log.e("scroll", "vertical");
                    } else if (third || selected == 2) {

                        if (selected == 0) {
                            if (player.isPlaying()) {
                                isdonebyus = true;
                                player.pause();
                            }
                            second = false;
                            first = false;
                            third = true;
                            selected = 2;
                            playpausebutton.setVisibility(View.GONE);
                            bottomview.setVisibility(View.VISIBLE);
                            //toolbar.setVisibility(View.GONE);
                            seeklay.setVisibility(View.VISIBLE);

                        }

                        int progress = (int) ((60000 * (motionEvent.getX() - trackx)) / touchview.getWidth());

                        if (lastprogress != progress) {
                            player.seekTo(currentseek + progress);
                            dragseek.setProgress(currentseek + progress);
                            seektime.setText(milltominute(dragseek.getProgress()));
                            seekdelay.setText("[" + milltominute(progress) + "]");
                            // Log.e("scroll","horizontal"+milltominute(dragseek.getProgress())+","+milltominute(progress));
                        }
                        lastprogress = progress;


                    }
                    lastx = motionEvent.getX();
                    downy = motionEvent.getY();
                } else {
                    seeklay.setVisibility(View.GONE);
                    if (isdonebyus) player.play();
                    isdonebyus = false;
                    if(islock){
                        if(unlockbtn.getVisibility()==View.GONE){
                            unlockbtn.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    unlockbtn.setVisibility(View.GONE);
                                }
                            },2000);
                        }
                    }
                    else {
                        if (motionEvent.getX() == putx && motionEvent.getY() == puty && System.currentTimeMillis() - lasttime <= 1000) {
                          speedView.setVisibility(View.GONE);
                            if (isshow) {
                                hideSystemUI();
                                bottomview.setVisibility(View.GONE);
                                toolbar.setVisibility(View.GONE);
                            } else {
                                showSystemUI();
                                playpausebutton.setVisibility(View.VISIBLE);
                                bottomview.setVisibility(View.VISIBLE);
                                toolbar.setVisibility(View.VISIBLE);
                                hidehandler.postDelayed(hiderunnable,4000);

                            }
                        } else {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    hideSystemUI();
                                    volumecontainerView.setVisibility(View.INVISIBLE);
                                    bottomview.setVisibility(View.GONE);
                                    toolbar.setVisibility(View.GONE);
                                    playpausebutton.setVisibility(View.VISIBLE);

                                }
                            }, 300);
                        }
                    }


                }


                return false;
            }
        });
        findViewById(R.id.lockbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                islock=true;
                hideSystemUI();
                bottomview.setVisibility(View.GONE);
                toolbar.setVisibility(View.GONE);
                unlockbtn.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        unlockbtn.setVisibility(View.GONE);
                    }
                },2000);

            }
        });
       unlockbtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               islock=false;
               unlockbtn.setVisibility(View.GONE);
               showSystemUI();
               bottomview.setVisibility(View.VISIBLE);
               toolbar.setVisibility(View.VISIBLE);
               new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       hideSystemUI();
                       bottomview.setVisibility(View.GONE);
                       toolbar.setVisibility(View.GONE);
                   }
               },4000);
           }
       });
        soundView.setMaxprogress(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        soundView.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        intializePlayer();
        Button speedbtn = findViewById(R.id.speedbtn);
        speedbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedView.setVisibility(View.VISIBLE);
            }
        });
        SeekBar speedseekbar=findViewById(R.id.speedseekbar);
        TextView speedtextview=findViewById(R.id.speedtextview);
        speedseekbar.setProgress(playbackspeed);
        speedbtn.setText(0.5f+(playbackspeed/10.0f)+"X");
        speedtextview.setText(0.5f+(playbackspeed/10.0f)+"X");
        speedseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                playbackspeed=i;
                PlaybackParameters param = new PlaybackParameters(0.5f+(playbackspeed/10.0f));
                player.setPlaybackParameters(param);
                speedbtn.setText(0.5f+(playbackspeed/10.0f)+"X");
                speedtextview.setText(0.5f+(playbackspeed/10.0f)+"X");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        speedView.setVisibility(View.GONE);
                    }
                },3000);
            }
        });

        startPlayer();
    }
    public void intializePlayer() {


        Uri uri = Uri.parse(videoModels.get(currentitem).getUrl());
        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem,currentitemseek);
        currentitemseek=0;
        videotitle.setText(videoModels.get(currentitem).getName());
        player.prepare();



    }

    public Size getVideoWidthOrHeight(File file, String widthOrHeight) throws IOException {
        MediaMetadataRetriever retriever = null;
        Bitmap bmp = null;
        FileInputStream inputStream = null;
        int mWidth = 0;
        int mheight = 0;
        try {
            retriever = new MediaMetadataRetriever();
            inputStream = new FileInputStream(file.getAbsolutePath());
            retriever.setDataSource(inputStream.getFD());
            bmp = retriever.getFrameAtTime();

            mWidth = bmp.getWidth();

            mheight = bmp.getHeight();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            if (retriever != null) {
                retriever.release();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return new Size(mWidth, mheight);
    }
    // This function is called when user accept or decline the permission.
// Request Code is used to check which permission called this function.
// This request code is provided when user is prompt for permission.


    public String milltominute(long milliseconds) {
        // milliseconds to minutes.
        boolean v = false;
        if (milliseconds < 0) {
            v = true;
            milliseconds = Math.abs(milliseconds);
        }
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        String time = "";
        if (hours == 0) {
            time = String.format("%02d:%02d", minutes, seconds);
        } else {
            time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }

        if (v) return "-" + time;
        else return time;


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!isplaybackground)pausePlayer();

    }

    public ArrayList<String> getAllMedia() {
        HashSet<String> videoItemHashSet = new HashSet<>();
        String[] projection = {MediaStore.Video.VideoColumns.DATA, MediaStore.Video.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        try {
            cursor.moveToFirst();
            do {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                String[] divide = path.split("/");
                Log.e("path", divide[divide.length - 2]);
                videoItemHashSet.add(path);
            } while (cursor.moveToNext());

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> downloadedList = new ArrayList<>(videoItemHashSet);
        return downloadedList;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentitem", currentitem);
        if(isorientionchange)outState.putLong("currentitemseek", player.getCurrentPosition());

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.e("restorecall", currentitem + "");

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void pausePlayer() {
        if(player==null)return;
        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }

    private void startPlayer() {
        player.play();
        Log.e("size", "startplayer");

    }

    @Override
    protected void onDestroy() {
       if(player!=null){
           player.stop();
       player.setVideoSurface(null);
        player.release();
       }

       Log.e("destroy","destroy");
        //player.setVideoSurface(null);

        super.onDestroy();
    }

    @Override
    public void onDetachedFromWindow() {
        Log.e("detach","onDetachedFromWindow");
//

        super.onDetachedFromWindow();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.e("Focus",hasFocus+"");
        if (hasFocus) {
           // hideSystemUI();
        }
   }

    private void hideSystemUI() {

        isshow = false;
        Log.e("hide", "hide");
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

    }

    private void showSystemUI() {
        isshow = true;
//
        Log.e("hide", "show");
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}