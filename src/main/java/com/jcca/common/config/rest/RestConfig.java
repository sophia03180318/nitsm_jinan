package com.jcca.common.config.rest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

/**
 * @author Manager
 */
@Configuration
public class RestConfig {

    @Autowired
    private RestTemplateBuilder builder;

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(fetchHttpClient());
        return builder.requestFactory(() -> requestFactory).build();
    }

    private HttpClient fetchHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        //同时最大发送请求数
        builder.setMaxConnTotal(200);
        //最大同时接收响应
        builder.setMaxConnPerRoute(100);
        builder.setDefaultRequestConfig(fetchRequestConfig());
        builder.setSSLContext(fetchSSLContext());
        builder.setSSLHostnameVerifier(fetchHostnameVerifier());
        return builder.build();
    }

    private RequestConfig fetchRequestConfig() {
        Builder builder = RequestConfig.custom();
        builder.setConnectionRequestTimeout(1 * 60 * 1000);
        builder.setConnectTimeout(5 * 1000);
        builder.setSocketTimeout(5 * 60 * 1000);
        return builder.build();
    }

    private SSLContext fetchSSLContext() {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(TrustAllStrategy.INSTANCE);
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HostnameVerifier fetchHostnameVerifier() {
        return NoopHostnameVerifier.INSTANCE;
    }

}
