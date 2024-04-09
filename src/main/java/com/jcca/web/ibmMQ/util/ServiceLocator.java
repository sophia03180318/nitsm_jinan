package com.jcca.web.ibmMQ.util;


import com.jcca.web.ibmMQ.support.INamedServiceProvider;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public final class ServiceLocator {
    public static <S extends INamedServiceProvider> S findService(String name, Class<S> serviceType) {
        return (S) findServices(serviceType).get(name);
    }

    public static <S extends INamedServiceProvider> Map<String, S> findServices(Class<S> serviceType) {
        Map<String, S> services = new HashMap<String, S>();
        Iterator<S> iter = ServiceLoader.load(serviceType).iterator();
        while (iter.hasNext()) {
            INamedServiceProvider iNamedServiceProvider = (INamedServiceProvider) iter.next();
            services.put(iNamedServiceProvider.getName(), (S) iNamedServiceProvider);
        }
        return services;
    }
}


