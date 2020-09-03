package com.keesail.klh.common.base;

import com.keesail.klh.service.KlhCouponsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @ClassName : StaticScheduleTask
 * @Description : 定时任务
 * @Author : YangYu
 * @Date: 2020/6/15
 */

@Configuration
@EnableScheduling
@Slf4j
public class StaticScheduleTask {
    @Resource
    private KlhCouponsService klhCouponsService;

    @Scheduled(cron = "0 0 0 1/1 * ?")
    private void configureTasks() {
        log.info("----------------更新优惠券开始job------------------------------"+LocalDateTime.now());
        klhCouponsService.upActivity();
        log.info("-------------------更新优惠券结束job------------------------------"+LocalDateTime.now());
    }
}