package com.example.mybaghackathon.ui.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.MainActivity;
import com.example.mybaghackathon.R;

/**
 * S02 · 로그인 — 카카오 로그인 버튼 하나만 있고, 누르면 MainActivity
 * (홈 탭)로 이동함.
 *
 * 기능: 카카오 로그인 버튼 클릭 시 바로 MainActivity로 넘어가는(실제 인증
 * 로직은 아직 없는) 로그인 화면.
 */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.loginKakaoButton).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
