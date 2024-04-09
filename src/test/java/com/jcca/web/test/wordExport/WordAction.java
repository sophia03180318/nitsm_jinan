package com.jcca.web.test.wordExport;


import com.jcca.common.utils.WordUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * @author zhaozheng@jccatech.com
 * @date 2021/3/15 14:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WordAction {
    private String filePath; //文件路径
    private String fileName; //文件名称
    private String fileOnlyName; //文件唯一名称

@Test
public void createWord() {
    /** 用于组装word页面需要的数据 */
    Map<String, Object> dataMap = new HashMap<String, Object>();

    /** 组装数据 */
    dataMap.put("userName", "张三");

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
    dataMap.put("currDate", sdf.format(new Date()));

    dataMap.put("content", "这是其它内容这是其它内容这是其它内容这是其它内容这是其它内容这是其它内容这是其它内容这是其它内容这是其它内容这是其它内容这是其它内容这是其它内容这是其它内容");

    List<Map<String, Object>> newsList = new ArrayList<Map<String, Object>>();
    for (int i = 1; i <= 10; i++) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("title", "标题" + i);
        map.put("content", "内容" + (i * 2));
        map.put("author", "作者" + (i * 3));
        newsList.add(map);
    }
    dataMap.put("newsList", newsList);

    /** 文件名称，唯一字符串 */
    Random r = new Random();
    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
    StringBuffer sb = new StringBuffer();
    sb.append(sdf1.format(new Date()));
    sb.append("_");
    sb.append(r.nextInt(100));

//文件路径
    filePath = "/templates/import/";

    //文件唯一名称
    fileOnlyName = "用freemarker导出的Word文档_" + sb + ".doc";

    //文件名称
    fileName = "用freemarker导出的Word文档.doc";

    /** 生成word */
 //   WordUtil.createWord(dataMap, "test.ftl", filePath, fileOnlyName);

}

    }
