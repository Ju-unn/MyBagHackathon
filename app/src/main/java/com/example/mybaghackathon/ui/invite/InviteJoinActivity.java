package com.example.mybaghackathon.ui.invite;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.AppContainer;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.databinding.ActivityInviteJoinBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.login.LoginActivity;
import com.example.mybaghackathon.ui.roomdetail.RoomDetailActivity;

/** 카카오톡 또는 웹 초대 링크로 진입해 여행방 참여를 처리한다. */
public class InviteJoinActivity extends AppCompatActivity implements InviteJoinContract.View {

    private static final String PARAM_INVITE_CODE = "invite_code";

    private ActivityInviteJoinBinding binding;
    private InviteJoinContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInviteJoinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        AppContainer container = ((MyBagApplication) getApplication()).getAppContainer();
        presenter = new InviteJoinPresenter(this, container.tripRepository);

        String inviteCode = readInviteCode(getIntent().getData());
        if (inviteCode == null) {
            showJoinError(getString(R.string.invite_join_invalid));
            return;
        }
        if (!container.tokenStorage.isLoggedIn()) {
            showLoginRequired();
            return;
        }
        presenter.join(inviteCode);
    }

    private String readInviteCode(Uri uri) {
        if (uri == null) {
            return null;
        }

        String code = uri.getQueryParameter(PARAM_INVITE_CODE);
        if (code == null && "https".equalsIgnoreCase(uri.getScheme())) {
            code = uri.getLastPathSegment();
        }
        return code == null || code.trim().isEmpty() ? null : code.trim();
    }

    private void showLoginRequired() {
        binding.inviteJoinState.screenStateProgress.setVisibility(View.GONE);
        binding.inviteJoinState.screenStateTitle.setText(R.string.invite_join_login_title);
        binding.inviteJoinState.screenStateMessage.setText(R.string.invite_join_login_message);
        binding.inviteJoinState.screenStateRetry.setText(R.string.invite_join_login_action);
        binding.inviteJoinState.screenStateRetry.setVisibility(View.VISIBLE);
        binding.inviteJoinState.screenStateRetry.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    @Override
    public void showLoading() {
        binding.inviteJoinState.screenStateProgress.setVisibility(View.VISIBLE);
        binding.inviteJoinState.screenStateTitle.setText(R.string.invite_join_loading_title);
        binding.inviteJoinState.screenStateMessage.setText(R.string.invite_join_loading_message);
        binding.inviteJoinState.screenStateRetry.setVisibility(View.GONE);
    }

    @Override
    public void showJoinError(String message) {
        binding.inviteJoinState.screenStateProgress.setVisibility(View.GONE);
        binding.inviteJoinState.screenStateTitle.setText(R.string.invite_join_error_title);
        binding.inviteJoinState.screenStateMessage.setText(message);
        binding.inviteJoinState.screenStateRetry.setText(R.string.action_retry);
        binding.inviteJoinState.screenStateRetry.setVisibility(View.VISIBLE);
        binding.inviteJoinState.screenStateRetry.setOnClickListener(v -> presenter.retry());
    }

    @Override
    public void openTrip(long tripId) {
        Intent intent = new Intent(this, RoomDetailActivity.class);
        intent.putExtra(RoomDetailActivity.EXTRA_TRIP_ID, tripId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.onDestroy();
        }
        binding = null;
        super.onDestroy();
    }
}
