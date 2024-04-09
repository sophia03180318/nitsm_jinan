package com.jcca.web.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @ClassName ProjectVersionConf
 * @Description 获取项目版本
 * @Date 2020/8/20 14:34
 * @Author hanwone
 */
@Component
@PropertySource(value = "classpath:git.properties")
@Data
public class ProjectVersionConf {
    @Value("${git.build.version}")
    private String version;

    @Value("${git.branch}")
    private String branch;

    @Value("${git.commit.id}")
    private String commitId;

    @Value("${git.tags}")
    private String tags;
}
