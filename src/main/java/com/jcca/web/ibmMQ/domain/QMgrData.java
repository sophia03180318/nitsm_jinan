package com.jcca.web.ibmMQ.domain;


import com.jcca.web.ibmMQ.annotation.PCFParam;
import lombok.Data;

@Data
public class QMgrData extends StatisticalData {
    private static final long serialVersionUID = 1L;

    public QMgrData(Monitor monitor) {
        super(monitor);
    }

    @PCFParam(2015)
    private String queueManagerName;

    @PCFParam(value = 1149, option = 11)
    private QMgrStatus queueManagerStatus;

    @PCFParam(1230)
    private int connectionCount;

    @PCFParam(31)
    private int commandLevel;

    @PCFParam(32)
    private Platform platform;

    @PCFParam(2120)
    private String version;

    public enum QMgrStatus {
        Starting(1),

        Running(2),

        Quiescing(3),

        Unavailable(-1);

        private final int value;

        QMgrStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

    }

    public enum Platform {
        Unix(3), IBM_i(4), HP_NonStop(13), HP_OpenVMS(12), Windows(11), ZOS(1);
        private final int value;

        Platform(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

    }

    public boolean isZOS() {
        return (Platform.ZOS == this.platform);
    }

}


