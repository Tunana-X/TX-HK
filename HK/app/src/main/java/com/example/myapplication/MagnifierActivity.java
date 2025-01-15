package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.SeekBar;


import android.media.MediaRecorder;
import java.io.IOException;
import android.media.AudioRecord;
import android.media.AudioFormat;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONException;
import android.os.Handler;

import android.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import android.os.Looper;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
//import java.util.Base64;

public class MagnifierActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private float zoomLevel ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_magnifier);
        initZoomTask(); // 初始化zoomTask
        startZoomTask(); // 开始执行zoomTask
        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceHolderCallback);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 这里将滑块的值转换为放大倍数

                zoomLevel = progress / 100f;  //原始值为100
                adjustZoom(zoomLevel);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 用户开始滑动时的回调
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 用户停止滑动时的回调
            }
        });



    }

    private Handler handler2 = new Handler(Looper.getMainLooper());
    private volatile boolean isRecording = true;

    //1录音过程
    private String saveAudioToFile() {
        File audioFile = new File(getExternalFilesDir(null), "record.pcm");
        int audioSource = MediaRecorder.AudioSource.MIC;
        int sampleRateInHz = 16000; // 采样率
        int channelConfig = AudioFormat.CHANNEL_IN_MONO; // 单声道
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT; // 16位PCM编码
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

        if (ContextCompat.checkSelfPermission(MagnifierActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MagnifierActivity.this, "需要录音权限", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MagnifierActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 3);
        }
        AudioRecord audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);

        audioRecord.startRecording();

        byte[] audioData = new byte[bufferSizeInBytes];
        long totalBytesRecorded = 0; // 用于存储总字节数
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(audioFile);
            long startTime = System.currentTimeMillis();
            while (isRecording && (System.currentTimeMillis() - startTime) < 2000) {
                int readSize = audioRecord.read(audioData, 0, audioData.length);
                if (readSize > 0) {
                    fos.write(audioData, 0, readSize);
                    totalBytesRecorded += readSize;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            audioRecord.stop();
            audioRecord.release();
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return audioFile.getAbsolutePath();
    }
    //2 从文件中读取数据并进行Base64编码
    private String getBase64FromAudioFile(String filePath, long totalBytesRecorded) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bao.write(buffer, 0, bytesRead);
            }
            String base64PCMData = Base64.encodeToString(bao.toByteArray(), Base64.DEFAULT);
            return base64PCMData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    //3总过程
    private void voiceRecord() {
        // 检查录音权限
        if (ContextCompat.checkSelfPermission(MagnifierActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            handler2.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MagnifierActivity.this, "需要录音权限", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(MagnifierActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 3);
                }
            });
            return; // 没有权限，直接返回
        }

        new Thread(() -> {
            while(isRecording) {
                handler2.post(() -> handleResult());
                try {
                    // 暂停2秒（2000毫秒）
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // 处理中断异常
                    e.printStackTrace();
                }
//                try {
////                    String audioFilePath = saveAudioToFile();
////                    long totalBytesRecorded = new File(audioFilePath).length(); // 获取文件的总字节数
////                    String base64PCMData = getBase64FromAudioFile(audioFilePath, totalBytesRecorded);
////                    String voiceResult = baiduVoice(base64PCMData, totalBytesRecorded);
//
//                } catch (IOException e) {
//                    handler2.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(MagnifierActivity.this, "开始录音时出错", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    e.printStackTrace();
//                }
            }

        }).start();
    }

    //4百度api
    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();
    static String myToken = "24.18b3f781a7d976fa838fb332e88d8e40.2592000.1734792663.282335-116328923";
    public String baiduVoice(String A,long L) throws IOException{
        int Lint = (int)L;
        String mySpeech = A.replaceAll("\\s+", "");
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,"{\"format\":\"pcm\",\"rate\":16000,\"channel\":1,\"cuid\":\"lsSvmwccYfBTRSJ1VDtyMpUao79enf06\",\"token\":\""+myToken+"\",\"speech\":\""+mySpeech+"\",\"len\":" + Lint + "}");
        Request request = new Request.Builder()
                .url("https://vop.baidu.com/server_api")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        String responseString = response.body().string(); // 获取响应字符串
        return responseString;
    }

    private Handler handler = new Handler();
    private Runnable zoomTask;
    private boolean shouldZoomIn = false;
    private boolean shouldZoomOut = false;

    //5.处理结果
    public void handleResult() {
//        JSONObject jsonObject = null;
//        try {
//            jsonObject = new JSONObject(result);
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
        String resultText = null;
        SharedPreferences sharedPreferences = getSharedPreferences("SharedData", MODE_PRIVATE);
        resultText = sharedPreferences.getString("result_text", "");

        if (resultText.contains("放大")&&!resultText.contains("放大镜")) {
            shouldZoomIn = true;
            shouldZoomOut = false;
            handler.post(zoomTask);
        } else if (resultText.contains("缩小")) {
            shouldZoomIn = false;
            shouldZoomOut = true;
            handler.post(zoomTask);
        } else if (resultText.contains("停止")) {
            shouldZoomIn = false;
            shouldZoomOut = false;
            // 停止任务
            handler.removeCallbacks(zoomTask);
        }
    }
    // 初始化Runnable
    private void initZoomTask() {
        zoomTask = new Runnable() {
            @Override
            public void run() {
                if (shouldZoomIn) {
                    if(zoomLevel<1){
                        zoomLevel += 0.01;
                    }
                    adjustZoom(zoomLevel);
                    // 更新 SeekBar 的值
                    SeekBar seekBar = findViewById(R.id.seekBar);
                    seekBar.setProgress((int) (zoomLevel * 100)); // 0 - 100 对应滑块值
                } else if (shouldZoomOut) {
                    if(zoomLevel>0){
                        zoomLevel -= 0.01;
                    }
                    adjustZoom(zoomLevel);
                    // 更新 SeekBar 的值
                    SeekBar seekBar = findViewById(R.id.seekBar);
                    seekBar.setProgress((int) (zoomLevel * 100)); // 0 - 100 对应滑块值
                }
                // 检查是否需要继续执行
                if (shouldZoomIn || shouldZoomOut) {
                    handler.postDelayed(this, 100); // 每秒更新一次
//                    handler.postDelayed(this, 100);
                }
            }
        };
    }
    private void startZoomTask() {
        handler.post(zoomTask); // 立即执行zoomTask
    }

    //下面不用动
    private void adjustZoom(float zoomLevel) {
        // 这里需要根据您的摄像头API和具体的放大镜实现来调整
        // 以下代码是一个示例，您需要根据实际情况进行调整
        if (camera != null) {
            Camera.Parameters params = camera.getParameters();
            // 假设您的摄像头支持设置缩放参数
            params.setZoom((int) (zoomLevel * params.getMaxZoom()));
            camera.setParameters(params);
        }
    }
    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (ContextCompat.checkSelfPermission(MagnifierActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MagnifierActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                startCamera();
            }
            String[] permissions = {
                    Manifest.permission.RECORD_AUDIO,
//                    Manifest.permission.WRITE_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_AUDIO
            };
            boolean flag = true;
            for (int i=0;i<permissions.length;i++) {
                if (ContextCompat.checkSelfPermission(MagnifierActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    flag = false;
                    break;
                }
            }
            if (flag==false) {
                ActivityCompat.requestPermissions(MagnifierActivity.this,  permissions, 2);
            } else {
                voiceRecord();
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stopCamera();
        }
    };

    private void startCamera() {
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            Toast.makeText(this, "无法启动摄像头", Toast.LENGTH_SHORT).show();
            releaseCamera();
        }
    }

    private void stopCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "需要摄像头权限", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 2) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    // 如果任何一个权限被拒绝，可以在这里处理
                    Toast.makeText(this, "需要权限：" + permissions[i], Toast.LENGTH_SHORT).show();
                }
            }
            // 如果所有权限都被授予，可以执行需要这些权限的操作
            voiceRecord();
        }
        if (requestCode == 3) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "需要录音权限", Toast.LENGTH_SHORT).show();
            }
            // 如果所有权限都被授予，可以执行需要这些权限的操作
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
        handler.removeCallbacks(zoomTask);
        handler = null;
        handler2 = null;
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
