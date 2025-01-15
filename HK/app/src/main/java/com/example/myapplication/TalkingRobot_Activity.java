package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//配置应用信息

public class TalkingRobot_Activity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private Button buttonSend;
    private ChatAdapter chatAdapter;
    private final List<Message> chatMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talking_robot);

        recyclerView = findViewById(R.id.recyclerView);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        // 初始化 RecyclerView
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
        initAIResponse();
        // 监听发送按钮
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = editTextMessage.getText().toString().trim();
                if (!userMessage.isEmpty()) {
                    // 添加用户消息到列表
                    chatMessages.add(new Message(userMessage, true));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerView.scrollToPosition(chatMessages.size() - 1);

                    // 清空输入框
                    editTextMessage.setText("");

                    // 模拟 AI 回复
                    simulateAIResponse(userMessage);
                }
            }
        });
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    private void AIResponse(String Response) {
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 添加 AI 回复到列表
                String aiMessage = Response;
                chatMessages.add(new Message(aiMessage, false));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                recyclerView.scrollToPosition(chatMessages.size() - 1);
            }
        }, 1000); // 模拟延迟 1 秒
    }

    private void initAIResponse() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                JSONObject jsonBody = new JSONObject();
                try {
                    JSONArray messagesArray = new JSONArray();
                    JSONObject userMessageObject1 = new JSONObject(); // 修改后的名称
                    JSONObject userMessageObject2 = new JSONObject(); // 修改后的名称
                    userMessageObject1.put("role", "System");
                    userMessageObject1.put("content", "你是一个为老年人服务的护工，对老年人的日常生活起居有丰富的经验。你是一个老年人健康专家，对老年人身体出现的常见问题了如指掌，并且知道如何解决。你是一个幽默风趣的心理学家，能够察觉老年人的心理问题并开导他们");
                    userMessageObject2.put("role", "user");
                    userMessageObject2.put("content","你好我的智能助手");
                    messagesArray.put(userMessageObject1);
                    messagesArray.put(userMessageObject2);
                    jsonBody.put("model", "generalv3.5");
                    jsonBody.put("messages", messagesArray);
                    jsonBody.put("temperature", 0.5);

                    Log.d("JSONBody", jsonBody.toString()); // 打印 JSON 调试信息
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
                Request request = new Request.Builder()
                        .url("https://spark-api-open.xf-yun.com/v1/chat/completions")
                        .header("Authorization", "Bearer TdmvZRBGqvaEukrXIwtR:GWkBDycxLdoBDhMcsHcZ")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> AIResponse("获取回复失败，因为 " + e.getMessage()));
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            try {
                                String responseBody = response.body().string();
                                Log.d("AIResponse", responseBody); // 调试用

                                JSONObject jsonObject = new JSONObject(responseBody);
                                int code = jsonObject.optInt("code", -1); // 获取响应码
                                if (code != 0) {
                                    runOnUiThread(() -> AIResponse("请求失败，错误信息：" + jsonObject.optString("message", "未知错误")));
                                    return;
                                }

                                JSONArray choices = jsonObject.optJSONArray("choices");
                                if (choices != null && choices.length() > 0) {
                                    JSONObject firstChoice = choices.getJSONObject(0);
                                    JSONObject messageObject = firstChoice.optJSONObject("message");
                                    if (messageObject != null) {
                                        String content = messageObject.optString("content", "未能解析有效的回复");
                                        runOnUiThread(() -> AIResponse(content.trim()));
                                    } else {
                                        runOnUiThread(() -> AIResponse("未能找到消息内容"));
                                    }
                                } else {
                                    runOnUiThread(() -> AIResponse("未能找到有效的回复"));
                                }
                            }  catch (JSONException e) {
                                runOnUiThread(() -> AIResponse("解析响应失败：" + e.getMessage()));
                            }
                        } else {
                            runOnUiThread(() -> AIResponse("获取回复失败，错误码：" + response.code()));
                        }
                    }
                });
            }
        }).start();
    }

    private void simulateAIResponse(String userMessage) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonBody = new JSONObject();
                try {
                    JSONArray messagesArray = new JSONArray();
                    JSONObject userMessageObject = new JSONObject(); // 修改后的名称
                    userMessageObject.put("role", "user");
                    userMessageObject.put("content", userMessage);
                    messagesArray.put(userMessageObject);

                    jsonBody.put("model", "generalv3.5");
                    jsonBody.put("messages", messagesArray);
                    jsonBody.put("temperature", 0.5);

                    Log.d("JSONBody", jsonBody.toString()); // 打印 JSON 调试信息
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
                Request request = new Request.Builder()
                        .url("https://spark-api-open.xf-yun.com/v1/chat/completions")
                        .header("Authorization", "Bearer TdmvZRBGqvaEukrXIwtR:GWkBDycxLdoBDhMcsHcZ")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> AIResponse("获取回复失败，因为 " + e.getMessage()));
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            try {
                                String responseBody = response.body().string();
                                Log.d("AIResponse", responseBody); // 调试用

                                JSONObject jsonObject = new JSONObject(responseBody);
                                int code = jsonObject.optInt("code", -1); // 获取响应码
                                if (code != 0) {
                                    runOnUiThread(() -> AIResponse("请求失败，错误信息：" + jsonObject.optString("message", "未知错误")));
                                    return;
                                }

                                JSONArray choices = jsonObject.optJSONArray("choices");
                                if (choices != null && choices.length() > 0) {
                                    JSONObject firstChoice = choices.getJSONObject(0);
                                    JSONObject messageObject = firstChoice.optJSONObject("message");
                                    if (messageObject != null) {
                                        String content = messageObject.optString("content", "未能解析有效的回复");
                                        runOnUiThread(() -> AIResponse(content.trim()));
                                    } else {
                                        runOnUiThread(() -> AIResponse("未能找到消息内容"));
                                    }
                                } else {
                                    runOnUiThread(() -> AIResponse("未能找到有效的回复"));
                                }
                            }  catch (JSONException e) {
                                runOnUiThread(() -> AIResponse("解析响应失败：" + e.getMessage()));
                            }
                        } else {
                            runOnUiThread(() -> AIResponse("获取回复失败，错误码：" + response.code()));
                        }
                    }
                });
            }
        }).start();
    }

}
