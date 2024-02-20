package com.resautu.fsaplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.resautu.fsaplayer.data.ItemData;
import com.resautu.fsaplayer.utils.FileUtil;
import com.resautu.fsaplayer.utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Response;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private ImageButton preButton;
    private TextView tvCurrentTime;
    private TextView tvDuration;

    private enum PlayMode {
        SINGLE, LOOP, RANDOM
    }
    private PlayMode playMode = PlayMode.LOOP;
    private TextView tvSongName;
    private TextView tvSongDesc;
    private SeekBar seekBar;
    private ImageButton playPauseButton;
    private ImageButton nextButton;
    private String audioName;
    private String audioDesc;
    private String audioHashValue;
    private MediaPlayer mediaPlayer;
    private String serverAddress;
    private FSAPlayerApplication app;
    private final Handler handler = new Handler();
    private Handler audioPrepareHandler;
    private File cacheDir;
    private List<ItemData> audioItemList;
    private int audioPlayingIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        tvSongName = findViewById(R.id.tv_song_name);
        tvSongDesc = findViewById(R.id.tv_song_desc);
        seekBar = findViewById(R.id.seek_bar);
        playPauseButton = findViewById(R.id.play_pause_button);
        nextButton = findViewById(R.id.next_button);
        preButton = findViewById(R.id.previous_button);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvDuration = findViewById(R.id.tv_duration);

        app = FSAPlayerApplication.getInstance();

        playPauseButton.setOnClickListener(this);
        preButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);

        audioItemList = app.getAudioItemList();

        audioPlayingIndex = getIntent().getIntExtra("Id", 0);
        audioName = audioItemList.get(audioPlayingIndex).getItemName();
        audioDesc = getIntent().getStringExtra("Desc");
        audioHashValue = audioItemList.get(audioPlayingIndex).getHashValue();
        serverAddress = getIntent().getStringExtra("ServerAddress");
        cacheDir = this.getCacheDir();


        tvSongName.setText(audioName);
        tvSongDesc.setText(audioDesc);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);

        Handler.Callback callback = msg -> {
            String path = msg.getData().getString("path");
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        };
        audioPrepareHandler = new Handler(callback);
        prepareAudio();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    private void prepareAudio(){
        seekBar.setEnabled(false);
        File audioFile = new File(cacheDir, audioHashValue);
        if(audioFile.exists()){
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("path", audioFile.getAbsolutePath());
            msg.setData(bundle);
            audioPrepareHandler.sendMessage(msg);
            return;
        }
        else{
            try {
                audioFile.createNewFile();
            } catch (IOException e) {
                Log.e("PlayerActivity", "Failed to create audio file: " + audioFile.getAbsolutePath());
                throw new RuntimeException(e);
            }
        }
        Runnable r = () -> {
            try {
                Response response = app.httpClient.sendSyncGetRequest("/audio?hash=" + audioHashValue);
                if (response.isSuccessful()) {
                    byte[] bytes = response.body().bytes();

                    FileUtil.writeBytesToFile(bytes, audioFile);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("path", audioFile.getAbsolutePath());
                    msg.setData(bundle);
                    audioPrepareHandler.sendMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(r).start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.play_pause_button) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseButton.setImageResource(R.drawable.ic_play);
            } else {
                mediaPlayer.start();
                playPauseButton.setImageResource(R.drawable.ic_pause);
            }
        } else if(v.getId() == R.id.next_button){
            audioPlayingIndex = (audioPlayingIndex + 1) % audioItemList.size();
            audioName = audioItemList.get(audioPlayingIndex).getItemName();
            //audioDesc = audioItemList.get(audioPlayingIndex).getItemDesc();
            audioHashValue = audioItemList.get(audioPlayingIndex).getHashValue();
            tvSongName.setText(audioName);
            tvSongDesc.setText(audioDesc);
            mediaPlayer.reset();
            prepareAudio();
        } else if(v.getId() == R.id.previous_button){
            audioPlayingIndex = (audioPlayingIndex - 1 + audioItemList.size()) % audioItemList.size();
            audioName = audioItemList.get(audioPlayingIndex).getItemName();
            //audioDesc = audioItemList.get(audioPlayingIndex).getItemDesc();
            audioHashValue = audioItemList.get(audioPlayingIndex).getHashValue();
            tvSongName.setText(audioName);
            tvSongDesc.setText(audioDesc);
            mediaPlayer.reset();
            prepareAudio();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mediaPlayer.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        seekBar.setMax(mp.getDuration());
        seekBar.setEnabled(true);
        int duration = mp.getDuration();
        tvDuration.setText(StringUtil.converTime(duration));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(StringUtil.converTime(currentPosition));
                }
                handler.postDelayed(this, 1000); // 每秒更新一次SeekBar的位置
            }
        }, 0);
        playPauseButton.setImageResource(R.drawable.ic_pause);
        mp.start();
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        if(playMode == PlayMode.LOOP){
            audioPlayingIndex = (audioPlayingIndex + 1) % audioItemList.size();
            audioName = audioItemList.get(audioPlayingIndex).getItemName();
            //audioDesc = audioItemList.get(audioPlayingIndex).getItemDesc();
            audioHashValue = audioItemList.get(audioPlayingIndex).getHashValue();
            tvSongName.setText(audioName);
            tvSongDesc.setText(audioDesc);
            mp.reset();
            prepareAudio();
        } else if(playMode == PlayMode.RANDOM){
            audioPlayingIndex = (int) (Math.random() * audioItemList.size());
            audioName = audioItemList.get(audioPlayingIndex).getItemName();
            //audioDesc = audioItemList.get(audioPlayingIndex).getItemDesc();
            audioHashValue = audioItemList.get(audioPlayingIndex).getHashValue();
            tvSongName.setText(audioName);
            tvSongDesc.setText(audioDesc);
            mp.reset();
            prepareAudio();
        } else if(playMode == PlayMode.SINGLE){
            mp.start();
        }
    }
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("PlayerActivity", "MediaPlayer error: " + what + ", " + extra);
        return false;
    }
}