package com.jcca.web.ibmMQ.service.impl;


import com.ibm.mq.pcf.PCFException;
import com.jcca.web.ibmMQ.command.ICommandProcessor;
import com.jcca.web.ibmMQ.common.*;
import com.jcca.web.ibmMQ.domain.*;
import com.jcca.web.ibmMQ.service.IMetadataService;
import com.jcca.web.ibmMQ.util.Enums;
import com.jcca.web.ibmMQ.util.Metadata;
import com.jcca.web.ibmMQ.util.Strings;
import com.jcca.web.ibmMQ.vo.MQObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


@Service("metadataService")
public class MetadataService implements IMetadataService {
    @Resource(name = "commandProcessor")
    private ICommandProcessor commandProcessor;


    public boolean isQMgrAvailable(Connection connection) {
        return (pingQMgr(connection) == 0);
    }


    public int pingQMgr(final Connection connection) {

        try {

            return (Integer) this.commandProcessor.process("PING_QMGR", new HashMap<String, Object>(1) {
                {
                    this.put("context.connection", connection);
                }
            });

        } catch (Exception e) {

            PCFMessageServiceImpl.checkConnectionException(connection, e);

            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e
                    .getMessage()}, e);

        }

    }

    @Override
    public boolean existObjectName(Connection paramConnection, Monitor.Category paramCategory, String paramString) {

        try {

            return (Boolean) this.commandProcessor.process("EXIST_OBJECT_NAME", new HashMap<String, Object>(3) {
                {
                    this.put("context.connection", paramConnection);
                    this.put("context.object.category", paramCategory);
                    this.put("context.object.name", paramString);
                }
            });

        } catch (Exception e) {

            PCFMessageServiceImpl.checkConnectionException(paramConnection, e);

            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e.getMessage()}, e);

        }

    }


    public String queryQMgrName(final Connection connection) {
        try {

            return (String) this.commandProcessor.process("INQUIRE_QMGR_NAME", new HashMap<String, Object>(1) {
                {
                    this.put("context.connection", connection);
                }
            });

        } catch (Exception e) {

            PCFMessageServiceImpl.checkConnectionException(connection, e);

            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e
                    .getMessage()}, e);

        }

    }


    public List<MQObject> queryListenerNames(Connection connection, String listenerName) {
        return retrieveMQObjectNames(connection, Monitor.Category.Listener, listenerName, null);
    }


    public List<MQObject> queryChannelNames(Connection connection, String channelName, String channelType) {

        boolean isChannelTypePresent = !Strings.isNullOrEmpty(channelType);

        ChannelData.ChannelType type = isChannelTypePresent ? (ChannelData.ChannelType) Enums.valueOf(channelType, ChannelData.ChannelType.class) : null;

        if (type == null && isChannelTypePresent) {

            throw new InvalidMetadataException(40005, ErrorConstants.Message.MSG_INVALID_MONITOR_OBJECTTYPE_2, new Object[]{channelType, Monitor.Category.Channel,
                    Arrays.toString((Object[]) ChannelData.ChannelType.values())});

        }

        return retrieveMQObjectNames(connection, Monitor.Category.Channel, channelName, (Enum<?>) type);

    }

    public List<MQObject> queryQueueNames(Connection connection, String queueName, String queueType) {

        boolean isQueueTypePresent = !Strings.isNullOrEmpty(queueType);

        QueueData.QueueType type = isQueueTypePresent ? (QueueData.QueueType) Enums.valueOf(queueType, QueueData.QueueType.class) : null;

        if (type == null && isQueueTypePresent) {

            throw new InvalidMetadataException(40005, ErrorConstants.Message.MSG_INVALID_MONITOR_OBJECTTYPE_2, new Object[]{queueType, Monitor.Category.Queue,
                    Arrays.toString((Object[]) QueueData.QueueType.values())});

        }

        return retrieveMQObjectNames(connection, Monitor.Category.Queue, queueName, (Enum<?>) type);

    }


    public List<MQObject> queryTopicNames(Connection connection, String topicName, String topicType) {
        boolean isTopicTypePresent = !Strings.isNullOrEmpty(topicType);
        TopicData.TopicType type = isTopicTypePresent ? (TopicData.TopicType) Enums.valueOf(topicType, TopicData.TopicType.class) : null;
        if (type == null && isTopicTypePresent) {
            throw new InvalidMetadataException(40005, ErrorConstants.Message.MSG_INVALID_MONITOR_OBJECTTYPE_2, new Object[]{topicType, Monitor.Category.Topic,
                    Arrays.toString((Object[]) TopicData.TopicType.values())});

        }

        return retrieveMQObjectNames(connection, Monitor.Category.Topic, topicName, (Enum<?>) type);

    }


    public List<String> queryCategories() {
        return Enums.toList(Monitor.CategoryQuery.class);
    }


    public List<String> queryObjectTypes(String category) {

        Monitor.Category cate = (Monitor.Category) Enums.valueOf(category, Monitor.Category.class);

        if (cate == null) {

            throw new InvalidMetadataException(40004, ErrorConstants.Message.MSG_INVALID_MONITOR_CATEGORY_2, new Object[]{category,
                    Arrays.toString(Monitor.Category.values())});

        }

        switch (cate) {
            case Queue:
                return Enums.toList(QueueData.QueueTypeQueue.class);
            case Channel:
                return Enums.toList(ChannelData.ChannelTypeQuery.class);
        }


        return Metadata.objectTypesOf(cate);

    }

    public String queryObjectType(final Connection connection, String category, final String objectName) {

        final Monitor.Category cate = (Monitor.Category) Enums.valueOf(category, Monitor.Category.class);

        if (cate == null) {

            throw new InvalidMetadataException(40004, ErrorConstants.Message.MSG_INVALID_MONITOR_CATEGORY_2, new Object[]{category,
                    Arrays.toString(Monitor.Category.values())});

        }

        try {

            return ((Enum) this.commandProcessor.process("INQUIRE_OBJECT_TYPE", new HashMap<String, Object>(3) {
                {
                    this.put("context.connection", connection);
                    this.put("context.object.category", cate);
                    this.put("context.object.name", objectName);
                }
            })).name();

        } catch (Exception e) {

            PCFMessageServiceImpl.checkConnectionException(connection, e);

            if (e instanceof PCFException) {

                int rc = ((PCFException) e).getReason();

                switch (rc) {

                    case 2085:
                        throw new InvalidMetadataException(40007, ErrorConstants.Message.MSG_UNKNOWN_MONITOR_OBJECTNAME, new Object[]{objectName});
                    case 4008:
                        throw new InvalidMetadataException(40006, ErrorConstants.Message.MSG_INVALID_MONITOR_OBJECTNAME_2, new Object[]{objectName});
                }
            }
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e
                    .getMessage()}, e);
        }
    }

    public List<String> queryMeasurements(String category, String objectType) {
        return Metadata.measurementsOf(category, objectType);
    }

    public StatisticalData queryMonitorDetails(final Monitor monitor) {
        StatisticalData data = null;
        try {

            Iterator<StatisticalData> dataIter = ((List) this.commandProcessor.process(monitor, new HashMap<String, Object>(2) {
                {
                    this.put("context.monitor", monitor);
                    this.put("context.measureonly", false);
                }
            })).iterator();
            data = dataIter.hasNext() ? (StatisticalData) dataIter.next() : null;
        } catch (Exception e) {
            PCFMessageServiceImpl.checkConnectionException(monitor.getConnection(), e);
            throw new StatisticalDataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e
                    .getMessage()}, e);
        }
        if (data == null) {
            throw new StatisticalDataNotFoundException(40405, ErrorConstants.Message.MSG_STATISTICS_NOT_FOUND, new Object[]{monitor
                    .getName(), monitor.getConnection().getName()});
        }
        return data;
    }

    public boolean isMQTTServiceAvailable(final Connection connection) {
        try {
            return (Boolean) this.commandProcessor.process("PING_MQTT", new HashMap<String, Object>(1) {
                {
                    this.put("context.connection", connection);
                }
            });
        } catch (Exception e) {
            PCFMessageServiceImpl.checkConnectionException(connection, e);
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e
                    .getMessage()}, e);
        }
    }

    public List<MQTTChannelData> queryMQTTChannelStatistics(final Connection connection) {
        try {
            return (List) this.commandProcessor.process("INQUIRE_MQTT_CHANNEL", new HashMap<String, Object>(1) {
                {
                    this.put("context.connection", connection);
                }
            });
        } catch (Exception e) {
            PCFMessageServiceImpl.checkConnectionException(connection, e);
            if (e instanceof PCFException && (
                    (PCFException) e).getReason() == 6130) {
                throw new StatisticalDataNotAvailableException(40909, ErrorConstants.Message.MSG_MQTT_SERVICE_NOT_AVAILABLE, new Object[]{connection});
            }
            throw new StatisticalDataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e
                    .getMessage()}, e);
        }
    }


    public void createSubscription(final Connection connection, final String subscriptionName, final String topicString, final String destinationQueue) {
        try {
            this.commandProcessor.process("CREATE_SUBSCRIPTION", new HashMap<String, Object>(4) {
                {
                    this.put("context.connection", connection);
                    this.put("context.subscription.name", subscriptionName);
                    this.put("context.subscription.topicstring", topicString);
                    this.put("context.subscription.destination", destinationQueue);
                }
            });
        } catch (Exception e) {
            PCFMessageServiceImpl.checkConnectionException(connection, e);
            if (e instanceof PCFException) {
                int rc = ((PCFException) e).getReason();
                switch (rc) {
                    case 3311:
                        throw new IllegalMetadataStateException(40913, ErrorConstants.Message.MSG_MQ_SUB_ALREADY_EXISTS, new Object[]{subscriptionName, connection});
                    case 3317:
                        throw new InvalidMetadataException(40015, ErrorConstants.Message.MSG_MQ_INVALID_DESTINATION, new Object[]{destinationQueue, subscriptionName});
                }
            }
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e
                    .getMessage()}, e);
        }
    }

    public void deleteSubscription(final Connection connection, final String subscriptionName) {
        try {
            this.commandProcessor.process("DELETE_SUBSCRIPTION", new HashMap<String, Object>(2) {
                {
                    this.put("context.connection", connection);
                    this.put("context.subscription.name", subscriptionName);
                }
            });
        } catch (Exception e) {
            PCFMessageServiceImpl.checkConnectionException(connection, e);
            if (e instanceof PCFException) {
                int rc = ((PCFException) e).getReason();
                switch (rc) {
                    case 2428:
                        throw new IllegalMetadataStateException(40915, ErrorConstants.Message.MSG_MQ_UNKNOWN_SUBSCRIPTION, new Object[]{subscriptionName, connection});
                }
            }
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e
                    .getMessage()}, e);
        }
    }

    public void createQueue(final Connection connection, final String queueName, final QueueData.QueueType queueType) {
        try {
            this.commandProcessor.process("CREATE_QUEUE", new HashMap<String, Object>(3) {
                {
                    this.put("context.connection", connection);
                    this.put("context.object.name", queueName);
                    this.put("context.object.type", queueType);
                }
            });
        } catch (Exception e) {
            PCFMessageServiceImpl.checkConnectionException(connection, e);
            if (e instanceof PCFException) {
                int rc = ((PCFException) e).getReason();
                switch (rc) {
                    case 4001:
                        throw new IllegalMetadataStateException(40912, ErrorConstants.Message.MSG_MQ_OBJECT_ALREADY_EXISTS, new Object[]{queueName, connection});
                }
            }
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e
                    .getMessage()}, e);
        }
    }

    public void clearQueue(final Connection connection, final String queueName) {
        try {
            this.commandProcessor.process("CLEAR_QUEUE", new HashMap<String, Object>(2) {
                {
                    this.put("context.connection", connection);
                    this.put("context.object.name", queueName);
                }
            });
        } catch (Exception e) {
            PCFMessageServiceImpl.checkConnectionException(connection, e);
            if (e instanceof PCFException) {
                int rc = ((PCFException) e).getReason();
                switch (rc) {
                    case 2085:
                        throw new IllegalMetadataStateException(40914, ErrorConstants.Message.MSG_MQ_UNKNOWN_OBJECTNAME, new Object[]{queueName, connection});
                    case 2055:
                        throw new IllegalMetadataStateException(40916, ErrorConstants.Message.MSG_MQ_Q_NOT_EMPTY, new Object[]{queueName});
                }
            }
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e
                    .getMessage()}, e);
        }
    }

    public void deleteQueue(final Connection connection, final String queueName) {
        try {
            this.commandProcessor.process("DELETE_QUEUE", new HashMap<String, Object>(2) {
                {
                    this.put("context.connection", connection);
                    this.put("context.object.name", queueName);
                }
            });
        } catch (Exception e) {
            PCFMessageServiceImpl.checkConnectionException(connection, e);
            if (e instanceof PCFException) {
                int rc = ((PCFException) e).getReason();
                switch (rc) {
                    case 2085:
                        throw new IllegalMetadataStateException(40914, ErrorConstants.Message.MSG_MQ_UNKNOWN_OBJECTNAME, new Object[]{queueName, connection});
                }
            }
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e
                    .getMessage()}, e);
        }
    }

    public String queryTopicString(final Connection connection, final String topicObjectName) {
        try {
            return (String) this.commandProcessor.process("INQUIRE_TOPIC_STRING", new HashMap<String, Object>(2) {
                {
                    this.put("context.connection", connection);
                    this.put("context.object.name", topicObjectName);
                }
            });
        } catch (Exception e) {
            PCFMessageServiceImpl.checkConnectionException(connection, e);
            if (e instanceof PCFException) {
                int rc = ((PCFException) e).getReason();
                switch (rc) {
                    case 2085:
                        throw new IllegalMetadataStateException(40914, ErrorConstants.Message.MSG_MQ_UNKNOWN_OBJECTNAME, new Object[]{topicObjectName, connection});
                }
            }
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e
                    .getMessage()}, e);
        }
    }

    public String querySubscriptionName(Monitor monitor) {
        ensureMonitorIsTopic(monitor);
        return String.format("SYSTEM.MONITOR.MANAGED.DURABLESUB.%s", new Object[]{monitor.getId()});
    }

    public String querySubscriptionDestination(Connection connection) {
        return String.format("SYSTEM.MONITOR.%s", new Object[]{connection.getId()});
    }

    private List<MQObject> retrieveMQObjectNames(final Connection connection, final Monitor.Category category, final String objectName, final Enum<?> objectType) {
        String command = null;
        switch (category) {
            case Queue:
                command = "INQUIRE_Q_NAMES";
                break;
            case Topic:
                command = "INQUIRE_TOPIC_NAMES";
                break;
            case Listener:
                command = "INQUIRE_LISTENER_NAMES";
                break;
            case Channel:
                command = "INQUIRE_CHANNEL_NAMES";
                break;
            default:
                throw new IllegalArgumentException(String.format("%s is not supported of retrieving names!", new Object[]{category}));
        }
        try {
            return (List) this.commandProcessor.process(command, new HashMap<String, Object>(4) {
                {
                    this.put("context.connection", connection);
                    this.put("context.object.category", category);
                    this.put("context.object.name", objectName);
                    this.put("context.object.type", objectType);
                }
            });
        } catch (Exception e) {
            PCFMessageServiceImpl.checkConnectionException(connection, e);
            throw new MetadataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e
                    .getMessage()}, e);
        }
    }

    private void ensureMonitorIsTopic(Monitor monitor) {
        if (monitor.getCategory() != Monitor.Category.Topic)
            throw new IllegalMetadataStateException(40910, ErrorConstants.Message.MSG_ILLEGAL_MONITOR_CATEGORY, new Object[]{monitor
                    .getCategory(), Monitor.Category.Topic});
    }
}


