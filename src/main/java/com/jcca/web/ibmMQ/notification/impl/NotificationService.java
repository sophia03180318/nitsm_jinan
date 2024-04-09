package com.jcca.web.ibmMQ.notification.impl;


import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jcca.web.ibmMQ.common.ThreadFactories;
import com.jcca.web.ibmMQ.config.Configuration;
import com.jcca.web.ibmMQ.domain.StatisticalData;
import com.jcca.web.ibmMQ.notification.INotificationProvider;
import com.jcca.web.ibmMQ.notification.INotificationService;
import com.jcca.web.ibmMQ.support.BeanLocator;
import com.jcca.web.ibmMQ.util.ServiceLocator;
import com.jcca.web.ibmMQ.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service("notificationService")
public class NotificationService implements INotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final int EXECUTOR_DEFAULT_POOL_SIZE = Math.min(10, Runtime.getRuntime().availableProcessors());

    private static final String NOTIFICATION_PROVIDER_NAME_SEPARATOR = ",";

    private List<INotificationProvider> providers;

    private ExecutorService executorService;


    @Resource(name = "configuration")
    private Configuration configuration;


    @Resource(name = "beanLocator")
    private BeanLocator beanLocator;


    @PostConstruct
    private void init() {
        Map<String, INotificationProvider> providerRegistry = Maps.newHashMap();
        Collection<INotificationProvider> buildinProviders = this.beanLocator.findBeans(INotificationProvider.class);
        if (log.isDebugEnabled()) {
            log.debug("Found buildin notification providers: {}", buildinProviders);
        }
        registerProviders(providerRegistry, buildinProviders);
        Collection<INotificationProvider> externalProviders = ServiceLocator.findServices(INotificationProvider.class).values();
        registerProviders(providerRegistry, externalProviders);
        final Set<String> validEnabledProviders = Sets.newHashSet();
        String providerNames = this.configuration.getNotificationProviders();
        if (!Strings.isNullOrEmpty(providerNames)) {
            Iterable<String> enabledProviders = Splitter.on(",").omitEmptyStrings().trimResults().split(providerNames);
            for (String providerName : enabledProviders) {
                if (!providerRegistry.containsKey(providerName)) {
                    if (log.isWarnEnabled()) {
                        log.warn("No such notification provider with the name '{}' is found in config.properties, ignored.", providerName);
                    }
                    continue;
                }
                validEnabledProviders.add(providerName);
            }
        }
        if (validEnabledProviders.isEmpty()) {
            validEnabledProviders.add("default");
        }
        if (log.isInfoEnabled()) {
            log.info("Enabled notification providers: {}",
                    Joiner.on(",").join(validEnabledProviders));
        }
        this.providers = (List<INotificationProvider>) ImmutableList.copyOf(Iterables.filter(providerRegistry.values(), new Predicate<INotificationProvider>() {
            public boolean apply(INotificationProvider provider) {
                return validEnabledProviders.contains(provider.getName());
            }
        }));
        this.executorService = Executors.newFixedThreadPool(EXECUTOR_DEFAULT_POOL_SIZE,
                ThreadFactories.newDaemonThreadFactory("wmq-monitoring-daemon-pool-notification"));
        if (log.isDebugEnabled()) {
            log.debug("Notification service is started with providers: {}",
                    Joiner.on(",").join(this.providers));
        }
    }

    private void registerProviders(Map<String, INotificationProvider> registry, Collection<INotificationProvider> providers) {
        for (INotificationProvider provider : providers) {
            String name = provider.getName();
            if (Strings.isNullOrEmpty(name)) {
                log.error("Notification provider name is required, cannot be null or empty.");
                continue;
            }
            if (registry.containsKey(name)) {
                if (log.isWarnEnabled()) {
                    log.warn("Notification provider with the name '{}' is conflict with another one, this provider is ignored.", name);
                }
                continue;
            }
            registry.put(name, provider);
        }
    }

    public List<INotificationProvider> getNotificationProviders() {
        return this.providers;
    }

    public void notify(StatisticalData data) {
        for (INotificationProvider provider : this.providers) {
            this.executorService.submit(new Notifier(provider, data));
        }
    }

    @PreDestroy
    private void destroy() {
        this.executorService.shutdown();
        for (INotificationProvider provider : this.providers) {
            try {
                provider.shutdown();
            } catch (Exception e) {
                log.error(String.format("Got error when shutting down notification provider '%s'", new Object[]{provider.getName()}), e);
            }
        }
    }

    private class Notifier implements Runnable {
        private final INotificationProvider provider;
        private final StatisticalData data;


        public Notifier(INotificationProvider provider, StatisticalData data) {
            this.provider = provider;
            this.data = data;
        }

        public void run() {
            try {
                this.provider.notify(this.data);
            } catch (Exception e) {
                log.error(
                        String.format("Notification provider '%s' fail to send notification with data '%s'!", new Object[]{
                                this.provider.getName(), this.data
                        }), e);
            }
        }
    }
}

