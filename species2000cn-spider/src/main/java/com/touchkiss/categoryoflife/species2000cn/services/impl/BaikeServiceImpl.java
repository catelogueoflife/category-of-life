package com.touchkiss.categoryoflife.species2000cn.services.impl;

import com.touchkiss.categoryoflife.species2000cn.bean.BaikeResponse;
import com.touchkiss.categoryoflife.species2000cn.services.BaikeService;
import com.touchkiss.categoryoflife.species2000cn.utils.CategoryUtils;
import com.touchkiss.categoryoflife.utils.NumberUtil;
import com.touchkiss.categoryoflife.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2020/07/08 23:13
 *
 * @author Touchkiss
 */
@Service
public class BaikeServiceImpl implements BaikeService {
    private final static Pattern NumberPattern = Pattern.compile("[0-9]+");
    public final static String SPECIAL_CHARCODE = "[\"!@#$%^&*（）\\(\\)\\-_=+…·\\~`:'“”‘、\\[\\]|\\\\?/,？\\.，。：；;{}]";

    @Override
    public List<BaikeResponse> query(String word) {
        List<BaikeResponse> result = new ArrayList<>();
        try {
            Set<String>titleSet=new HashSet<>();
            String key = word, property = "";
            if (word.contains(" ")) {
                int endIndex = key.indexOf(" ");
                key = word.substring(0, endIndex);
                property = word.substring(endIndex + 1);
            }
            if (word.contains("的")) {
                int endIndex = word.indexOf("的");
                key = word.substring(0, endIndex);
                property = word.substring(endIndex + 1);
            }

            //返回完全匹配答案
            boolean propertyNotBlank = StringUtils.isNotBlank(property);
            Set<String> keySet = CategoryUtils.matchCase(property);

            Document document = Jsoup.parse(new URL("https://baike.baidu.com/item/" + key), 20000);
            String documentTitle = StringUtil.substringAtIndexOf(document.title(), " ", "（", "_");
            Elements elementsByClass = document.getElementsByClass("main-content");
            if (!CollectionUtils.isEmpty(elementsByClass)) {
                Element mainContent = elementsByClass.get(0);
                Iterator<Element> iterator = mainContent.getAllElements().iterator();
                final StringBuilder title = new StringBuilder();
                final StringBuilder contentBuilder = new StringBuilder();
                Stack<String> titleStack = new Stack<>();
                Stack<Integer> levelStack = new Stack<>();
                while (iterator.hasNext()) {
                    Element next = iterator.next();
                    String currentText = next.text();
                    if (next.hasClass("lemma-summary")) {
                        title.append("简介");
                        contentBuilder.append(currentText);
                        String tempTitle = removePreferences(title.toString()).replace(documentTitle, "");
                        if (titleSet.contains(tempTitle)){
                            continue;
                        }
                        titleSet.add(tempTitle);
                        String tempValue = removePreferences(contentBuilder.toString());
                        BaikeResponse tempItem = new BaikeResponse(tempTitle, tempValue);
                        if (tempTitle.contains(key)){
                            return new ArrayList(Collections.singletonList(tempItem));
                        }
                        if (propertyNotBlank) {
                            for (String k : keySet) {
                                if (tempTitle.contains(k)) {
                                    return new ArrayList(Collections.singletonList(tempItem));
                                }
                            }
                        }
                        result.add(tempItem);
                        title.delete(0, title.length());
                        contentBuilder.delete(0, contentBuilder.length());
                    } else if (next.hasClass("basic-info")) {
                        String finalKey = key;
                        for (Element basicInfoBlock : next.getAllElements()) {
                            for (Element basicInfoItem : basicInfoBlock.getAllElements()) {
                                if (basicInfoItem.hasClass("name")) {
                                    title.append(basicInfoItem.text());
                                } else if (basicInfoItem.hasClass("value")) {
                                    String tempTitle2 = removePreferences(title.toString()).replace(documentTitle, "");
                                    if (titleSet.contains(tempTitle2)){
                                        continue;
                                    }
                                    titleSet.add(tempTitle2);
                                    String tempValue2 = removePreferences(basicInfoItem.text());
                                    BaikeResponse tempItem2 = new BaikeResponse(tempTitle2, tempValue2);
                                    if (tempTitle2.contains(finalKey)) {
                                        return new ArrayList(Collections.singletonList(tempItem2));
                                    }
                                    if (propertyNotBlank) {
                                        for (String k : keySet) {
                                            if (tempTitle2.contains(k)) {
                                                return new ArrayList(Collections.singletonList(tempItem2));
                                            }
                                        }
                                    }
                                    result.add(tempItem2);
                                    title.delete(0, title.length());
                                    contentBuilder.delete(0, contentBuilder.length());
                                }
                            }
                        }
                    } else if (next.hasClass("para-title")) {
                        currentText = removePreferences(currentText).replace(documentTitle, "");
                        if (StringUtils.isBlank(currentText)) {
                            continue;
                        }
                        Optional<String> levelClass = next.classNames().stream().filter(name -> name.matches("level-[0-9]+")).findFirst();
                        if (levelClass.isPresent()) {
                            String currLevel = levelClass.get();
                            Matcher matcher = NumberPattern.matcher(currLevel);
                            if (matcher.find()) {
                                int levelNumber = NumberUtil.stringToInt(matcher.group(0));
                                if (!titleStack.empty()) {
                                    String tempTitle3 = String.join("-", new ArrayList<>(titleStack));
                                    if (titleSet.contains(tempTitle3)){
                                        continue;
                                    }
                                    titleSet.add(tempTitle3);
                                    String tempValue3 = removePreferences(contentBuilder.toString());
                                    BaikeResponse tempItem3 = new BaikeResponse(tempTitle3, tempValue3);
                                    if (tempTitle3.contains(key)) {
                                        return new ArrayList(Collections.singletonList(tempItem3));
                                    }
                                    if (propertyNotBlank) {
                                        for (String k : keySet) {
                                            if (tempTitle3.contains(k)) {
                                                return new ArrayList(Collections.singletonList(tempItem3));
                                            }
                                        }
                                    }
                                    result.add(tempItem3);
                                    contentBuilder.delete(0, contentBuilder.length());
                                }
//                            当前栈为空,第一次碰到para-title
                                int search = levelStack.search(levelNumber);
                                if (search > -1) {
                                    for (int i = 0; i < search; i++) {
                                        titleStack.pop();
                                        levelStack.pop();
                                    }
                                }
                                titleStack.push(currentText);
                                levelStack.push(levelNumber);
                            }
                        }
                    } else if (next.hasClass("para")) {
                        contentBuilder.append(currentText);
                    }
                }
            }
        } catch (Exception ignore) {

        }
        return result;
    }

    String removePreferences(String source) {
        return source.replace(" 编辑", "").replace(" ", "").replaceAll("[ \\[]*[\\u2460-\\u277f-]+[\\] ]*", "").replaceAll("[ ]*\\[[ ]*[0-9-]+[ ]*\\][ ]*", "").replaceAll("（[见如]*图[0-9-]+）", "");
    }
}
