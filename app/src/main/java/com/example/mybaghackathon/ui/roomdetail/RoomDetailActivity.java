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
import com.example.mybaghackathon.ui.atoms.IconButtonView;
import com.example.mybaghackathon.ui.checklist.ChecklistActivity;
import com.example.mybaghackathon.ui.overlay.InviteShareSheet;
import com.example.mybaghackathon.ui.upload.ScheduleUploadActivity;
import com.google.android.material.button.MaterialButton;

/** S05 · Room Detail — members + "upload schedule to generate checklist" entry point. */
public class RoomDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        TextView title = findViewById(R.id.topAppBarCompactTitle);
        title.setText("제주 가족 여행");
        findViewById(R.id.topAppBarBack).setOnClickListener(v -> finish());

        IconButtonView inviteButton = findViewById(R.id.roomDetailInviteButton);
        inviteButton.setIcon(R.drawable.ic_add);
        inviteButton.setOnClickListener(v ->
                InviteShareSheet.newInstance("https://mybag.app/invite/8f2c91")
                        .show(getSupportFragmentManager(), "invite_share"));

        LinearLayout memberList = findViewById(R.id.roomDetailMemberList);
        addMember(memberList, "나", true, R.color.bag_avatar_2);
        addMember(memberList, "민지", false, R.color.bag_avatar_1);
        addMember(memberList, "유진", false, R.color.bag_avatar_4);

        findViewById(R.id.roomDetailReanalyzeButton).setOnClickListener(v ->
                startActivity(new Intent(this, ScheduleUploadActivity.class)));

        MaterialButton viewChecklist = findViewById(R.id.bottomCtaPrimary);
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
