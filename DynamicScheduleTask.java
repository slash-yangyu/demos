package com.keesail.klh.common;

import com.keesail.klh.dao.KlhCouponsDao;
import com.keesail.klh.entity.KlhUserCoupons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.time.LocalDateTime;

/**
 * @ClassName : DynamicScheduleTask
 * @Description : 动态定时器
 * @Author : YangYu
 * @Date: 2020/6/15
 */
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class DynamicScheduleTask  implements SchedulingConfigurer {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("ss mm HH dd MM ? yyyy");

    @Autowired      //注入mapper
    @SuppressWarnings("all")
    KlhCouponsDao cronMapper;

    /**
     * 执行定时任务.
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        getCron(cronMapper.getTime()).forEach(s -> taskRegistrar.addTriggerTask(() -> System.out.println("干: " + LocalDateTime.now()),triggerContext -> new CronTrigger(s).nextExecutionTime(triggerContext)));
    }



    /***
     *  功能描述：日期转换cron表达式
     * @param date
     * @return
     */
    public static String getCron(Date date) {
        String formatTimeStr = null;
        if (Objects.nonNull(date)) {
            formatTimeStr = sdf.format(date);
        }
        return formatTimeStr;
    }
    
}
