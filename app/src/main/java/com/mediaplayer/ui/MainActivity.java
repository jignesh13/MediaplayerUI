package com.mediaplayer.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.ui.PlayerView;


public class MainActivity extends AppCompatActivity {
    private float downy;
    private float endheight;
    private float diffheight;
    private int currentprogress;
    private int currentseek;
    private Handler handler;
    private float lastx;
    private float putx,puty;
    private float trackx;
    private float finalwidth;
    private int lastprogress;
    private long lasttime;
    private int selected = 0;
    private boolean isshow;
    private PlayerView playerView;
    private boolean first=true,second=true,third = true;
    private ExoPlayer player;
    private SeekBar dragseek;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isshow=false;
        hideSystemUI();
        dragseek=findViewById(R.id.dragseek);
        ImageView playpausebutton = findViewById(R.id.imageButton);

        TextView currentprogresslbl=findViewById(R.id.currentprogress);
        TextView endprogresslbl=findViewById(R.id.endprogress);

        handler=new Handler();

        player = new ExoPlayer.Builder(this).build();

         playerView=findViewById(R.id.player);
        playerView.setPlayer(player);


//http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4
        MediaItem mediaItem = MediaItem.fromUri("asset:///test.mp4");
// Set the media item to be played.
        player.setMediaItem(mediaItem);
        player.prepare();
        playpausebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(player.isPlaying())player.pause();
                else player.play();
            }
        });
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                dragseek.setProgress((int) player.getCurrentPosition());
              //  currentprogresslbl.setText(milltominute(player.getCurrentPosition()));
                handler.postDelayed(this::run,1000);
            }
        };
        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if(isPlaying){
                    playpausebutton.setImageResource(R.drawable.ic_baseline_pause_24);
                    endprogresslbl.setText(milltominute(player.getDuration()));
                    dragseek.setMax((int) player.getDuration());
                handler.postDelayed(runnable,0);
                }
                else {
                    playpausebutton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                   handler.removeCallbacks(runnable);
                }
            }
        });
        player.setSeekParameters(SeekParameters.CLOSEST_SYNC);
        dragseek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if(b){
                    player.seekTo(i);

                }
                currentprogresslbl.setText(milltominute(i));



            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                player.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                player.play();
            }
        });
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        SoundView soundView= findViewById(R.id.volumeview);
        TextView progresstext=findViewById(R.id.progresstext);
        ImageView volumeview=findViewById(R.id.volumeicon);
        View volumecontainerView = findViewById(R.id.volumecontainer);
        soundView.setOnsoundProgressChangeListner(new SoundProgressChangeListner() {
            @Override
            public void onchange(int progress) {
                Log.e("change",progress+"");
                progresstext.setText(progress+"");
                if(progress==0){
                    volumeview.setImageResource(R.drawable.ic_baseline_volume_off_24);
                }
                else {
                    volumeview.setImageResource(R.drawable.ic_baseline_volume_up_24);
                }


            }
        });

        View bottomview=findViewById(R.id.bottomview);
        View touchview=findViewById(R.id.toucher);

        touchview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    lastx=motionEvent.getX();
                    downy=motionEvent.getY();
                    putx=motionEvent.getX();
                    puty=motionEvent.getY();
                    lasttime=System.currentTimeMillis();
                    endheight=downy-getResources().getDimensionPixelSize(R.dimen.widthmeasure);
                    diffheight=endheight-downy;
                    currentprogress=soundView.getProgress();
                    first=true;
                    second=true;
                    third=true;
                    selected=0;
                    trackx=motionEvent.getX();
                    currentseek=dragseek.getProgress();
                    finalwidth=motionEvent.getX()+touchview.getWidth();
                    Log.e("width",touchview.getWidth()+"");

                }
                else if(motionEvent.getAction()==MotionEvent.ACTION_MOVE){
                    Log.e("myrb","x="+lastx+"y="+downy+"");
                    Log.e("myrb","x="+motionEvent.getX()+"y="+motionEvent.getY()+","+motionEvent.getAction());
                    float xdistance = motionEvent.getX()-lastx;
                    float ydistance = motionEvent.getY()-downy;
                    if(first && Math.abs(xdistance)==0 && Math.abs(ydistance)==0){


                    }
                    else if((second && Math.abs(xdistance)<Math.abs(ydistance))||selected==1){
                        if(selected==0){
                            selected=1;
                            first=false;
                            second=true;
                            third=false;
                            volumecontainerView.setVisibility(View.VISIBLE);
                        }
                        float tempwidth=endheight-motionEvent.getY();
                        float progress = (tempwidth*soundView.getMaxprogess())/diffheight;
                        Log.e("progress",(soundView.getMaxprogess()-progress)+"");
                        int jprogress=(int)(soundView.getMaxprogess()-progress);
                        int prog=currentprogress+jprogress;
                        Log.e("myprogress",(prog)+"");
                        if(prog > soundView.getMaxprogess()) soundView.setProgress(soundView.getMaxprogess());
                        else if(prog < 0)soundView.setProgress(0);
                        else{
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, prog, 0);
                            soundView.setProgress(prog);
                        }

                        Log.e("scroll","vertical");
                    }
                    else if(third||selected==2){

                        if(selected==0){

                            second=false;
                            first=false;
                            third=true;
                            selected=2;
                            playpausebutton.setVisibility(View.GONE);
                            bottomview.setVisibility(View.VISIBLE);

                        }

                        int progress = (int) ((60000*(motionEvent.getX()-trackx))/touchview.getWidth());

                        if(lastprogress!=progress){
                            dragseek.setProgress(currentseek+progress);
                            player.seekTo(currentseek+progress);
                            Log.e("scroll","horizontal"+dragseek.getProgress());
                        }
                        lastprogress=progress;



                    }
                    lastx=motionEvent.getX();
                    downy=motionEvent.getY();
                }
                else {
                    if(motionEvent.getX()==putx&&motionEvent.getY()==puty && System.currentTimeMillis()-lasttime<=1000){
                        if(isshow){
                            hideSystemUI();
                            bottomview.setVisibility(View.GONE);
                        }
                        else {
                            showSystemUI();
                            playpausebutton.setVisibility(View.VISIBLE);
                            bottomview.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(player.isPlaying()){
                                        hideSystemUI();
                                        bottomview.setVisibility(View.GONE);
                                    }

                                }
                            },2500);
                        }
                    }
                    else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                hideSystemUI();
                                volumecontainerView.setVisibility(View.INVISIBLE);
                                bottomview.setVisibility(View.GONE);
                                playpausebutton.setVisibility(View.VISIBLE);

                            }
                        },300);
                    }

                }



            return false;
            }
        });
        soundView.setMaxprogress(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
       soundView.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        startPlayer();
    }

    public String milltominute(long milliseconds){
        // milliseconds to minutes.
        String minutes = ((milliseconds / 1000) / 60)+"";
        // formula for conversion for
        // milliseconds to seconds
        String seconds = ((milliseconds / 1000) % 60)+"";
        if(minutes.length()==1)minutes="0"+minutes;
        if(seconds.length()==1)seconds="0"+seconds;
        return minutes+":"+seconds;

    }
    @Override
    protected void onPause() {
        super.onPause();
        pausePlayer();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    private void pausePlayer(){
        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }
    private void startPlayer(){
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        View decorView = getWindow().getDecorView();
        isshow=false;
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

    }

    private void showSystemUI() {
        isshow=true;
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);


    }
}