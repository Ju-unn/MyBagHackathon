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
            // 1년(365일)을 넘어가는 차이는 배지 안에서 숫자가 너무 길어지므로 "365+"로 캡핑한다.
            long absDays = Math.abs(days);
            String magnitude = absDays > 365 ? "365+" : String.valueOf(absDays);
            return days > 0 ? "D-" + magnitude : "D+" + magnitude;
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

    /** 오늘이 "yyyy-MM-dd" 시작일~종료일 사이(포함)인지 여부. 파싱 실패/빈 값이면 false. */
    public static boolean isTravelingNow(String startDate, String endDate) {
        if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            return false;
        }

        SimpleDateFormat format = new SimpleDateFormat(ISO_DATE_PATTERN, Locale.KOREA);
        format.setLenient(false);
        try {
            Date start = format.parse(startDate);
            Date end = format.parse(endDate);
            if (start == null || end == null) {
                return false;
            }

            Calendar today = truncateToDate(Calendar.getInstance());
            Calendar startCal = truncateToDate(calendarOf(start));
            Calendar endCal = truncateToDate(calendarOf(end));
            return !today.before(startCal) && !today.after(endCal);
        } catch (ParseException e) {
            return false;
        }
    }

    private static Calendar calendarOf(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
}
