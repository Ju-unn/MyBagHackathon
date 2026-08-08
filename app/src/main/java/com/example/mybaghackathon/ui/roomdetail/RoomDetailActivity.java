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

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.AppContainer;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.mapper.WeatherMapper;
import com.example.mybaghackathon.data.repository.PackingRepository;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.data.repository.WeatherRepository;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * S09 · 방 상세 화면.
 *
 * <p>trip_id가 전달되면 여행방, 참여자, 날씨 예보, 반입 제한 정보를 서버에서
 * 조회합니다. trip_id가 없는 디자인 미리보기 진입에서는 XML 샘플 값을 유지합니다.</p>
 */
public class RoomDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "trip_id";
    public static final String EXTRA_ROOM_NAME = "room_name";
    public static final String EXTRA_IS_HOST = "is_host";
    public static final String EXTRA_INVITE_CODE = "invite_code";

    private static final String DEFAULT_ROOM_NAME = "제주 가족 여행";
    private static final String INVITE_URL_BASE = "https://mybag.app/invite/";

    private ActivityRoomDetailBinding binding;
    private TripRepository tripRepository;
    private WeatherRepository weatherRepository;
    private PackingRepository packingRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private long tripId = -1L;
    private boolean isHost;
    private int memberCount = 3;
    private String inviteCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());
        AppContainer container = ((MyBagApplication) getApplication()).getAppContainer();
        tripRepository = container.tripRepository;
        weatherRepository = container.weatherRepository;
        packingRepository = container.packingRepository;

        readArguments();
        bindActions();
        showPreviewMembers();

        if (tripId > 0L) {
            loadRoomData();
        }
    }

    private void readArguments() {
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, -1L);
        isHost = getIntent().getBooleanExtra(EXTRA_IS_HOST, true);
        inviteCode = getIntent().getStringExtra(EXTRA_INVITE_CODE);

        String roomName = getIntent().getStringExtra(EXTRA_ROOM_NAME);
        if (!hasText(roomName)) {
            roomName = DEFAULT_ROOM_NAME;
        }
        binding.roomDetailTopBar.topAppBarCompactTitle.setText(roomName);
        updateHostUi();
    }

    private void bindActions() {
        binding.roomDetailTopBar.topAppBarBack.setOnClickListener(v -> finish());
        binding.roomDetailInviteButton.setOnClickListener(v -> showInviteSheet());

        MaterialButton viewTips = binding.roomDetailBottomCta.bottomCtaSecondary;
        viewTips.setText(R.string.room_detail_view_tips);
        viewTips.setOnClickListener(v -> {
            Intent intent = new Intent(this, WeatherFeedbackActivity.class);
            intent.putExtra(WeatherFeedbackActivity.EXTRA_TRIP_ID, tripId);
            startActivity(intent);
        });

        MaterialButton viewChecklist = binding.roomDetailBottomCta.bottomCtaPrimary;
        viewChecklist.setText(R.string.room_detail_view_checklist);
        viewChecklist.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChecklistActivity.class);
            intent.putExtra(ChecklistActivity.EXTRA_TRIP_ID, tripId);
            intent.putExtra(ChecklistActivity.EXTRA_MEMBER_COUNT, memberCount);
            intent.putExtra(ChecklistActivity.EXTRA_IS_HOST, isHost);
            startActivity(intent);
        });
    }

    private void showInviteSheet() {
        if (!hasText(inviteCode)) {
            Toast.makeText(this, "초대 코드를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        String inviteUrl = INVITE_URL_BASE + Uri.encode(inviteCode.trim());
        InviteShareSheet.newInstance(inviteUrl)
                .show(getSupportFragmentManager(), "invite_share");
    }

    private void showPreviewMembers() {
        LinearLayout memberList = binding.roomDetailMemberList;
        addMember(memberList, "나", true, R.color.bag_avatar_2);
        addMember(memberList, "민지", false, R.color.bag_avatar_1);
        addMember(memberList, "유진", false, R.color.bag_avatar_4);
    }

    private void loadRoomData() {
        executor.execute(() -> {
            AppResult<Trip> tripResult = tripRepository.getTripDetail(tripId);
            if (!tripResult.isSuccess() || tripResult.getData() == null) {
                runOnUiThread(() -> handleTripFailure(tripResult));
                return;
            }

            Trip trip = tripResult.getData();
            AppResult<List<Weather>> weatherResult = null;
            if (hasText(trip.getDestinationCity())
                    && hasText(trip.getStartDate())
                    && hasText(trip.getEndDate())) {
                weatherResult = weatherRepository.getForecast(
                        trip.getDestinationCity(), trip.getStartDate(), trip.getEndDate());
            }
            AppResult<List<PackingItem>> packingResult = packingRepository.listItems(tripId, null);

            AppResult<List<Weather>> finalWeatherResult = weatherResult;
            runOnUiThread(() -> bindRoomData(trip, finalWeatherResult, packingResult));
        });
    }

    private void handleTripFailure(AppResult<Trip> result) {
        if (!canUpdateUi()) {
            return;
        }
        String message = result.getError() == null
                ? "여행방 정보를 불러오지 못했습니다."
                : result.getError().getMessage();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void bindRoomData(
            Trip trip,
            AppResult<List<Weather>> weatherResult,
            AppResult<List<PackingItem>> packingResult
    ) {
        if (!canUpdateUi()) {
            return;
        }

        bindTrip(trip);
        if (weatherResult != null && weatherResult.isSuccess()) {
            bindWeather(weatherResult.getData());
        }
        if (packingResult.isSuccess()) {
            bindRestriction(packingResult.getData());
        }
    }

    private void bindTrip(Trip trip) {
        if (hasText(trip.getTripName())) {
            binding.roomDetailTopBar.topAppBarCompactTitle.setText(trip.getTripName());
        }
        binding.roomDetailDestinationValue.setText(
                joinNonEmpty(" ", trip.getDestinationCountry(), trip.getDestinationCity()));
        binding.roomDetailDatesValue.setText(
                joinNonEmpty(" ~ ", trip.getStartDate(), trip.getEndDate()));

        int currentUserId = ((MyBagApplication) getApplication())
                .getAppContainer()
                .tokenStorage
                .getUserId();
        if (currentUserId > 0) {
            isHost = trip.getOwnerUserId() == currentUserId;
            updateHostUi();
        }

        List<TripMember> members = trip.getMembers();
        memberCount = members.size();
        binding.roomDetailMemberList.removeAllViews();
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

    private void bindWeather(List<Weather> weatherList) {
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
        for (int index = 0; index < rows.length; index++) {
            boolean visible = index < count;
            rows[index].setVisibility(visible ? View.VISIBLE : View.GONE);
            if (!visible) {
                continue;
            }
            Weather weather = weatherList.get(index);
            dates[index].setText(formatWeatherDate(weather.getDate()));
            icons[index].setType(WeatherMapper.toIconType(weather.getCondition()));
            statuses[index].setText(
                    conditionLabel(weather.getCondition())
                            + " " + Math.round(weather.getTempMax()) + "°");
        }
    }

    private void bindRestriction(List<PackingItem> items) {
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

        String warning = joinNonEmpty(
                ": ", restrictedItem.getItemName(), restrictedItem.getRestrictionReason());
        binding.roomDetailRestrictionText.setText(warning);
        binding.roomDetailRestrictionWarning.setVisibility(View.VISIBLE);
    }

    private void updateHostUi() {
        binding.roomDetailHostInviteArea.setVisibility(isHost ? View.VISIBLE : View.GONE);
    }

    private void addMember(LinearLayout list, String name, boolean host, int avatarColorRes) {
        View row = LayoutInflater.from(this)
                .inflate(R.layout.molecule_member_list_item, list, false);
        ((TextView) row.findViewById(R.id.memberName)).setText(name);

        com.example.mybaghackathon.ui.atoms.AvatarView avatar =
                row.findViewById(R.id.memberAvatar);
        avatar.setInitial(name.substring(0, 1));
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

    private boolean canUpdateUi() {
        return binding != null && !isFinishing() && !isDestroyed();
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        binding = null;
        super.onDestroy();
    }
}
