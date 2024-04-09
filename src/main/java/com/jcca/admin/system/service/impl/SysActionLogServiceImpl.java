package com.jcca.admin.system.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.jcca.admin.system.dao.SysActionLogMapper;
import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.service.SysActionLogService;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.entity.AlarmEventRel;
import com.jcca.web.event.service.AlarmEventRelService;
import com.jcca.web.event.service.AlarmEventService;
import com.jcca.web.statistics.entity.*;
import com.jcca.web.statistics.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author hanwone
 * @date 2020-04-06 12:11
 **/
@Service
@Slf4j
public class SysActionLogServiceImpl extends ServiceImpl<SysActionLogMapper, SysActionLog> implements SysActionLogService {

    @Resource
    private SysActionLogMapper actionLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeAll() {
        return actionLogMapper.deleteAll();
    }

    /**
     * 采集数据转存为文件
     *
     * @param table      表名
     * @param dateFormat 日期
     * @return 查出的数据
     */
    @Override
    public List<Map<String, Object>> getData(String table, Date dateFormat) {
        return actionLogMapper.getData(table, dateFormat);
    }

    /**
     * 删除指定天数之前的数据
     *
     * @param table      表名
     * @param dateFormat 日期
     */
    @Override
    public void removeData(String table, Date dateFormat) {
        actionLogMapper.removeData(table, dateFormat);
    }

    /**
     * 删除最旧的5000条数据
     *
     * @param tables      要删除数据的表
     * @param amount      要保留的数据天数
     * @param recoverPath 存储数据路径
     */
    @Override
    public void remove5000(String[] tables, int amount, String recoverPath) {
        Executor executor = (Executor) SpringContextUtil.getBean("transferDataExecutor");
        if (amount > 0) amount = -amount;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, amount);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        DateTime time = DateUtil.parse(year + "-" + month + "-" + day, "yyyy-MM-dd");

