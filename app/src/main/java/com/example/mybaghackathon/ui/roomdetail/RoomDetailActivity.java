package com.example.mybaghackathon.ui.roomdetail;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityRoomDetailBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.checklist.ChecklistActivity;
import com.example.mybaghackathon.ui.feedback.WeatherFeedbackActivity;
import com.example.mybaghackathon.ui.overlay.InviteShareSheet;
import com.google.android.material.button.MaterialButton;

/**
 * S05 · 방 상세 — 멤버 목록 + "일정 업로드해서 체크리스트 생성하기" 진입점.
 *
 * 기능: 멤버 목록에 아바타/호스트 뱃지를 채워 넣고, 초대 버튼으로
 * InviteShareSheet를, 재분석 버튼으로 ScheduleUploadActivity를 열며,
 * "체크리스트 보기" 버튼으로 ChecklistActivity로 이동하는 화면.
 */
public class RoomDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_NAME = "room_name";
    public static final String EXTRA_IS_HOST = "is_host";

    private static final String DEFAULT_ROOM_NAME = "제주 가족 여행";
    private static final String INVITE_URL = "https://mybag.app/invite/8f2c91";

    private ActivityRoomDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        String roomName = getIntent().getStringExtra(EXTRA_ROOM_NAME);
        if (roomName == null || roomName.trim().isEmpty()) {
            roomName = DEFAULT_ROOM_NAME;
        }

        TextView title = binding.roomDetailTopBar.topAppBarCompactTitle;
        title.setText(roomName);
        binding.roomDetailTopBar.topAppBarBack.setOnClickListener(v -> finish());

        boolean isHost = getIntent().getBooleanExtra(EXTRA_IS_HOST, true);
        binding.roomDetailHostInviteArea.setVisibility(isHost ? View.VISIBLE : View.GONE);

        MaterialButton inviteButton = binding.roomDetailInviteButton;
        inviteButton.setOnClickListener(v ->
                InviteShareSheet.newInstance(INVITE_URL)
                        .show(getSupportFragmentManager(), "invite_share"));

        LinearLayout memberList = binding.roomDetailMemberList;
        addMember(memberList, "나", true, R.color.bag_avatar_2);
        addMember(memberList, "민지", false, R.color.bag_avatar_1);
        addMember(memberList, "유진", false, R.color.bag_avatar_4);

        MaterialButton viewTips = binding.roomDetailBottomCta.bottomCtaSecondary;
        viewTips.setText(R.string.room_detail_view_tips);
        viewTips.setOnClickListener(v ->
                startActivity(new Intent(this, WeatherFeedbackActivity.class)));

        MaterialButton viewChecklist = binding.roomDetailBottomCta.bottomCtaPrimary;
        viewChecklist.setText(R.string.room_detail_view_checklist);
        viewChecklist.setOnClickListener(v -> startActivity(new Intent(this, ChecklistActivity.class)));
    }

    private void addMember(LinearLayout list, String name, boolean host, int avatarColorRes) {
        View row = LayoutInflater.from(this).inflate(R.layout.molecule_member_list_item, list, false);
        ((TextView) row.findViewById(R.id.memberName)).setText(name);
        com.example.mybaghackathon.ui.atoms.AvatarView avatar = row.findViewById(R.id.memberAvatar);
        avatar.setInitial(name.substring(0, 1));
        avatar.setAvatarColor(ContextCompat.getColor(this, avatarColorRes));
        row.findViewById(R.id.memberHostBadge).setVisibility(host ? View.VISIBLE : View.GONE);
        list.addView(row);
    }
}
