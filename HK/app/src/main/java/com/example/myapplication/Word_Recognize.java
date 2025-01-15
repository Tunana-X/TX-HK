package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.IOException;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class Word_Recognize extends AppCompatActivity implements TextToSpeech.OnInitListener {
    //文字识别模块的变量
    private static final int REQUEST_IMAGE_PICK = 1;
    private Uri selectedImageUri;
    private ImageView imageView;
    private TextView textResult;

    //语音的变量
    private TextToSpeech textToSpeech;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_word_recognize);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        imageView = findViewById(R.id.imageView);
        textResult = findViewById(R.id.textResult);
        Button selectImageButton = findViewById(R.id.selectImageButton);
        Button recognizeTextButton = findViewById(R.id.recognizeTextButton);

        Button speakButton = findViewById(R.id.speakButton); // 新增的语音播报按钮
        Button stopSpeakButton = findViewById(R.id.stopSpeakButton); // 中断语音按钮

        selectImageButton.setOnClickListener(v -> selectImage());
        recognizeTextButton.setOnClickListener(v -> recognizeText());

        // 初始化 TextView 和 Button
        textResult = findViewById(R.id.textResult); // TextView 用于显示识别结果


        // 设置按钮点击事件
        speakButton.setOnClickListener(v -> {
            // 假设识别文本保存在 textResult 中
            String recognizedText = textResult.getText().toString();  // 获取识别的文本
            readText(recognizedText); // 调用朗读方法
        });


        // 设置按钮点击事件
        selectImageButton.setOnClickListener(v -> selectImage());
        recognizeTextButton.setOnClickListener(v -> recognizeText());
        speakButton.setOnClickListener(v -> {
            String recognizedText = textResult.getText().toString();
            readText(recognizedText);
        });
        stopSpeakButton.setOnClickListener(v -> stopSpeaking()); // 设置中断按钮点击事件
        // 初始化 TextToSpeech 对象
        textToSpeech = new TextToSpeech(this, this);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
        }
    }
    private void recognizeText() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "请先选择一张图片！", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            TextRecognizer recognizer = TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
            recognizer.process(image)
                    .addOnSuccessListener(visionText -> textResult.setText(visionText.getText()))
                    .addOnFailureListener(e -> textResult.setText("识别失败: " + e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();

            Toast.makeText(this, "无法加载图片！", Toast.LENGTH_LONG).show();
        }
    }


    //开始语音
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // 设置语言为中文
            int langResult = textToSpeech.setLanguage(Locale.CHINA); // 设置为中文
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // 语言数据缺失或不支持的处理
                Toast.makeText(this, "语音功能不可用，请检查语言支持", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "初始化语音失败", Toast.LENGTH_LONG).show();        }
    }

    // 在 Activity 销毁时释放 TTS 资源
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    // 实现文本朗读方法
    public void readText(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null); // 启动朗读
        }
    }
    // 停止语音播报
    public void stopSpeaking() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop(); // 停止当前语音播放
            Toast.makeText(this, "语音播报已中断", Toast.LENGTH_SHORT).show();
        }
    }


}
