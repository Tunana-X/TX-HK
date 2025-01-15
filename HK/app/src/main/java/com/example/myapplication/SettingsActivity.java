package com.example.myapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
public class SettingsActivity extends AppCompatActivity {

    private EditText emergencyContactEditText;
    private Button saveEmergencyContactButton;
    private Button backToMainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        emergencyContactEditText = findViewById(R.id.emergencyContactEditText);
        saveEmergencyContactButton = findViewById(R.id.saveEmergencyContactButton);
        backToMainButton = findViewById(R.id.backToMainButton);
        loadEmergencycontactNumber();
        saveEmergencyContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emergencyContactEditText.length()==11) {
                    saveEmergencyContactNumber(emergencyContactEditText.getText().toString());
                    Toast.makeText(SettingsActivity.this, "紧急联系人已保存", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(SettingsActivity.this, "紧急联系人保存失败，联系号码格式错误", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backToMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 关闭当前Activity，返回到上一个Activity
            }
        });
    }
    private void loadEmergencycontactNumber(){
        SharedPreferences sharedPreferences = getSharedPreferences("EmergencyContact",MODE_PRIVATE);
        String emergencyContactNumber = sharedPreferences.getString("emergencyContactNumber","");
        emergencyContactEditText.setText(emergencyContactNumber);
    }
    private void saveEmergencyContactNumber(String number) {
        SharedPreferences sharedPreferences = getSharedPreferences("EmergencyContact", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("emergencyContactNumber", number);
        editor.apply();
    }
}