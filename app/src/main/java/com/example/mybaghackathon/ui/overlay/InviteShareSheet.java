package com.example.mybaghackathon.ui.overlay;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.kakao.sdk.share.ShareClient;
import com.kakao.sdk.share.WebSharerClient;
import com.kakao.sdk.template.model.Button;
import com.kakao.sdk.template.model.Link;
import com.kakao.sdk.template.model.TextTemplate;

import java.util.Collections;
import java.util.Map;

import kotlin.Unit;

/**
 * BS01 · 초대 공유 — 여행방 링크 복사 + 카카오 공유.
 *
 * 기능: 초대 링크를 보여주고, 복사 버튼을 누르면 클립보드에 복사한 뒤
 * 토스트를 띄우는 바텀시트.
 */
public class InviteShareSheet extends BottomSheetDialogFragment {

    private static final String ARG_LINK = "link";
    private static final String ARG_INVITE_CODE = "invite_code";

    private SheetInviteBinding binding;

    public static InviteShareSheet newInstance(String link, String inviteCode) {
        InviteShareSheet sheet = new InviteShareSheet();
        Bundle args = new Bundle();
        args.putString(ARG_LINK, link);
        args.putString(ARG_INVITE_CODE, inviteCode);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = SheetInviteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String link = getArguments() == null ? null : getArguments().getString(ARG_LINK);
        String inviteCode = getArguments() == null
                ? null : getArguments().getString(ARG_INVITE_CODE);

        if (link == null || link.trim().isEmpty()
                || inviteCode == null || inviteCode.trim().isEmpty()) {
            dismissAllowingStateLoss();
            return root;
        }

        TextView linkText = binding.inviteLinkText;
        linkText.setText(link);

        binding.inviteCopyLinkButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("invite_link", link));
            Toast.makeText(getContext(), R.string.action_copy_link, Toast.LENGTH_SHORT).show();
        });

        binding.inviteKakaoButton.setOnClickListener(v -> shareToKakao(link, inviteCode));

        return root;
    }

    private void shareToKakao(String inviteUrl, String inviteCode) {
        Map<String, String> executionParams =
                Collections.singletonMap(ARG_INVITE_CODE, inviteCode);
        Link templateLink = new Link(
                inviteUrl,
                inviteUrl,
                executionParams,
                Collections.emptyMap()
        );
        TextTemplate template = new TextTemplate(
                getString(R.string.invite_share_message),
                templateLink,
                Collections.singletonList(
                        new Button(getString(R.string.invite_share_button), templateLink)
                )
        );

        if (!ShareClient.getInstance().isKakaoTalkSharingAvailable(requireContext())) {
            openWebSharer(template);
            return;
        }

        setShareEnabled(false);
        ShareClient.getInstance().shareDefault(requireContext(), template, (result, error) -> {
            if (!isAdded() || binding == null) {
                return Unit.INSTANCE;
            }
            setShareEnabled(true);
            if (error != null || result == null) {
                showShareError();
                return Unit.INSTANCE;
            }
            startActivity(result.getIntent());
            dismissAllowingStateLoss();
            return Unit.INSTANCE;
        });
    }

    private void openWebSharer(TextTemplate template) {
        try {
            Uri sharerUrl = WebSharerClient.getInstance().makeDefaultUrl(template);
            startActivity(new Intent(Intent.ACTION_VIEW, sharerUrl));
            dismissAllowingStateLoss();
        } catch (RuntimeException error) {
            showShareError();
        }
    }

    private void setShareEnabled(boolean enabled) {
        if (binding != null) {
            binding.inviteKakaoButton.setEnabled(enabled);
        }
    }

    private void showShareError() {
        Toast.makeText(requireContext(), R.string.invite_share_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
