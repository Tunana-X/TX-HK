package com.example.myapplication;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ALM extends AppCompatActivity {
    MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alm);

        try {
            // 初始化 MediaPlayer
            mMediaPlayer = new MediaPlayer();
            // 设置数据源
            mMediaPlayer = MediaPlayer.create(this, R.raw.music);
            // 获取 AudioManager 服务
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            //这行代码检查 STREAM_ALARM 音频流的音量是否不为0。如果音量为0，表示用户可能已经将闹钟声音静音了，此时不会播放音频
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                // 设置音频循环播放
                mMediaPlayer.setLooping(true);
                // 开始播放音频
                mMediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //弹出对话框
        new AlertDialog.Builder(this).setTitle("闹钟提醒").setMessage("该吃药了")
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //释放 MediaPlayer 资源
                        mMediaPlayer.release();
                        mMediaPlayer= null;
                        finish();
                    }
                }).show();
    }
}

