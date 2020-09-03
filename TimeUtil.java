package com.feihe.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @Author LiShuai
 * 评论排序
 * @Date 2020/6/2 17:19
 */
public class TimeUtil {
    private static final long ONE_MINUTE = 60000L;
    private static final long ONE_HOUR = 3600000L;
    private static final long ONE_DAY = 86400000L;
    private static final long ONE_WEEK = 604800000L;

    private static final String ONE_SECOND_AGO = "秒前";
    private static final String ONE_MINUTE_AGO = "分钟前";
    private static final String ONE_HOUR_AGO = "小时前";
    private static final String ONE_DAY_AGO = "天前";
    private static final String ONE_MONTH_AGO = "月前";
    private static final String ONE_YEAR_AGO = "年前";

    public static String format(Date createTime) {
        //LocalDateTime time = LocalDateTime.parse(createTime, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        LocalDateTime time = createTime.toInstant().atZone( ZoneId.systemDefault()).toLocalDateTime();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = sdf.format(createTime);
        long compareTime = getTimestampOfDateTime(time);

        LocalDateTime now = LocalDateTime.now();
        long rightTime = getTimestampOfDateTime(now);

        long delta = rightTime - compareTime;
        //1小时以内  显示样例  xx分钟前
        if (delta < 45L * ONE_MINUTE) {
            long minutes = toMinutes(delta);
            return (minutes <= 0 ? 1 : minutes) + ONE_MINUTE_AGO;
        }
        //超过1小时、小于24小时  显示样例  xx小时前
        if (delta < 24L * ONE_HOUR) {
            long hours = toHours(delta);
            return (hours <= 0 ? 1 : hours) + ONE_HOUR_AGO;
        }
        //消息超过或等于1天、小于两天  显示样例 昨天 12:35
        if (delta < 48L * ONE_HOUR) {
            return "昨天 "+format.substring(11,16);
        }
        //消息大于等于2天、未跨年  显示样例 5/4 12:36
        if (delta < 30L * ONE_DAY) {
            long days = toDays(delta);
            return format.substring(5,16);
        }
        //消息时间跨年  显示样例  2019/06/02 09:30
        return format.substring(0,16);
    }

    private static long toSeconds(long date) {
        return date / 1000L;
    }

    private static long toMinutes(long date) {
        return toSeconds(date) / 60L;
    }

    private static long toHours(long date) {
        return toMinutes(date) / 60L;
    }

    private static long toDays(long date) {
        return toHours(date) / 24L;
    }

    private static long toMonths(long date) {
        return toDays(date) / 30L;
    }

    private static long toYears(long date) {
        return toMonths(date) / 365L;
    }


    public static long getTimestampOfDateTime(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return instant.toEpochMilli();
    }

    public static void main(String[] args) throws Exception {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date parse = sdf.parse("2019-12-30 14:31:25");
        System.out.println(format(parse));
    }
}
