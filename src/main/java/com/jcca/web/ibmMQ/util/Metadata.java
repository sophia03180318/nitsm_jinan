package com.jcca.web.ibmMQ.util;


import com.ibm.mq.pcf.PCFException;
import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.PCFParam;
import com.jcca.web.ibmMQ.common.ErrorConstants;
import com.jcca.web.ibmMQ.common.InvalidMetadataException;
import com.jcca.web.ibmMQ.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;

@ThreadSafe
public enum Metadata {
    Queue(QueueData.class),
    Topic(TopicData.class),
    Channel(ChannelData.class),
    MQTTChannel(MQTTChannelData.class),
    QueueManager(QMgrData.class),
    Listener(ListenerData.class);

    static {
        log = LoggerFactory.getLogger(Metadata.class);
        namePattern = Pattern.compile("^[\\w.-]+$");
    }

    private final transient Map<String, FieldHolder> fields = new HashMap<String, FieldHolder>();
    private final transient Map<String, List<String>> measurementMapping = new HashMap<String, List<String>>();
    private final transient Map<String, Map<Integer, Enum<?>>> enumMappings = new HashMap<String, Map<Integer, Enum<?>>>();

    private static final Logger log;

    private static final String DEFAULT = "DEFAULT";

    private static final Pattern namePattern;

    private transient Constructor constructor;
    private transient Class dataType;

    Metadata(Class clazz) {
        processMetadata(clazz);
    }

    private StatisticalData newInstance(Monitor monitor) throws Exception {
        StatisticalData data = (StatisticalData) this.constructor.newInstance(new Object[]{monitor});
        data.setCaptureTime(new Date());
        return data;
    }

    public Object invoke(Object object, PCFMessage response) throws Exception {
        return invoke(object, response, null, null);
    }

