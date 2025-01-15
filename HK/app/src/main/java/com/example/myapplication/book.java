package com.example.myapplication;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.myapplication.databinding.BookBinding;
import android.content.SharedPreferences;

public class book extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private BookBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = BookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences("BookPreferences", MODE_PRIVATE);

        // 设置工具栏
        setSupportActionBar(binding.toolbar);

        // 设置导航
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // 假设你从 Intent 或者 Bundle 中获取当前页面的 contentId
        int currentContentId = getIntent().getIntExtra("contentId", 1);  // 默认值为 1

        // 获取 FAB 按钮并更新图标
        FloatingActionButton fab = findViewById(R.id.fab);
        updateBookmarkIcon(fab, currentContentId);  // 传入当前内容的 contentId

        // 设置 FAB 点击事件，切换书签
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleBookmark(fab, currentContentId);  // 传入当前内容的 contentId
                Snackbar.make(view, isBookmarked(currentContentId) ? "Bookmarked" : "Unbookmarked", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });
    }

    // 更新书签图标
    private void updateBookmarkIcon(FloatingActionButton fab, int contentId) {
        if (isBookmarked(contentId)) {
            fab.setImageResource(R.drawable.ic_bookmark_filled);  // 显示已书签图标
        } else {
            fab.setImageResource(R.drawable.ic_bookmark_outline);  // 显示未书签图标
        }
    }

    // 切换书签状态
    private void toggleBookmark(FloatingActionButton fab, int contentId) {
        boolean currentState = isBookmarked(contentId);
        saveBookmarkStatus(contentId, !currentState);  // 切换书签状态
        updateBookmarkIcon(fab, contentId);  // 更新图标
    }

    // 检查指定内容的书签状态
    private boolean isBookmarked(int contentId) {
        return sharedPreferences.getBoolean("isBookmarked_" + contentId, false);
    }

    // 保存指定内容的书签状态
    private void saveBookmarkStatus(int contentId, boolean isBookmarked) {
        sharedPreferences.edit().putBoolean("isBookmarked_" + contentId, isBookmarked).apply();
    }
}
