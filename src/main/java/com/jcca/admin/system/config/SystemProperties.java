package com.jcca.admin.system.config;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName ProjectProperties
 * @Description TODO
 * @Date 2020/4/9 13:58
 * @Author hanwone
 */
@Data
@Configuration
public class SystemProperties {
    /**
     * 是否开启验证码
     */
    private boolean captchaOpen = false;
}
