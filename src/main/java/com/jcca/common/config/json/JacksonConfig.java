package com.jcca.common.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import cn.hutool.json.JSONNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.serializerByType(JSONNull.class, new JSONNullSerializer());
        return builder;
    }

    public static class JSONNullSerializer extends JsonSerializer<JSONNull> {
        @Override
        public void serialize(JSONNull value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNull(); // 将 JSONNull 序列化为普通的 null
        }
    }
}