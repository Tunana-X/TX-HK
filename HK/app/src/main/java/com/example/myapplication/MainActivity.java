package com.example.myapplication;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.IOException;
import android.os.Bundle;
import android.net.Uri;
import android.content.SharedPreferences;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private Button settingEmergencyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化小秘书按钮
        Button assistanceButton = findViewById(R.id.Assistancebutton);
        assistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Assistance.class);
                startActivity(intent);
            }
        });
        //初始化图象识别按钮
        Button ImageRecognizeButton = findViewById(R.id.imageButton);
        ImageRecognizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Word_Recognize.class);
                startActivity(intent);
            }
        });
        //初始化放大镜按钮
        Button magnifierButton = findViewById(R.id.magnifierButton);
        magnifierButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MagnifierActivity.class);
                startActivity(intent);
            }
        });

        // 初始化紧急呼叫按钮
        Button emergencyButton = findViewById(R.id.emergencyButton);
        emergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "呼叫中...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + getEmergencyContactNumber()));
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE}, 1);
                }
            }
        });
        //初始化ai接口按钮
        Button settingAIButton = findViewById(R.id.chataiButton);
        settingAIButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TalkingRobot_Activity.class);
                startActivity(intent);
            }
        });
        //初始化书签按钮
        Button bookButton = findViewById(R.id.bookButton);
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, book.class);
                startActivity(intent);
            }
        });


        // 初始化设置紧急联系人按钮
        settingEmergencyButton = findViewById(R.id.setting_emergencyButton);
        settingEmergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        voiceRecord();
    }

    private String getEmergencyContactNumber() {
        SharedPreferences sharedPreferences = getSharedPreferences("EmergencyContact", MODE_PRIVATE);
        return sharedPreferences.getString("emergencyContactNumber", "13602719980"); // 默认电话号码
    }


    private Handler handler2 = new Handler(Looper.getMainLooper());
    private volatile boolean isRecording = true;

    //实现语音输入
    private String saveAudioToFile() {
        File audioFile = new File(getExternalFilesDir(null), "record.pcm");
        int audioSource = MediaRecorder.AudioSource.MIC;
        int sampleRateInHz = 16000; // 采样率
        int channelConfig = AudioFormat.CHANNEL_IN_MONO; // 单声道
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT; // 16位PCM编码
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "需要录音权限", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 3);
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
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            handler2.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "需要录音权限", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 3);
                }
            });
            return; // 没有权限，直接返回
        }

        new Thread(() -> {
            while (isRecording) {
                try {
                    String audioFilePath = saveAudioToFile();
                    long totalBytesRecorded = new File(audioFilePath).length(); // 获取文件的总字节数
                    String base64PCMData = getBase64FromAudioFile(audioFilePath, totalBytesRecorded);
                    String voiceResult = baiduVoice(base64PCMData, totalBytesRecorded);
                    Log.d("BaiduVoice", "识别结果: " + voiceResult);  // 打印返回的原始结果
                    handler2.post(() -> handleResult(voiceResult));
                } catch (IOException e) {
                    handler2.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "开始录音时出错", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }
            }

        }).start();
    }

    //4百度api
    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();
    static String myToken = "25.897bfb292c0555046bb14955b7b5e319.315360000.2052209186.282335-116328923";

    public String baiduVoice(String A, long L) throws IOException {
        int Lint = (int) L;
        String mySpeech = A.replaceAll("\\s+", "");
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"format\":\"pcm\",\"rate\":16000,\"channel\":1,\"cuid\":\"lsSvmwccYfBTRSJ1VDtyMpUao79enf06\",\"token\":\"" + myToken + "\",\"speech\":\"" + mySpeech + "\",\"len\":" + Lint + "}");
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



    //5.处理结果
    public void handleResult(String result) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        String resultText = null;

        try {
            final String text = jsonObject.getString("result");
            // 如果输入为空或仅包含空格，直接返回
            if (text == null || text.trim().equals("[]") || text.trim().equals("[\"\"]")) {
                return;
            }
            resultText = text;
            updateResultText(resultText);

            handler2.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
                }
            });
        }  catch (JSONException e) {
            handler2.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "识别出错", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        if (resultText.contains("聊天") || resultText.contains("机器人")) {
            Intent intent = new Intent(MainActivity.this, TalkingRobot_Activity.class);
            startActivity(intent);
        } else if (resultText.contains("书签")) {
            Intent intent = new Intent(MainActivity.this, book.class);
            startActivity(intent);
        } else if (resultText.contains("放大镜")) {
            Intent intent = new Intent(MainActivity.this, MagnifierActivity.class);
            startActivity(intent);
        } else if (resultText.contains("图像") || resultText.contains("识别")) {
            Intent intent = new Intent(MainActivity.this, Word_Recognize.class);
            startActivity(intent);
        } else if (resultText.contains("呼叫")) {
            Toast.makeText(MainActivity.this, "呼叫中...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + getEmergencyContactNumber()));
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                startActivity(intent);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, 1);
            }
        } else if (resultText.contains("联系人")) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (resultText.contains("秘书")) {
            Intent intent = new Intent(MainActivity.this, Assistance.class);
            startActivity(intent);
        } else if (resultText.contains("主页")) {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void updateResultText(String resultText) {
        SharedPreferences sharedPreferences = getSharedPreferences("SharedData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("result_text", resultText);
        editor.apply();
    }
}
