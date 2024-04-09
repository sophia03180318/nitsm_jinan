package com.jcca.web2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author 小白龙
 * @date 2020/1/3
 */

@Configuration
@EnableSwagger2
public class ApiSwaggerConfig {

    @Bean("nitsmAPI")
    public Docket createRest() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("API")
                .forCodeGeneration(true)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.jcca.web"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("TDCS/CTC-API数据接口")
                .description("本接口文档仅供开发测试使用，线上环境必须关闭！！！")
                .contact(new Contact("中航鼎成", "", ""))
                .version("v2.0")
                .build();
    }
}
