package com.example.mediaplayer;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import rm.com.audiowave.AudioWaveView;
import rm.com.audiowave.OnProgressListener;

public class MainActivity extends AppCompatActivity {

    TextView file_path;
    Button Selectfile;
    //String path; //to get path
    ImageButton playbutton;
    ImageButton rewindbutton;
    ImageButton forwardbutton;
    TextView t1;
    TextView t2;
    SeekBar seekbar;
    MediaPlayer mediaPlayer;
    AudioWaveView audioWaveView;
    Uri uriS;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        file_path=(TextView)findViewById(R.id.path_show);
        Selectfile=(Button)findViewById(R.id.path_picker);
        playbutton= (ImageButton) findViewById(R.id.play_audio);
        rewindbutton=(ImageButton)findViewById(R.id.rewind);
        forwardbutton=(ImageButton)findViewById(R.id.forward);
        seekbar=(SeekBar)findViewById(R.id.seekbar);
        audioWaveView = (AudioWaveView)findViewById(R.id.wave);
        t1 = (TextView)findViewById(R.id.textview1);
        t2 = (TextView)findViewById(R.id.textview2);
        imageView=(ImageView)findViewById(R.id.imageinmid);

        //image in mid array

        int[] imgArray = {R.drawable.a1,
        R.drawable.a3,
        R.drawable.a4};
        imageView.setImageResource(imgArray[1]);



        //initializing media player
        mediaPlayer = new MediaPlayer();

        //selecting file
        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            //file operation

                            assert data != null;
                            //path = data.getData().getPath();  //gets the directory path
                          // file_path.setText(path);
                            uriS=data.getData();
                            play(this, uriS);
                            //seeking();
                            try {

                                byte[] myByte = convert();
                                assert myByte != null;
                                audioWaveView.setRawData(myByte);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        Selectfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               //Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
              //  fileIntent.setType("*/*");
                Intent fileIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);

                someActivityResultLauncher.launch(fileIntent);




                Toast.makeText(getApplicationContext(),"FILE OPENING", Toast.LENGTH_SHORT).show();


            }
        });


        //selecting file ends...
        //mediaplayer interface
        playbutton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                //pause-play
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    playbutton.setImageResource(R.drawable.ic_baseline_pause_24);
                }
                else
                {
                    mediaPlayer.pause();
                    playbutton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }

                //******************SETS IMAGE USING HANDLER*************************
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    int i = 0;


                    public void run() {
                        imageView.setImageResource(imgArray[i]);
                        i++;
                        if (i > imgArray.length - 1) {
                            i = 0;
                        }
                        handler.postDelayed(this, 5000);
                    }
                };
                handler.postDelayed(runnable, 5000);



                //Using the Seekbar
                int duration=mediaPlayer.getDuration();

                seekbar.setMax(duration);
                    //getting current position
                    //create a handler and post runnable in the UI thread so we can update the SeekBar
                    Handler mediaHandler = new Handler();
                    //keeps updating and along with that thread keeps updating seekbar,timer,waveform
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mediaPlayer != null)
                            {
                                int currentPosition = mediaPlayer.getCurrentPosition();
                                //seekbars position
                                seekbar.setProgress(currentPosition);
                                //audiowave's position
                                final float audioWaveProgress = (currentPosition / (float) duration) * 100F;
                                audioWaveView.setProgress(audioWaveProgress);
                                //updates timers here
                                t1.setText(converttominutes(currentPosition/1000));
                                t2.setText(converttominutes(duration/1000));

                            }
                            mediaHandler.postDelayed(this, 1000);
                            //updates every second
                        }
                    });

                    //user drag to change seekbar and media
                    seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            if(mediaPlayer != null && b)
                            {
                                mediaPlayer.seekTo(i);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                    //user drags to change waveform to seek media times
              /*  audioWaveView.setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onStartTracking(float v) {

                    }

                    @Override
                    public void onStopTracking(float v) {

                    }

                    @Override
                    public void onProgressChanged(float v, boolean b) {
                        if(mediaPlayer != null && b)
                        {
                            mediaPlayer.seekTo((int) (v));
                        }

                    }
                });*/

            }

            });

        rewindbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);

            }
        });

        forwardbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);

            }
        });




    }
    private String converttominutes(int text)
    {
        int minutes = (text%3600)/60;
        int seconds = text%60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        return  timeString;
    }


    //player initialization
    private void play(ActivityResultCallback<ActivityResult> context, Uri myUri){

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), myUri);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Toast.makeText(MainActivity.this, "Media Playing", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //**************************WAVEFORMS BYTECODE************************************
    public byte[] convert() throws IOException {

        InputStream inputStream =
                getContentResolver().openInputStream(uriS);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];

        for (int readNum; (readNum = inputStream.read(b)) != -1;) {
            bos.write(b, 0, readNum);
        }

        byte[] bytes = bos.toByteArray();

        return bytes;
    }


//media player ends

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
    }

    private void clearMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }



}