package com.example.musicplayer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

import static android.media.MediaPlayer.create;

public class PlayerActivity extends AppCompatActivity {

    Button btnplay, btnnext, btnprev, btnFastF, btnFastP;
    TextView txtsn, txtsstart, txtsstop;
    SeekBar seekbar;
    BarVisualizer bar;
    ImageView imageview;

    String sName;
    public static final String Extra_Name = "Song_Name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySong;
    Thread updateSeekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(bar!=null){
            bar.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getSupportActionBar().setTitle("Playing Now");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnprev = findViewById(R.id.btnprev);
        btnnext = findViewById(R.id.btnnext);
        btnplay = findViewById(R.id.btnplay);
        btnFastF = findViewById(R.id.btnFastF);
        btnFastP = findViewById(R.id.btnFastP);
        txtsn = findViewById(R.id.txtsn);
        txtsstart = findViewById(R.id.txtsstart);
        txtsstop = findViewById(R.id.txtsstop);
        seekbar = findViewById(R.id.seekbar);
        bar = findViewById(R.id.bar);
        imageview = findViewById(R.id.imageview);

        if(mediaPlayer!= null){

            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySong = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songName");
        position = bundle.getInt("pos", 0);
        txtsn.setSelected(true);
        Uri uri = Uri.parse(mySong.get(position).toString());
        sName = mySong.get(position).getName();
        txtsn.setText(sName);

        mediaPlayer = create(getApplicationContext(), uri);
        mediaPlayer.start();
        updateSeekbar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition =0;
                while(currentPosition<totalDuration){
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekbar.setProgress(currentPosition);

                    }
                    catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }

            }
        };
        seekbar.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        seekbar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.MULTIPLY);
        seekbar.getThumb().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.SRC_IN);


        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());

            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        txtsstop.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtsstart.setText(currentTime);
                handler.postDelayed(this, delay);

            }
        }, delay);

        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    btnplay.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }
                else{
                    btnplay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });

        //next listener

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnnext.performClick();
            }
        });


        int audioSessionId = mediaPlayer.getAudioSessionId();
        if(audioSessionId !=-1){
            bar.setAudioSessionId(audioSessionId);
        }


        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)%mySong.size());
                Uri u = Uri.parse(mySong.get(position).toString());
                mediaPlayer = create(getApplicationContext(), u);
                sName = mySong.get(position).getName();
                txtsn.setText(sName);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageview);
                int audioSessionId = mediaPlayer.getAudioSessionId();
                if(audioSessionId !=-1){
                    bar.setAudioSessionId(audioSessionId);
                }
                String endTime = createTime(mediaPlayer.getDuration());
                txtsstop.setText(endTime);
            }

        });

        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1)<0)?(mySong.size()-1):(position-1);
                Uri u = Uri.parse(mySong.get(position).toString());
                mediaPlayer = create(getApplicationContext(), u);
                sName = mySong.get(position).getName();
                txtsn.setText(sName);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageview);
                int audioSessionId = mediaPlayer.getAudioSessionId();
                if(audioSessionId !=-1){
                    bar.setAudioSessionId(audioSessionId);
                }
                String endTime = createTime(mediaPlayer.getDuration());
                txtsstop.setText(endTime);
            }

        });

        btnFastF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+15000);
                }
            }
        });
        btnFastP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-15000);
                }
            }
        });

    }

    public void startAnimation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageview, "rotation", 0f, 360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();

    }

    public String createTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time+=min+":";
        if(sec<10){
            time+="0";
        }
        time+=sec;
        return time;
    }

}