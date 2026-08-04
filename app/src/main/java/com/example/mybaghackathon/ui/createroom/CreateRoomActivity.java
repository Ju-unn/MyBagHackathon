package com.example.mybaghackathon.ui.createroom;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.ui.roomdetail.RoomDetailActivity;
import com.google.android.material.button.MaterialButton;

/** S04 · Create Room — name the trip room, see the host (me) listed, then create it. */
public class CreateRoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        TextView title = findViewById(R.id.topAppBarCompactTitle);
        title.setText(R.string.create_room_title);
        findViewById(R.id.topAppBarBack).setOnClickListener(v -> finish());

        LinearLayout memberList = findViewById(R.id.createRoomMemberList);
        View me = LayoutInflater.from(this).inflate(R.layout.molecule_member_list_item, memberList, false);
        ((TextView) me.findViewById(R.id.memberName)).setText("나");
        me.findViewById(R.id.memberHostBadge).setVisibility(View.VISIBLE);
        memberList.addView(me);

        MaterialButton submit = findViewById(R.id.bottomCtaPrimary);
        submit.setText(R.string.create_room_submit);
        submit.setOnClickListener(v -> {
            startActivity(new Intent(this, RoomDetailActivity.class));
            finish();
        });
    }
}
