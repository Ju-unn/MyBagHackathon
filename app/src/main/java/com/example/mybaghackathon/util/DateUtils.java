package com.example.mybaghackathon.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

// 날짜 포맷/변환을 담당하는 유틸
public final class DateUtils {

    private static final String ISO_DATE_PATTERN = "yyyy-MM-dd";

    private DateUtils() {
    }

    /** "yyyy-MM-dd" 형식의 시작일을 오늘 기준 D-day 텍스트("D-12"/"D-DAY"/"D+3")로 변환한다. */
    public static String formatDday(String startDate) {
        if (startDate == null || startDate.isEmpty()) {
            return "";
        }

        SimpleDateFormat format = new SimpleDateFormat(ISO_DATE_PATTERN, Locale.KOREA);
        format.setLenient(false);
        try {
            Date start = format.parse(startDate);
            if (start == null) {
                return "";
            }

            Calendar today = truncateToDate(Calendar.getInstance());
            Calendar target = Calendar.getInstance();
            target.setTime(start);
            truncateToDate(target);

            long days = TimeUnit.MILLISECONDS.toDays(target.getTimeInMillis() - today.getTimeInMillis());
            if (days == 0) {
                return "D-DAY";
            }
            return days > 0 ? "D-" + days : "D+" + Math.abs(days);
        } catch (ParseException e) {
            return "";
        }
    }

    private static Calendar truncateToDate(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
