package com.example.mybaghackathon.ui.createroom;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityCreateRoomBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.upload.ScheduleUploadActivity;
import com.google.android.material.button.MaterialButton;

/**
 * S04 · 방 만들기 — 여행방 이름과 인원을 입력받는 화면.
 *
 * 이 시점엔 방이 실제로 생성되지 않음. 입력한 방 이름·인원은
 * ScheduleUploadActivity로 인텐트를 통해 넘어가 임시 상태로 유지되다가,
 * 상세 검토 화면에서 "목록 아이템 생성"을 눌러야 비로소 방이 생성된다.
 */
public class CreateRoomActivity extends AppCompatActivity {

    private static final int MEMBER_COUNT_MIN = 1;
    private static final int MEMBER_COUNT_MAX = 10;
    private static final int MEMBER_COUNT_DEFAULT = 3;

    private ActivityCreateRoomBinding binding;
    private int memberCount = MEMBER_COUNT_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        TextView title = binding.createRoomTopBar.topAppBarCompactTitle;
        title.setText(R.string.create_room_title);
        binding.createRoomTopBar.topAppBarBack.setOnClickListener(v -> finish());

        updateMemberCountLabel();
        binding.createRoomMemberMinus.setOnClickListener(v -> {
            if (memberCount <= MEMBER_COUNT_MIN) return;
            memberCount--;
            updateMemberCountLabel();
        });
        binding.createRoomMemberPlus.setOnClickListener(v -> {
            if (memberCount >= MEMBER_COUNT_MAX) return;
            memberCount++;
            updateMemberCountLabel();
        });

        MaterialButton submit = binding.createRoomBottomCta.bottomCtaPrimary;
        submit.setText(R.string.create_room_submit);
        submit.setOnClickListener(v -> {
            String roomName = binding.createRoomNameField.textFieldInput.getText() == null
                    ? ""
                    : binding.createRoomNameField.textFieldInput.getText().toString().trim();

            Intent intent = new Intent(this, ScheduleUploadActivity.class);
            intent.putExtra("room_name", roomName);
            intent.putExtra("member_count", memberCount);
            startActivity(intent);
            finish();
        });
    }

    private void updateMemberCountLabel() {
        binding.createRoomMemberCount.setText(getString(R.string.create_room_member_count_format, memberCount));
    }
}
