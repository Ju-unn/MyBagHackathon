package com.example.mybaghackathon.ui.roomdetail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.AppContainer;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.data.mapper.WeatherMapper;
import com.example.mybaghackathon.databinding.ActivityRoomDetailBinding;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.TripMember;
import com.example.mybaghackathon.model.Weather;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.atoms.WeatherIconView;
import com.example.mybaghackathon.ui.checklist.ChecklistActivity;
import com.example.mybaghackathon.ui.feedback.WeatherFeedbackActivity;
import com.example.mybaghackathon.ui.overlay.InviteShareSheet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** S09 방 상세 화면. 화면 표시와 사용자 입력 전달만 담당한다. */
public class RoomDetailActivity extends AppCompatActivity implements RoomDetailContract.View {

    public static final String EXTRA_TRIP_ID = "trip_id";
    public static final String EXTRA_ROOM_NAME = "room_name";
    public static final String EXTRA_IS_HOST = "is_host";
    public static final String EXTRA_INVITE_CODE = "invite_code";

    private static final String DEFAULT_ROOM_NAME = "여행방";
    private static final String INVITE_URL_BASE = "https://mybag.duckdns.org/invite/";
    private static final String INVITE_PREFS = "room_invite_codes";

    private ActivityRoomDetailBinding binding;
    private RoomDetailContract.Presenter presenter;
    private Trip lastTrip;
    private List<Weather> lastWeather = Collections.emptyList();
    private List<PackingItem> lastPackingItems = Collections.emptyList();
    private String lastWeatherState;
    private boolean lastIsHost;
    private boolean screenReady;
    private long currentUserId = -1L;
    private boolean resumedOnce;

    @Override
    public void showLoading(boolean loading) {
        if (!canUpdateUi()) {
            return;
        }
        setActionsEnabled(!loading);
        if (!loading) {
            screenReady = lastTrip != null;
            binding.roomDetailState.getRoot().setVisibility(View.GONE);
            return;
        }
        screenReady = false;
        binding.roomDetailState.getRoot().setVisibility(View.VISIBLE);
        binding.roomDetailState.screenStateProgress.setVisibility(View.VISIBLE);
        binding.roomDetailState.screenStateTitle.setText(R.string.room_detail_loading_title);
        binding.roomDetailState.screenStateMessage.setText(R.string.room_detail_loading_message);
        binding.roomDetailState.screenStateRetry.setVisibility(View.GONE);
    }

    @Override
    public void showLoadError(String message) {
        if (!canUpdateUi()) {
            return;
        }
        setActionsEnabled(false);
        screenReady = false;
        binding.roomDetailState.getRoot().setVisibility(View.VISIBLE);
        binding.roomDetailState.screenStateProgress.setVisibility(View.GONE);
        binding.roomDetailState.screenStateTitle.setText(R.string.room_detail_error_title);
        binding.roomDetailState.screenStateMessage.setText(message);
        binding.roomDetailState.screenStateRetry.setVisibility(View.VISIBLE);
        binding.roomDetailState.screenStateRetry.setOnClickListener(v -> presenter.retry());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        AppContainer container = ((MyBagApplication) getApplication()).getAppContainer();
        currentUserId = container.tokenStorage.getUserId();
        presenter = new RoomDetailPresenter(
                this,
                container.tripRepository,
                container.weatherRepository,
                container.packingRepository,
                currentUserId
        );

        bindActions();
        Object retained = getLastCustomNonConfigurationInstance();
        if (retained instanceof RoomScreenSnapshot) {
            restoreSnapshot((RoomScreenSnapshot) retained);
        } else {
            readArguments();
        }
    }

    private void readArguments() {
        long tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, -1L);
        boolean initialHost = getIntent().getBooleanExtra(EXTRA_IS_HOST, false);
        String inviteCode = getIntent().getStringExtra(EXTRA_INVITE_CODE);
        String roomName = getIntent().getStringExtra(EXTRA_ROOM_NAME);

        inviteCode = resolveInviteCode(tripId, inviteCode);

