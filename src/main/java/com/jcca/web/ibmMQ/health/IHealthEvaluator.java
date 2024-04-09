package com.jcca.web.ibmMQ.health;

import java.util.Map;

public interface IHealthEvaluator {
    HealthResult evaluate(String paramString, Map<String, Object> paramMap);

    void validate(String paramString);
}