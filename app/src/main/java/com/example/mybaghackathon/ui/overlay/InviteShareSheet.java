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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/** BS01 · Invite Share — copy-link + Kakao share for a trip room. */
public class InviteShareSheet extends BottomSheetDialogFragment {

    private static final String ARG_LINK = "link";

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
        View root = inflater.inflate(R.layout.sheet_invite, container, false);

        String link = getArguments() != null && getArguments().getString(ARG_LINK) != null
                ? getArguments().getString(ARG_LINK) : "https://mybag.app/invite/8f2c91";

        TextView linkText = root.findViewById(R.id.inviteLinkText);
        linkText.setText(link);

        root.findViewById(R.id.inviteCopyLinkButton).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("invite_link", link));
            Toast.makeText(getContext(), R.string.action_copy_link, Toast.LENGTH_SHORT).show();
        });

        root.findViewById(R.id.inviteKakaoButton).setOnClickListener(v -> dismiss());

        return root;
    }
}
