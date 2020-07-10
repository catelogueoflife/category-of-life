package com.touchkiss.categoryoflife.spider.life.services.impl;

import com.touchkiss.categoryoflife.life.bean.LifeCategory;
import com.touchkiss.categoryoflife.life.bean.LifeCategorySourceDatabase;
import com.touchkiss.categoryoflife.life.bean.SourceDatabase;
import com.touchkiss.categoryoflife.life.services.LifeCategoryDaoService;
import com.touchkiss.categoryoflife.life.services.LifeCategorySourceDatabaseDaoService;
import com.touchkiss.categoryoflife.life.services.SourceDatabaseDaoService;
import com.touchkiss.categoryoflife.spider.base.ThreadPoolExecutors;
import com.touchkiss.categoryoflife.spider.constants.UrlConstants;
import com.touchkiss.categoryoflife.spider.life.bean.bo.LifeCategoryBO;
import com.touchkiss.categoryoflife.spider.life.bean.convertor.LifeCategoryConvertor;
import com.touchkiss.categoryoflife.spider.life.bean.convertor.SourceDatabaseConvertor;
import com.touchkiss.categoryoflife.spider.life.bean.response.LifeCategoryResponse;
import com.touchkiss.categoryoflife.spider.life.services.LifeCategorySpiderService;
import com.touchkiss.categoryoflife.spider.life.services.LifeCategorySpiderThread;
import com.touchkiss.categoryoflife.spider.species.services.SpeciesSpiderService;
import com.touchkiss.categoryoflife.utils.GsonUtil;
import com.touchkiss.categoryoflife.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2020/03/18 17:33
 *
 * @author Touchkiss
 */
@Service
@Slf4j
public class LifeCategorySpiderServiceImpl implements LifeCategorySpiderService {
    Pattern ID_PATTERN = Pattern.compile("[a-z0-9]{32}");
    @Autowired
    private LifeCategoryDaoService lifeCategoryDaoService;
    @Autowired
    private SourceDatabaseDaoService sourceDatabaseDaoService;
    @Autowired
    private LifeCategorySourceDatabaseDaoService lifeCategorySourceDatabaseDaoService;
    @Autowired
    private LifeCategoryConvertor lifeCategoryConvertor;
    @Autowired
    private SourceDatabaseConvertor sourceDatabaseConvertor;
    @Autowired
    private SpeciesSpiderService speciesSpiderService;

    @Override
    public int fetchLifeCategoryFromWeb(int id) throws IOException {
        int result = 0;
        log.info("=============开始抓取物种id：" + id + "======");
        LifeCategory lifeCategoryInDB = lifeCategoryDaoService.selectById(id);
        if (lifeCategoryInDB == null || !"species".equals(lifeCategoryInDB.getType())) {
            String url = UrlConstants.LIFE_CATEGORY_TREE_FETCH_URL + id;
            String response = HttpUtil.get(url);
            LifeCategoryResponse lifeCategoryResponse = GsonUtil.fromJson(response, LifeCategoryResponse.class);
            if (lifeCategoryResponse != null && !CollectionUtils.isEmpty(lifeCategoryResponse.getItems())) {
                for (LifeCategoryBO lifeCategoryBO : lifeCategoryResponse.getItems()) {
                    LifeCategory lifeCategory = lifeCategoryConvertor.convert(lifeCategoryBO);
                    if (lifeCategory != null) {
                        if (lifeCategoryDaoService.selectById(lifeCategory.getId()) == null) {
                            lifeCategory.setFetched(false);
                            lifeCategoryDaoService.insert(lifeCategory);
                        }
                        lifeCategoryBO.getSource_databases().forEach(sourceDatabaseBO -> {
                            if (sourceDatabaseDaoService.selectById(sourceDatabaseBO.getSource_database_id()) == null) {
                                SourceDatabase sourceDatabase = sourceDatabaseConvertor.convert(sourceDatabaseBO);
                                sourceDatabaseDaoService.insert(sourceDatabase);
                            }
                            if (CollectionUtils.isEmpty(lifeCategorySourceDatabaseDaoService.queryLifeCategorySourceDatabaseList(new HashMap() {{
                                put("lifeId", lifeCategory.getId());
                                put("databaseId", sourceDatabaseBO.getSource_database_id());
                            }}))) {
                                LifeCategorySourceDatabase lifeCategorySourceDatabase = new LifeCategorySourceDatabase();
                                lifeCategorySourceDatabase.setLifeId(lifeCategory.getId());
                                lifeCategorySourceDatabase.setDatabaseId(sourceDatabaseBO.getSource_database_id());
                                lifeCategorySourceDatabaseDaoService.insert(lifeCategorySourceDatabase);
                            }
                        });
                    }
                }
                result = lifeCategoryResponse.getItems().size();
            }else{
                log.error("抓取{}返回结果不符合预期",url);
                log.info("http返回结果:{}",response);
            }
        } else if (lifeCategoryInDB != null && "species".equals(lifeCategoryInDB.getType())) {
            String url = lifeCategoryInDB.getUrl();
            Matcher matcher = ID_PATTERN.matcher(url);
            if (matcher.find()) {
                String species_id = matcher.group(0);
                try {
                    if (speciesSpiderService.fetchSpecies(species_id)) {
                        result = 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (lifeCategoryInDB != null) {
            lifeCategoryInDB.setFetched(true);
            lifeCategoryDaoService.updateById(lifeCategoryInDB, id);
        }
        return result;
    }

    @Override
    public void autoFetch() {
        List<LifeCategory> list = lifeCategoryDaoService.queryLifeCategoryList(new HashMap() {{
            put("id_get","5000000");
            put("fetched", "0");
            put("type_ne", "species");
            put("limit", "limit 5");
        }});
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(lifeCategory -> {
                try {
                    fetchLifeCategoryFromWeb(lifeCategory.getId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
//            list.stream()
//                    .map(lifeCategory -> new LifeCategorySpiderThread(lifeCategory.getId(), this))
//                    .forEach(ThreadPoolExecutors.pool::execute);
        }
    }
}
