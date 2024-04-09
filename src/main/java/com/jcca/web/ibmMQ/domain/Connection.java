package com.jcca.web.ibmMQ.domain;

import com.jcca.web.ibmMQ.entity.IBMConnection;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class Connection implements Cloneable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String channelName;

    @NotEmpty(message = "请输入IP地址")
    @Pattern(regexp = "^([1-9]|[1-9]\\d|1\\d{2}|2[0-1]\\d|22[0-3])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$", message = "IP地址格式不正确")
    private String host;

    @Min(value = 1, message = "端口号请输入1-65535位")
    @Max(value = 65535, message = "端口号请输入1-65535位")
    private int port;

    private String userId;
    private String description;


    public Connection clone() {
        try {
            return (Connection) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }


    public String toString() {
        return getClass().getSimpleName() + " [" + "id=" + this.id + "," + "name=" +
                this.name + "," + "channelName=" + this.channelName + "," + "host=" +
                this.host + "," + "port=" + this.port + "," + "userId=" + this.userId +
                "," + "description=" + this.description + "]";
    }


    public static IBMConnection getIBMConnection(Connection connection) {
        IBMConnection ibmConnection = new IBMConnection();
        ibmConnection.setConnectName(connection.getName());
        ibmConnection.setChannelName(connection.getChannelName());
        ibmConnection.setConnectHost(connection.getHost());
        ibmConnection.setConnectPort(connection.getPort());
        ibmConnection.setDescription(connection.getDescription());
        ibmConnection.setUserId(connection.getUserId());
        return ibmConnection;
    }

    public static Connection getConnection(IBMConnection ibmConnection) {
        Connection Connection = new Connection();
        Connection.setId(ibmConnection.getId());
        Connection.setUserId(ibmConnection.getUserId());
        Connection.setName(ibmConnection.getConnectName());
        Connection.setChannelName(ibmConnection.getChannelName());
        Connection.setHost(ibmConnection.getConnectHost());
        Connection.setPort(ibmConnection.getConnectPort());
        Connection.setDescription(ibmConnection.getDescription());
        return Connection;
    }
}

