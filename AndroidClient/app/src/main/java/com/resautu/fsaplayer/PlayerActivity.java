package com.resautu.fsaplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private ImageView ivAudioImage;
    private Animator playerImageAnimator;
    private ImageButton playModeButton;
    private Handler toastHandler;

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
        playModeButton = findViewById(R.id.play_mode_button);
        nextButton = findViewById(R.id.next_button);
        preButton = findViewById(R.id.previous_button);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvDuration = findViewById(R.id.tv_duration);
        ivAudioImage = findViewById(R.id.music_cover_image_view);

        app = FSAPlayerApplication.getInstance();

        playPauseButton.setOnClickListener(this);
        preButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        playModeButton.setOnClickListener(this);

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
        playerImageAnimator = AnimatorInflater.loadAnimator(this, R.animator.player_image_rotator);
        playerImageAnimator.setTarget(ivAudioImage);
        playerImageAnimator.setInterpolator(new LinearInterpolator());

        Handler.Callback callback = msg -> {
            String path = msg.getData().getString("path");
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                FileUtil.deleteIfExists(path);
                Toast.makeText(this, "无法播放音频, 请检查与服务器连接是否正常", Toast.LENGTH_SHORT).show();
            }
            return true;
        };
        audioPrepareHandler = new Handler(callback);

        Handler.Callback toastCallback = msg -> {
            String toast = msg.getData().getString("toast");
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
            return true;
        };
        toastHandler = new Handler(toastCallback);
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
                Toast.makeText(this, "无法创建文件: " + audioFile.getAbsolutePath() + ", 请检查文件权限是否提供", Toast.LENGTH_SHORT).show();
                return;
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
                } else{
                    throw new RuntimeException("Failed to get audio file: " + audioHashValue);
                }
            } catch (Exception e) {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("toast", "网络连接异常！请检查网络连接并重新连接服务器！");
                msg.setData(bundle);
                toastHandler.sendMessage(msg);
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
                if(playerImageAnimator.isRunning()){
                    playerImageAnimator.pause();
                }
            } else {
                mediaPlayer.start();
                playPauseButton.setImageResource(R.drawable.ic_pause);
                if(playerImageAnimator.isPaused()){
                    playerImageAnimator.resume();
                }
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
        } else if(v.getId() == R.id.play_mode_button){
            if(playMode == PlayMode.LOOP){
                playMode = PlayMode.RANDOM;
                playModeButton.setImageResource(R.drawable.ic_randomplay);
            } else if(playMode == PlayMode.RANDOM){
                playMode = PlayMode.SINGLE;
                playModeButton.setImageResource(R.drawable.ic_singleplay);
            } else if(playMode == PlayMode.SINGLE){
                playMode = PlayMode.LOOP;
                playModeButton.setImageResource(R.drawable.ic_loopplay);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mediaPlayer.seekTo(progress);
            tvCurrentTime.setText(StringUtil.converTime(progress));
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

        if(playerImageAnimator.isPaused()){
            playerImageAnimator.resume();
        } else if(!playerImageAnimator.isRunning()){
            playerImageAnimator.start();
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(StringUtil.converTime(currentPosition));
                    //ivAudioImage.setRotation(ivAudioImage.getRotation() + 0.5f);
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