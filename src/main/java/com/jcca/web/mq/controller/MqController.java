package com.jcca.web.mq.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.base.Strings;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.mq.controller.bean.MqWarningVo;
import com.jcca.web.mq.entity.*;
import com.jcca.web.mq.service.CollectMqService;
import com.jcca.web.mq.service.MqConnectionService;
import com.jcca.web.mq.service.MqGroupService;
import com.jcca.web.mq.service.MqMonitorService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author sophia
 * @date 2026/1/23 10:07
 */
@RestController
@RequestMapping("/api/mq")
@Slf4j
@ApiOperation(value = "新建业务组")
public class MqController {
    @Resource
    private MqMonitorService mqMonitorService;
    @Resource
    private MqConnectionService mqConnectionService;
    @Resource
    private MqGroupService mqGroupService;
    @Resource
    private CollectMqService collectMqService;


    @PostMapping("/createConnection")
    @ApiOperation(value = "创建连接")
    @RequiresPermissions({"api:mqMonitor:createConnection"})
    @ActionLog(name = "创建连接", title = "中间件", key = LogTypeConstant.ADD)
    public ResultVo createConnection(@Validated @RequestBody MqConnection connection) {

        if (Strings.isNullOrEmpty(connection.getConnectHost())) {
            return ResultVoUtil.warning("IP/主机不可为空");
        }
        if (connection.getConnectPort() < 1 || connection.getConnectPort() > 65535) {
            return ResultVoUtil.warning("端口范围为:[1-65535]!");
        }

        if (mqConnectionService.existsByHostAndPort(
                connection.getConnectHost(),
                connection.getConnectPort())) {
            return ResultVoUtil.warning("该主机和端口的 MQ 连接已存在");
        }
        //给予连接通道默认值
        if (Strings.isNullOrEmpty(connection.getChannelName())) {
            connection.setChannelName("NITSM.MON.SVRCONN");
        }
        MQQueueManager qm = null;
        PCFMessageAgent pcf = null;
        try {
            Hashtable<String, Object> props = new Hashtable<>();
            props.put(MQConstants.HOST_NAME_PROPERTY, connection.getConnectHost());
            props.put(MQConstants.PORT_PROPERTY, connection.getConnectPort());
            props.put(MQConstants.CHANNEL_PROPERTY, connection.getChannelName());
            props.put(MQConstants.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);
            props.put(MQConstants.USER_ID_PROPERTY, connection.getUserId());
            qm = new MQQueueManager(connection.getConnectName(), props);
            pcf = new PCFMessageAgent(qm);

            PCFMessage ping = new PCFMessage(MQConstants.MQCMD_PING_Q_MGR);
            pcf.send(ping);
        } catch (MQException e) {
            // 常见 MQ 失败原因
            switch (e.reasonCode) {
                case MQConstants.MQRC_HOST_NOT_AVAILABLE:
                    return ResultVoUtil.warning("MQ主机不可达（IP/端口错误或防火墙阻断）");
                case MQConstants.MQRC_CHANNEL_NOT_AVAILABLE:
                    return ResultVoUtil.warning("通道不存在或未启动");
                case MQConstants.MQRC_NOT_AUTHORIZED:
                    return ResultVoUtil.warning("MQ用户无连接权限");
                case MQConstants.MQRC_Q_MGR_NAME_ERROR:
                    return ResultVoUtil.warning("队列管理器名称错误");
                default:
                    return ResultVoUtil.warning("MQ连接失败，原因码=" + e.reasonCode + "，错误码=" + e.completionCode);
            }
        } catch (Exception e) {
            return ResultVoUtil.warning("MQ连接异常：" + e.getMessage());
        } finally {
            try {
                if (pcf != null) {
                    pcf.disconnect();
                }
            } catch (Exception ignored) {
            }

            try {
                if (qm != null && qm.isConnected()) {
                    qm.disconnect();
                }
            } catch (Exception ignored) {
            }
        }
        String id = MyIdUtil.getId();
        connection.setId(id);
        mqConnectionService.save(connection);
        return ResultVoUtil.success("队列新建成功", id);
    }


    @GetMapping("/getConnection/{id}")
    @ApiOperation(value = "获取队列管理器连接详情")
    public ResultVo getConnection(@PathVariable String id) {
        return ResultVoUtil.success(mqConnectionService.getById(id));
    }


    @GetMapping("/removeConnection/{id}")
    @ApiOperation(value = "删除连接")
    @RequiresPermissions({"api:mqMonitor:removeConnection"})
    @ActionLog(name = "删除连接", title = "中间件", key = LogTypeConstant.REMOVEE)
    public ResultVo removeConnection(@PathVariable String id) {
        List<MqGroup> mqGroups = mqGroupService.selectByConnectId(id);
        if (ObjectUtil.isNotNull(mqGroups) && !mqGroups.isEmpty()) {
            return ResultVoUtil.warning("请先删除连接中的业务组");
        }
        mqConnectionService.removeById(id);
        return ResultVoUtil.success("删除成功");
    }


    @PostMapping("/createGroup")
    @ApiOperation(value = "新建业务组")
    @ActionLog(name = "新建业务组", title = "中间件", key = LogTypeConstant.ADD)
    public ResultVo createGroup(@RequestBody MqGroup mqGroup) {
        if (Strings.isNullOrEmpty(mqGroup.getName())) {
            return ResultVoUtil.warning("业务名称不可为空");
        }
        List<MqGroup> mqGroups = mqGroupService.selectByName(mqGroup.getName(), mqGroup.getConnectId());
        if (ObjectUtil.isNotNull(mqGroups) && !mqGroups.isEmpty()) {
            return ResultVoUtil.warning("业务名称重复,请更换");
        }
        mqGroup.setId(MyIdUtil.getId());
        mqGroupService.save(mqGroup);
        return ResultVoUtil.success();
    }