        File f = new File(recoverPath + "/transfer-data");
        if (!f.exists()) {
            f.mkdirs();
        }
        String fileNa = DateUtil.format(new Date(), DatePattern.PURE_DATE_FORMAT);
        for (String table : tables) {
            executor.execute(() -> {
                Gson gson = new Gson();
                String path;
                BufferedWriter writer;
                path = recoverPath + "/transfer-data/" + table + "-" + fileNa + ".txt";
                List<Map<String, Object>> mapList;
                // 每次查询5000条数据
                while (!CollectionUtils.isEmpty((mapList = this.getData(table, time)))) {
                    try {
                        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true), StandardCharsets.UTF_8));
                        for (Map<String, Object> map : mapList) {
                            writer.write(gson.toJson(map));
                            writer.newLine();
                        }

                        writer.flush();
                        writer.close();

                        // 每次删除5000条数据
                        this.removeData(table, time);
                        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA, table, "清理" + mapList.size() + "条表数据");
                        TimeUnit.SECONDS.sleep(10);
                    } catch (Exception e) {
                        AppLogUtils.buildLogError(LogFunctionEnum.CRON_DATA, "清除[" + table + "]表数据异常", e);
                    }
                }
            });
        }
    }

    /**
     * 获取最新一条日志数据
     *
     * @return 最新一条数据
     */
    @Override
    public SysActionLog getLatestOne() {
        return actionLogMapper.getLatestOne();
    }

    @Override
    public void recoverData(String tname, BufferedReader reader) throws IOException {
        List<AlarmEvent> alarmEventList = new ArrayList<>(1024);
        List<AlarmEventRel> alarmEventRelList = new ArrayList<>(1024);
        List<AlarmInfo> alarmInfoList = new ArrayList<>(1024);
        List<HourCpu> hourCpuList = new ArrayList<>(1024);
        List<HourInterfaces> hourInterfacesList = new ArrayList<>(1024);
        List<HourInterfacesItem> hourInterfacesItemList = new ArrayList<>(1024);
        List<HourMemory> hourMemoryList = new ArrayList<>(1024);
        List<HourTemp> hourTempList = new ArrayList<>(1024);
        List<SysActionLog> actionLogList = new ArrayList<>(1024);
        String s = "";
        if ("ALARM_EVENT".equals(tname)) {
            AlarmEventService bean = SpringContextUtil.getBean(AlarmEventService.class);
            while ((s = reader.readLine()) != null) {
                AlarmEvent dto = JSONUtil.toBean(s, AlarmEvent.class);
                if (Objects.isNull(dto.getCreateTime())) {
                    dto.setCreateTime(new Date());
                    dto.setUpdateTime(new Date());
                }
                alarmEventList.add(dto);
                if (alarmEventList.size() == 1000) {
                    bean.saveOrUpdateBatch(alarmEventList);
                    alarmEventList.clear();
                }
            }
            if (CollectionUtil.isNotEmpty(alarmEventList)) {
                bean.saveOrUpdateBatch(alarmEventList);
                alarmEventList.clear();
            }
        }

        if ("ALARM_EVENT_REL".equals(tname)) {
            AlarmEventRelService bean = SpringContextUtil.getBean(AlarmEventRelService.class);
            while ((s = reader.readLine()) != null) {
                AlarmEventRel dto = JSONUtil.toBean(s, AlarmEventRel.class);
                if (Objects.isNull(dto.getCreateTime())) {
                    dto.setCreateTime(new Date());
                }
                alarmEventRelList.add(dto);
                if (alarmEventRelList.size() == 1000) {
                    bean.saveOrUpdateBatch(alarmEventRelList);
                    alarmEventRelList.clear();
                }
            }
            if (CollectionUtil.isNotEmpty(alarmEventRelList)) {
                bean.saveOrUpdateBatch(alarmEventRelList);
                alarmEventRelList.clear();
            }
        }

        if ("ALARM_INFO".equals(tname)) {
            AlarmInfoService bean = SpringContextUtil.getBean(AlarmInfoService.class);
            while ((s = reader.readLine()) != null) {
                AlarmInfo dto = JSONUtil.toBean(s, AlarmInfo.class);
                alarmInfoList.add(dto);
                if (alarmInfoList.size() == 1000) {
                    bean.saveOrUpdateBatch(alarmInfoList);
                    alarmInfoList.clear();
                }
            }
            if (CollectionUtil.isNotEmpty(alarmInfoList)) {
                bean.saveOrUpdateBatch(alarmInfoList);
                alarmInfoList.clear();
            }
        }

        if ("HOUR_CPU".equals(tname)) {
            HourCpuService bean = SpringContextUtil.getBean(HourCpuService.class);
            while ((s = reader.readLine()) != null) {
                HourCpu dto = JSONUtil.toBean(s, HourCpu.class);
                hourCpuList.add(dto);
                if (hourCpuList.size() == 1000) {
                    bean.saveOrUpdateBatch(hourCpuList);
                    hourCpuList.clear();
                }
            }
            if (CollectionUtil.isNotEmpty(hourCpuList)) {
                bean.saveOrUpdateBatch(hourCpuList);
                hourCpuList.clear();
            }
        }

        if ("HOUR_INTERFACES".equals(tname)) {
            HourInterfacesService bean = SpringContextUtil.getBean(HourInterfacesService.class);
            while ((s = reader.readLine()) != null) {
                HourInterfaces dto = JSONUtil.toBean(s, HourInterfaces.class);
                hourInterfacesList.add(dto);
                if (hourInterfacesList.size() == 1000) {
                    bean.saveOrUpdateBatch(hourInterfacesList);
                    hourInterfacesList.clear();
                }
            }
            if (CollectionUtil.isNotEmpty(hourInterfacesList)) {
                bean.saveOrUpdateBatch(hourInterfacesList);
                hourInterfacesList.clear();
            }
        }

        if ("HOUR_INTERFACES_ITEM".equals(tname)) {
            HourInterfacesItemService bean = SpringContextUtil.getBean(HourInterfacesItemService.class);
            while ((s = reader.readLine()) != null) {
                HourInterfacesItem dto = JSONUtil.toBean(s, HourInterfacesItem.class);
                hourInterfacesItemList.add(dto);
                if (hourInterfacesItemList.size() == 1000) {
                    bean.saveOrUpdateBatch(hourInterfacesItemList);
                    hourInterfacesItemList.clear();
                }
            }
            if (CollectionUtil.isNotEmpty(hourInterfacesItemList)) {
                bean.saveOrUpdateBatch(hourInterfacesItemList);
                hourInterfacesItemList.clear();
            }
        }

        if ("HOUR_MEMORY".equals(tname)) {
            HourMemoryService bean = SpringContextUtil.getBean(HourMemoryService.class);
            while ((s = reader.readLine()) != null) {
                HourMemory dto = JSONUtil.toBean(s, HourMemory.class);
                hourMemoryList.add(dto);
                if (hourMemoryList.size() == 1000) {
                    bean.saveOrUpdateBatch(hourMemoryList);
                    hourMemoryList.clear();
                }
            }
            if (CollectionUtil.isNotEmpty(hourMemoryList)) {
                bean.saveOrUpdateBatch(hourMemoryList);
                hourMemoryList.clear();
            }
        }

        if ("HOUR_TEMP".equals(tname)) {
            HourTempService bean = SpringContextUtil.getBean(HourTempService.class);
            while ((s = reader.readLine()) != null) {
                HourTemp dto = JSONUtil.toBean(s, HourTemp.class);
                hourTempList.add(dto);
                if (hourTempList.size() == 1000) {
                    bean.saveOrUpdateBatch(hourTempList);
                    hourTempList.clear();
                }
            }
            if (CollectionUtil.isNotEmpty(hourTempList)) {
                bean.saveOrUpdateBatch(hourTempList);
                hourTempList.clear();
            }
        }

        if ("SYS_ACTION_LOG".equals(tname)) {
            SysActionLogService bean = SpringContextUtil.getBean(SysActionLogService.class);
            while ((s = reader.readLine()) != null) {
                SysActionLog dto = JSONUtil.toBean(s, SysActionLog.class);
                actionLogList.add(dto);
                if (actionLogList.size() == 1000) {
                    bean.saveOrUpdateBatch(actionLogList);
                    actionLogList.clear();
                }
            }
            if (CollectionUtil.isNotEmpty(actionLogList)) {
                bean.saveOrUpdateBatch(actionLogList);
                actionLogList.clear();
            }
        }
    }
}
