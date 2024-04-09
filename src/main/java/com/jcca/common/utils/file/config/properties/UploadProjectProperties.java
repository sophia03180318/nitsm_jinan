package com.jcca.common.utils.file.config.properties;

import com.jcca.common.utils.ToolUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 项目-文件上传配置项
 *
 * @author 小白龙
 * @date 2018/11/6
 */
@Data
@Component
public class UploadProjectProperties {

    /**
     * 上传文件路径
     */
    @Value("${project.upload.file-path}")
    private String filePath;

    /**
     * 上传文件静态访问路径
     */
    @Value("${project.upload.static-path}")
    private String staticPath;
    /**
     * 文件访问服务器地址
     */
    @Value("${project.upload.static-url}")
    private String staticUrl;


    /**
     * 获取文件路径
     */
    public String getFilePath() {
        if (filePath == null) {
            return ToolUtil.getProjectPath() + "/upload/";
        }
        if (!filePath.endsWith("/")) {
            return filePath + "/";
        }
        return filePath;
    }
}
