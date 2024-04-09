package com.jcca.common.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author zhaozheng@jccatech.com
 * @date 2021/3/12 10:23
 */
@Slf4j
public class WordUtil {

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public static void createWord(Map dataMap, String templateName, String fileName, HttpServletResponse response) {
        try {

            response.setContentType("application/msword;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

            //创建配置实例
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);

            //设置编码
            configuration.setDefaultEncoding("UTF-8");

            //ftl模板文件统一放至 com.lun.template 包下面
            configuration.setClassForTemplateLoading(WordUtil.class, "/templates/wordTemplate");

            //获取模板
            Template template = configuration.getTemplate(templateName);

            Writer out = response.getWriter();


            //生成文件
            template.process(dataMap, out);

            //关闭流
            out.flush();
            out.close();
        } catch (Exception e) {
            log.error("创建word文件异常-WordUtil", e);

        }
    }
}
