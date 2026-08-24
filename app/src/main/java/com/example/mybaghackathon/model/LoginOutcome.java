package com.example.mybaghackathon.model;

// 로그인 시도 결과. 정상 로그인 성공이거나, 탈퇴 후 유예기간 내 계정이라 복구 확인이 필요한 상태 둘 중 하나
public class LoginOutcome {

    private final User user;
    private final boolean requiresRestoreConfirmation;
    private final String withdrawnAt;

    private LoginOutcome(User user, boolean requiresRestoreConfirmation, String withdrawnAt) {
        this.user = user;
        this.requiresRestoreConfirmation = requiresRestoreConfirmation;
        this.withdrawnAt = withdrawnAt;
    }

    public static LoginOutcome loggedIn(User user) {
        return new LoginOutcome(user, false, null);
    }

    public static LoginOutcome needsRestoreConfirmation(String withdrawnAt) {
        return new LoginOutcome(null, true, withdrawnAt);
    }

    public User getUser() {
        return user;
    }

    public boolean requiresRestoreConfirmation() {
        return requiresRestoreConfirmation;
    }

    public String getWithdrawnAt() {
        return withdrawnAt;
    }
}