    @GetMapping("/getGroups/{connectId}")
    @ApiOperation(value = "获取队列管理器下的所有业务组")
    public ResultVo getGroups(@PathVariable("connectId") String connectId) {
        List<MqGroup> mqGroups = mqGroupService.selectByConnectId(connectId);
        for (MqGroup mqGroup : mqGroups) {
            List<MqMonitor> monitorByGroupId = mqMonitorService.getMonitorByGroupId(mqGroup.getId());
            // category -> monitors
            Map<String, List<MqMonitor>> monitorMap =
                    monitorByGroupId.stream()
                            .collect(Collectors.groupingBy(MqMonitor::getCategory));

            mqGroup.setMonitors(monitorMap);
        }
        return ResultVoUtil.success(mqGroups);
    }


    @GetMapping("/getMonitors/{connectId}")
    @ApiOperation(value = "获取队列管理器下可选的数据")
    public ResultVo getMonitors(@PathVariable("connectId") String connectId) {
        QueryWrapper<CollectMq> qw = new QueryWrapper<>();
        qw.eq("CONNECTION_ID", connectId).select("CATEGORY", "NAME");
        ;
        List<CollectMq> collectMqs = collectMqService.list(qw);
        // category -> collect list
        Map<String, List<CollectMq>> monitorMap =
                collectMqs.stream()
                        .collect(Collectors.groupingBy(CollectMq::getCategory));
        return ResultVoUtil.success(monitorMap);
    }

    @PostMapping("/updateGroup")
    @ApiOperation(value = "修改业务组")
    @ActionLog(name = "修改业务组", title = "中间件", key = LogTypeConstant.ADD)
    public ResultVo updateGroup(@RequestBody MqGroup mqGroup) {
        if (Strings.isNullOrEmpty(mqGroup.getName())) {
            return ResultVoUtil.warning("业务名称不可为空");
        }
        List<MqGroup> mqGroups = mqGroupService.selectByName(mqGroup.getName(), mqGroup.getConnectId());
        if (ObjectUtil.isNotNull(mqGroups) && !mqGroups.isEmpty() && !mqGroups.get(0).getId().equals(mqGroup.getId())) {
            return ResultVoUtil.warning("业务名称重复,请更换");
        }
        mqGroupService.updateById(mqGroup);
        return ResultVoUtil.success();
    }

    @PostMapping("/removeGroup/{id}")
    @ApiOperation(value = "移除业务组")
    @ActionLog(name = "移除业务组", title = "中间件", key = LogTypeConstant.REMOVEE)
    public ResultVo removeGroup(@PathVariable("id") String groupId) {
        QueryWrapper<MqMonitor> wrapper = new QueryWrapper<>();
        wrapper.eq("GROUP_ID", groupId);
        List<MqMonitor> list = mqMonitorService.list(wrapper);
        if (ObjectUtil.isNotNull(list) && !list.isEmpty()) {
            return ResultVoUtil.warning("请先删除业务组内监视器");
        }
        mqGroupService.removeById(groupId);
        return ResultVoUtil.success("删除成功");
    }


    @PostMapping("/createMonitor")
    @ApiOperation(value = "创建队列或通道")
    @ActionLog(name = "创建队列或通道", title = "中间件", key = LogTypeConstant.ADD)
    public ResultVo createMonitor(@RequestBody MqMonitor monitor) {
        monitor.setId(MyIdUtil.getId());
        mqMonitorService.save(monitor);
        return ResultVoUtil.success("保存成功");
    }


/*
    @GetMapping("/getMonitor/{groupId}")
    @ApiOperation(value = "获取业务组下所有队列和通道")
    public ResultVo getMonitor(@PathVariable("groupId") String groupId) {
        QueryWrapper<MqMonitor> wrapper = new QueryWrapper<>();
        wrapper.eq("GROUP_ID", groupId);
        return ResultVoUtil.success(mqMonitorService.list(wrapper));
    }
*/


    @GetMapping("/removeMonitor/{id}")
    @ApiOperation(value = "删除监控项")
    @RequiresPermissions({"api:mqMonitor:removeMonitor"})
    @ActionLog(name = "删除监控项", title = "中间件", key = LogTypeConstant.REMOVEE)
    public ResultVo removeMonitor(@PathVariable String id) {
        mqMonitorService.removeById(id);
        return ResultVoUtil.success();
    }


    @PostMapping("/setWarn")
    @ApiOperation(value = "设置队列深度阈值")
    @ActionLog(name = "设置队列尝试阈值", title = "中间件", key = LogTypeConstant.MODIFY)
    public ResultVo setWarn(@RequestBody MqWarningVo warningVo) {
        MqMonitor mqMonitor = new MqMonitor();
        mqMonitor.setId(warningVo.getId());
        if (warningVo.getCurrentQDepth() == 0) {// 删除阈值时 前端传值0
            mqMonitor.setRuleValue(999999999);
        } else {
            mqMonitor.setRuleValue(warningVo.getCurrentQDepth());
        }
        mqMonitorService.updateById(mqMonitor);
        return ResultVoUtil.success("阈值配置成功");
    }

    @GetMapping("/getWarn/{id}")
    @ApiOperation(value = "获取队列深度阈值")
    public ResultVo getWarn(@PathVariable("id") String id) {
        MqMonitor mqMonitor = mqMonitorService.getById(id);
        return ResultVoUtil.success("", mqMonitor.getRuleValue());
    }


}
