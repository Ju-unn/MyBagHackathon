package com.example.mybaghackathon.ui.overlay;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.SheetInviteBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * BS01 · 초대 공유 — 여행방 링크 복사 + 카카오 공유.
 *
 * 기능: 초대 링크를 보여주고, 복사 버튼을 누르면 클립보드에 복사한 뒤
 * 토스트를 띄우는 바텀시트.
 */
public class InviteShareSheet extends BottomSheetDialogFragment {

    private static final String ARG_LINK = "link";

    private SheetInviteBinding binding;

    public static InviteShareSheet newInstance(String link) {
        InviteShareSheet sheet = new InviteShareSheet();
        Bundle args = new Bundle();
        args.putString(ARG_LINK, link);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = SheetInviteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String link = getArguments() != null && getArguments().getString(ARG_LINK) != null
                ? getArguments().getString(ARG_LINK) : "https://mybag.app/invite/8f2c91";

        TextView linkText = binding.inviteLinkText;
        linkText.setText(link);

        binding.inviteCopyLinkButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("invite_link", link));
            Toast.makeText(getContext(), R.string.action_copy_link, Toast.LENGTH_SHORT).show();
        });

        binding.inviteKakaoButton.setOnClickListener(v -> dismiss());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