    private Object invoke(Object object, PCFMessage response, List<FieldHolder> workingFields, Listener listener) throws Exception {
        Assert.notNull(object);
        Assert.notNull(response);
        if (workingFields == null) {
            workingFields = new ArrayList<FieldHolder>(this.fields.values());
        }
        List<Field> deferredFields = new ArrayList<Field>();
        for (FieldHolder holder : workingFields) {
            Field field = holder.field;
            try {
                if (holder.attr.option() == 12) {
                    deferredFields.add(field);
                    continue;
                }
                Object value = null;
                int attr = holder.attr.value();
                Class type = field.getType();
                if (type == int.class || type == boolean.class || type.isEnum()) {
                    value = Integer.valueOf(response.getIntParameterValue(attr));
                    if (type == boolean.class) {
                        switch (((Integer) value).intValue()) {
                            case 0:
                                value = Boolean.FALSE;
                                break;
                            case 1:
                                value = Boolean.TRUE;
                                break;
                        }
                    }
                    if (type.isEnum()) {
                        value = getEnum(type, ((Integer) value).intValue());
                    }
                } else if (type == String.class) {
                    value = response.getStringParameterValue(attr);
                    value = (value == null) ? null : ((String) value).trim();
                } else if (type == long.class) {
                    value = Long.valueOf(response.getInt64ParameterValue(attr));
                }
                field.setAccessible(true);
                field.set(object, value);
                if (listener != null) {
                    listener.onPropertySet(field.getName(), value);
                }
            } catch (PCFException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Handle field: {}, got MQ reason code: {}, ignored!", field.getName(), Integer.valueOf(e.getReason()));
                }
                if (e.getReason() == 3014 || e.getReason() == 3015) {
                    continue;
                }
                throw e;
            }
        }
        for (Field field : deferredFields) {
            Object value = BeanUtil.getValueWithGetter(object, field);
            if (listener != null) {
                listener.onPropertySet(field.getName(), value);
            }
        }
        return object;
    }

    public StatisticalData invoke(Monitor monitor, PCFMessage response, boolean measureOnly) throws Exception {
        return invoke(monitor, response, measureOnly, null);
    }

    public StatisticalData invoke(Monitor monitor, PCFMessage response, boolean measureOnly, Listener listener) throws Exception {
        return invoke(null, monitor, response, measureOnly, listener);
    }

    public StatisticalData invoke(StatisticalData existingData, Monitor monitor, PCFMessage response, boolean measureOnly, Listener listener) throws Exception {
        StatisticalData data = existingData;
        if (data == null) {
            data = newInstance(monitor);
        }
        List<FieldHolder> workingFields = new ArrayList<FieldHolder>();
        if (measureOnly) {
            List<String> allMeasurables = measurementsOf(monitor.getCategory(), monitor.getObjectType());
            for (String measurement : allMeasurables) {
                workingFields.add(this.fields.get(measurement));
            }
        } else {
            workingFields.addAll(this.fields.values());
        }
        return (StatisticalData) invoke(data, response, workingFields, listener);
    }

    public static Class<?> dataTypeOf(Monitor.Category category) {
        return valueOf(category.name()).getDataType();
    }

    public static StatisticalData newStatisticalDataFor(Monitor monitor) throws Exception {
        Assert.notNull(monitor);
        Assert.notNull(monitor.getCategory());
        Metadata metadata = valueOf(monitor.getCategory());
        return metadata.newInstance(monitor);
    }

    public static List<String> objectTypesOf(Monitor.Category category) {
        switch (category) {
            case Channel:
                return Enums.toList(ChannelData.ChannelType.class);
            case Queue:
                return Enums.toList(QueueData.QueueType.class);
            case Topic:
                return Enums.toList(TopicData.TopicType.class);
            case QueueManager:
            case Listener:
                return Arrays.asList(new String[]{category.name()});
        }
        return Collections.emptyList();
    }


    public static List<String> measurementsOf(String category, String objectType) {
        Monitor.Category cate = Enums.valueOf(category, Monitor.Category.class);
        if (cate == null) {
            throw new InvalidMetadataException(40004, ErrorConstants.Message.MSG_INVALID_MONITOR_CATEGORY_2, new Object[]{category,
                    Arrays.toString(Monitor.Category.values())});
        }
        return measurementsOf(cate, objectType);
    }

    public static List<String> measurementsOf(Monitor.Category category, String objectType) {
        Metadata metadata = valueOf(category.name());
        return metadata.getMeasurements(objectType);
    }

    public static boolean isValidMetadataName(String name) {
        return namePattern.matcher(name).matches();
    }

    public Class<?> getDataType() {
        return this.dataType;
    }

    public Monitor.Category getCategory() {
        return Monitor.Category.valueOf(name());
    }

    public Enum<?> getEnum(Class<?> type, int pcfAttr) {
        Map<Integer, Enum<?>> map = this.enumMappings.get(type.getName());
        if (map == null) {
            return null;
        }
        return map.get(Integer.valueOf(pcfAttr));
    }

    public List<String> getMeasurements(String objectType) {
        List<String> measurements = null;
        if (objectType != null && !objectType.equalsIgnoreCase("N/A")) {
            List<String> availableObjectTypes = objectTypesOf(getCategory());
            if (!CollectionsMine.containsIgnoreCase(availableObjectTypes, objectType.trim())) {
                throw new InvalidMetadataException(40005, ErrorConstants.Message.MSG_INVALID_MONITOR_OBJECTTYPE_2, new Object[]{objectType,
                        getCategory(), Arrays.toString(availableObjectTypes.toArray())});
            }
            measurements = this.measurementMapping.get(objectType.trim().toUpperCase());
            if (measurements == null) {
                measurements = this.measurementMapping.get("DEFAULT");
            }
        } else {
            measurements = this.measurementMapping.get("DEFAULT");
        }
        return (measurements == null) ? Collections.emptyList() : measurements;
    }

    private void addMeasurment(String objectType, String field) {
        List<String> measurements = this.measurementMapping.get(objectType);
        if (measurements == null) {
            measurements = new ArrayList<String>();
            this.measurementMapping.put(objectType, measurements);
        }
        measurements.add(field);
    }

    private void addField(Field field, PCFParam attr) {
        this.fields.put(field.getName(), new FieldHolder(field, attr));
        Class fieldType = field.getType();
        if (fieldType.isEnum()) {
            addEnum(fieldType);
        }
    }

    private void addEnum(Class fieldType) {
        for (Object o : fieldType.getEnumConstants()) {
            Enum<?> e = (Enum) o;
            try {
                Field f = e.getClass().getDeclaredField("value");
                f.setAccessible(true);
                Object v = f.get(e);
                if (v instanceof Integer) {
                    Map<Integer, Enum<?>> m = this.enumMappings.get(fieldType.getName());
                    if (m == null) {
                        m = new HashMap<Integer, Enum<?>>();
                        this.enumMappings.put(fieldType.getName(), m);
                    }
                    m.put((Integer) v, Enum.valueOf(fieldType, e.name()));
                }
            } catch (NoSuchFieldException e1) {
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    static class FieldHolder {
        Field field;
        PCFParam attr;

        FieldHolder(Field field, PCFParam attr) {
            this.field = field;
            this.attr = attr;
        }
    }

    static class FieldComparator
            implements Comparator<Field> {
        public int compare(Field field1, Field field2) {
            /* 460 */
            if (field1.getType().isEnum())
                return -1;
            if (field2.getType().isEnum())
                return 1;
            return 0;
        }
    }

    private void processMetadata(Class clazz) {
        this.dataType = clazz;
        try {
            this.constructor = clazz.getDeclaredConstructor(new Class[]{Monitor.class});
        } catch (Exception ex) {
        }
        Field[] fields = clazz.getDeclaredFields();
        List<Field> fieldList = Arrays.asList(fields);
        Collections.sort(fieldList, new FieldComparator());
        for (Field field : fieldList) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            PCFParam attr = field.getAnnotation(PCFParam.class);
            if (attr == null) {
                continue;
            }
            addField(field, attr);
            int option = attr.option();
            int[] types = attr.supportType();
            if (option >= 11) {
                if (types.length == 0) {
                    addMeasurment("DEFAULT", field.getName());
                    continue;
                }
                for (int type : types) {
                    Enum<?> e = getEnum(attr.supportTypeClass(), type);
                    addMeasurment(e.name().toUpperCase(), field.getName());
                }
            }
        }
    }

    public static interface Listener {
        void onPropertySet(String param1String, Object param1Object);
    }

    public static Metadata valueOf(Monitor.Category category) {
        return valueOf(category.name());
    }
}