package com.example.mybaghackathon.ui.createroom;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityCreateRoomBinding;
import com.example.mybaghackathon.ui.roomdetail.RoomDetailActivity;
import com.google.android.material.button.MaterialButton;

/**
 * S04 · 방 만들기 — 여행방 이름을 짓고, 호스트(나)가 목록에 표시되는 것을
 * 확인한 뒤 방을 생성함.
 *
 * 기능: 멤버 목록에 "나"를 호스트로 추가해 보여주고, 생성 버튼을 누르면
 * RoomDetailActivity로 이동하는 화면.
 */
public class CreateRoomActivity extends AppCompatActivity {

    private ActivityCreateRoomBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView title = binding.createRoomTopBar.topAppBarCompactTitle;
        title.setText(R.string.create_room_title);
        binding.createRoomTopBar.topAppBarBack.setOnClickListener(v -> finish());

        MaterialButton submit = binding.createRoomBottomCta.bottomCtaPrimary;
        submit.setText(R.string.create_room_submit);
        submit.setOnClickListener(v -> {
            startActivity(new Intent(this, RoomDetailActivity.class));
            finish();
        });
    }
}
