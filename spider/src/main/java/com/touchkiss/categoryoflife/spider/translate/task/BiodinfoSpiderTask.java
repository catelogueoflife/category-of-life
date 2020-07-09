package com.touchkiss.categoryoflife.spider.translate.task;

import com.touchkiss.categoryoflife.spider.translate.services.BiodinfoTranslateSpiderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created on 2020/03/21 17:30
 *
 * @author Touchkiss
 */
@Component
@Slf4j
public class BiodinfoSpiderTask {
    @Autowired
    private BiodinfoTranslateSpiderService biodinfoTranslateSpiderService;

//    @Scheduled(cron = "* * * * * ?")
    public void autoFetch() {
        log.info("====================定时任务抓取中文翻译==========");
        biodinfoTranslateSpiderService.autoFetch();
        log.info("====================定时任务抓取中文翻译==结束=====");
    }
}