        binding.roomDetailTopBar.topAppBarCompactTitle.setText(
                hasText(roomName) ? roomName.trim() : DEFAULT_ROOM_NAME);
        updateHostUi(initialHost);
        presenter.loadRoom(tripId, initialHost, inviteCode);
    }

    private void bindActions() {
        binding.roomDetailTopBar.topAppBarBack.setOnClickListener(v -> finish());
        binding.roomDetailInviteButton.setOnClickListener(v -> presenter.onInviteClicked());

        MaterialButton viewTips = binding.roomDetailBottomCta.bottomCtaSecondary;
        viewTips.setText(R.string.room_detail_view_tips);
        viewTips.setOnClickListener(v -> presenter.onTipsClicked());

        MaterialButton viewChecklist = binding.roomDetailBottomCta.bottomCtaPrimary;
        viewChecklist.setText(R.string.room_detail_view_checklist);
        viewChecklist.setOnClickListener(v -> presenter.onChecklistClicked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resumedOnce && presenter != null) {
            // 다른 화면에서 돌아오면 참여자와 방 상태를 최신 정보로 다시 그린다.
            presenter.retry();
        }
        resumedOnce = true;
    }

    @Override
    public void showTrip(Trip trip, boolean isHost) {
        if (!canUpdateUi()) {
            return;
        }
        lastTrip = trip;
        lastIsHost = isHost;
        rememberInviteCode(trip.getTripId(), trip.getInviteCode());
        if (hasText(trip.getTripName())) {
            binding.roomDetailTopBar.topAppBarCompactTitle.setText(trip.getTripName());
        }
        binding.roomDetailDestinationValue.setText(
                joinNonEmpty(" ", trip.getDestinationCountry(), trip.getDestinationCity()));
        binding.roomDetailDatesValue.setText(
                joinNonEmpty(" ~ ", trip.getStartDate(), trip.getEndDate()));
        updateHostUi(isHost);
        showMembers(trip.getMembers());
    }

    @Override
    public void showWeather(List<Weather> weatherList) {
        if (!canUpdateUi()) {
            return;
        }
        lastWeather = weatherList == null
                ? Collections.emptyList() : new ArrayList<>(weatherList);
        lastWeatherState = null;
        View[] rows = {
                binding.roomDetailWeatherRow1,
                binding.roomDetailWeatherRow2,
                binding.roomDetailWeatherRow3
        };
        TextView[] dates = {
                binding.roomDetailWeatherDate1,
                binding.roomDetailWeatherDate2,
                binding.roomDetailWeatherDate3
        };
        WeatherIconView[] icons = {
                binding.roomDetailWeatherIcon1,
                binding.roomDetailWeatherIcon2,
                binding.roomDetailWeatherIcon3
        };
        TextView[] statuses = {
                binding.roomDetailWeatherStatus1,
                binding.roomDetailWeatherStatus2,
                binding.roomDetailWeatherStatus3
        };

        int count = Math.min(weatherList == null ? 0 : weatherList.size(), rows.length);
        binding.roomDetailWeatherState.setVisibility(View.GONE);
        for (int index = 0; index < rows.length; index++) {
            boolean visible = index < count;
            rows[index].setVisibility(visible ? View.VISIBLE : View.GONE);
            if (!visible) {
                continue;
            }
            Weather weather = weatherList.get(index);
            dates[index].setText(formatWeatherDate(weather.getDate()));
            icons[index].setType(WeatherMapper.toIconType(weather.getCondition()));
            statuses[index].setText(getString(
                    R.string.room_detail_weather_status_format,
                    conditionLabel(weather.getCondition()),
                    Math.round(weather.getTempMax())));
        }
    }

    @Override
    public void showWeatherPending(String message) {
        showWeatherState(message);
    }

    @Override
    public void showWeatherEmpty() {
        showWeatherState(getString(R.string.room_detail_weather_empty));
    }

    @Override
    public void showPackingRestrictions(List<PackingItem> items) {
        if (!canUpdateUi()) {
            return;
        }
        lastPackingItems = items == null
                ? Collections.emptyList() : new ArrayList<>(items);
        PackingItem restrictedItem = null;
        if (items != null) {
            for (PackingItem item : items) {
                if (hasText(item.getRestrictionType()) || hasText(item.getRestrictionReason())) {
                    restrictedItem = item;
                    break;
                }
            }
        }

        if (restrictedItem == null) {
            binding.roomDetailRestrictionWarning.setVisibility(View.GONE);
            return;
        }

        binding.roomDetailRestrictionText.setText(joinNonEmpty(
                ": ", restrictedItem.getItemName(), restrictedItem.getRestrictionReason()));
        binding.roomDetailRestrictionWarning.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(String message) {
        if (canUpdateUi()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showRetryableError(String message) {
        if (canUpdateUi()) {
            Snackbar.make(binding.roomDetailContent, message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_retry, v -> presenter.retry())
                    .show();
        }
    }

    @Override
    public void openWeatherFeedback(long tripId) {
        Intent intent = new Intent(this, WeatherFeedbackActivity.class);
        intent.putExtra(WeatherFeedbackActivity.EXTRA_TRIP_ID, tripId);
        startActivity(intent);
    }

    @Override
    public void openChecklist(long tripId, int memberCount, boolean isHost) {
        Intent intent = new Intent(this, ChecklistActivity.class);
        intent.putExtra(ChecklistActivity.EXTRA_TRIP_ID, tripId);
        intent.putExtra(ChecklistActivity.EXTRA_MEMBER_COUNT, memberCount);
        intent.putExtra(ChecklistActivity.EXTRA_IS_HOST, isHost);
        startActivity(intent);
    }

    @Override
    public void showInviteShare(String inviteCode) {
        Fragment existing = getSupportFragmentManager().findFragmentByTag("invite_share");
        if (existing != null || getSupportFragmentManager().isStateSaved()) {
            return;
        }
        if (lastTrip != null) {
            rememberInviteCode(lastTrip.getTripId(), inviteCode);
        }
        String inviteUrl = INVITE_URL_BASE + Uri.encode(inviteCode);
        InviteShareSheet.newInstance(inviteUrl, inviteCode)
                .show(getSupportFragmentManager(), "invite_share");
    }

    private void showMembers(List<TripMember> members) {
        binding.roomDetailMemberList.removeAllViews();
        if (members == null) {
            return;
        }
        List<Integer> avatarColors = Arrays.asList(
                R.color.bag_avatar_2,
                R.color.bag_avatar_1,
                R.color.bag_avatar_4
        );
        for (int index = 0; index < members.size(); index++) {
            TripMember member = members.get(index);
            addMember(
                    binding.roomDetailMemberList,
                    displayName(member),
                    "OWNER".equalsIgnoreCase(member.getRole()),
                    avatarColors.get(index % avatarColors.size())
            );
        }
    }

    private void updateHostUi(boolean isHost) {
        binding.roomDetailHostInviteArea.setVisibility(isHost ? View.VISIBLE : View.GONE);
    }

    private void showWeatherState(String message) {
        if (!canUpdateUi()) {
            return;
        }
        lastWeather = Collections.emptyList();
        lastWeatherState = message;
        binding.roomDetailWeatherState.setText(message);
        binding.roomDetailWeatherState.setVisibility(View.VISIBLE);
        binding.roomDetailWeatherRow1.setVisibility(View.GONE);
        binding.roomDetailWeatherRow2.setVisibility(View.GONE);
        binding.roomDetailWeatherRow3.setVisibility(View.GONE);
    }

    private void setActionsEnabled(boolean enabled) {
        binding.roomDetailInviteButton.setEnabled(enabled);
        binding.roomDetailBottomCta.bottomCtaSecondary.setEnabled(enabled);
        binding.roomDetailBottomCta.bottomCtaPrimary.setEnabled(enabled);
    }

    private void restoreSnapshot(RoomScreenSnapshot snapshot) {
        String inviteCode = snapshot.trip.getInviteCode();
        if (!hasText(inviteCode)) {
            inviteCode = getIntent().getStringExtra(EXTRA_INVITE_CODE);
        }
        inviteCode = resolveInviteCode(snapshot.trip.getTripId(), inviteCode);
        int memberCount = snapshot.trip.getMembers() == null
                ? 1 : Math.max(1, snapshot.trip.getMembers().size());
        presenter.restoreRoomContext(
                snapshot.trip.getTripId(), snapshot.isHost, memberCount, inviteCode);
        showTrip(snapshot.trip, snapshot.isHost);
        if (hasText(snapshot.weatherState)) {
            showWeatherState(snapshot.weatherState);
        } else if (snapshot.weather.isEmpty()) {
            showWeatherEmpty();
        } else {
            showWeather(snapshot.weather);
        }
        showPackingRestrictions(snapshot.packingItems);
        binding.roomDetailState.getRoot().setVisibility(View.GONE);
        setActionsEnabled(true);
        screenReady = true;
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (!screenReady || lastTrip == null) {
            return null;
        }
        return new RoomScreenSnapshot(
                lastTrip,
                new ArrayList<>(lastWeather),
                new ArrayList<>(lastPackingItems),
                lastWeatherState,
                lastIsHost);
    }

    private static final class RoomScreenSnapshot {
        private final Trip trip;
        private final List<Weather> weather;
        private final List<PackingItem> packingItems;
        private final String weatherState;
        private final boolean isHost;

        private RoomScreenSnapshot(
                Trip trip,
                List<Weather> weather,
                List<PackingItem> packingItems,
                String weatherState,
                boolean isHost
        ) {
            this.trip = trip;
            this.weather = weather;
            this.packingItems = packingItems;
            this.weatherState = weatherState;
            this.isHost = isHost;
        }
    }

    private void addMember(LinearLayout list, String name, boolean host, int avatarColorRes) {
        String safeName = hasText(name) ? name.trim() : "여행자";
        View row = LayoutInflater.from(this)
                .inflate(R.layout.molecule_member_list_item, list, false);
        ((TextView) row.findViewById(R.id.memberName)).setText(safeName);

        com.example.mybaghackathon.ui.atoms.AvatarView avatar =
                row.findViewById(R.id.memberAvatar);
        avatar.setInitial(safeName.substring(0, 1));
        avatar.setAvatarColor(ContextCompat.getColor(this, avatarColorRes));
        row.findViewById(R.id.memberHostBadge)
                .setVisibility(host ? View.VISIBLE : View.GONE);
        list.addView(row);
    }

    private String displayName(TripMember member) {
        return hasText(member.getNickname()) ? member.getNickname().trim() : "여행자";
    }

    private String formatWeatherDate(String value) {
        if (!hasText(value)) {
            return "-";
        }
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            input.setLenient(false);
            Date date = input.parse(value);
            return date == null
                    ? value
                    : new SimpleDateFormat("M.d(E)", Locale.KOREA).format(date);
        } catch (ParseException ignored) {
            return value;
        }
    }

    private String conditionLabel(String condition) {
        if (condition == null) {
            return "맑음";
        }
        switch (condition) {
            case "rain":
                return "비";
            case "cloud":
                return "흐림";
            case "snow":
                return "눈";
            default:
                return "맑음";
        }
    }

    private String joinNonEmpty(String separator, String first, String second) {
        boolean hasFirst = hasText(first);
        boolean hasSecond = hasText(second);
        if (hasFirst && hasSecond) {
            return first.trim() + separator + second.trim();
        }
        if (hasFirst) {
            return first.trim();
        }
        if (hasSecond) {
            return second.trim();
        }
        return "-";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String resolveInviteCode(long tripId, String candidate) {
        if (hasText(candidate)) {
            String normalized = candidate.trim();
            rememberInviteCode(tripId, normalized);
            return normalized;
        }
        if (tripId <= 0L || currentUserId <= 0L) {
            return null;
        }
        return getSharedPreferences(INVITE_PREFS, MODE_PRIVATE)
                .getString(invitePreferenceKey(tripId), null);
    }

    private void rememberInviteCode(long tripId, String inviteCode) {
        if (tripId <= 0L || currentUserId <= 0L || !hasText(inviteCode)) {
            return;
        }
        getSharedPreferences(INVITE_PREFS, MODE_PRIVATE)
                .edit()
                .putString(invitePreferenceKey(tripId), inviteCode.trim())
                .apply();
    }

    private String invitePreferenceKey(long tripId) {
        return currentUserId + ":" + tripId;
    }

    private boolean canUpdateUi() {
        return binding != null && !isFinishing() && !isDestroyed();
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
