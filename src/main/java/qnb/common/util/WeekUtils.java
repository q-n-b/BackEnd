package qnb.common.util;

import java.time.*;

import static java.time.temporal.TemporalAdjusters.previousOrSame;
import static java.time.DayOfWeek.MONDAY;

public final class WeekUtils {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private WeekUtils(){}

    /** 이번 주(현재 시각 기준)의 월요일(로컬 날짜) */
    public static LocalDate thisWeekMondayKST() {
        return LocalDate.now(KST).with(previousOrSame(MONDAY));
    }

    /** 다음 주 월요일(주 범위 upper bound) */
    public static LocalDate nextWeekMondayKST() {
        return thisWeekMondayKST().plusWeeks(1);
    }

    /** 이 주의 시간 경계 (월요일 00:00 ~ 다음주 월요일 00:00) */
    public static ZonedDateTime thisWeekStartZdt() {
        return thisWeekMondayKST().atStartOfDay(KST);
    }
    public static ZonedDateTime nextWeekStartZdt() {
        return nextWeekMondayKST().atStartOfDay(KST);
    }
}

