package com.jcca.web.common.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.redis.service.RedisService;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAttach;
import com.jcca.web.asset.service.AssetAttachService;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.collect.service.AssetLinkAssetService;
import com.jcca.web.common.constants.ThreeDConst;
import com.jcca.web.common.entity.Property;
import com.jcca.web.common.service.PropertyService;
import com.jcca.web.common.service.ThreeDService;
import com.jcca.web.common.service.bean.*;
import com.jcca.web.common.util.MQUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @description: 3D机房传输相关信息变动
 * @author: sophia
 * @create: 2023/12/04 14:07
 **/
@Service
@Slf4j
public class ThreeDServiceImpl implements ThreeDService {

    @Resource
    private RoomService roomService;
    @Resource
    private CabinetService cabinetService;
    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private RedisService redisService;
    @Resource
    private AssetAttachService assetAttachService;
    @Resource
    private PropertyService propertyService;
    @Resource
    private AssetLinkAssetService linkAssetService;

    @Value("${threeD.roomId1}")
    private String roomId1;
    @Value("${threeD.roomId2}")
    private String roomId2;


    /**
     * RabbitMQ连接
     */
    private static Channel channel;
    private HashMap<String, String> modelMap;

    @Override
    public ThreeDResult syncAssetByRoom() {
        ThreeDResult threeDResult = new ThreeDResult();
        Object o = redisService.get(ThreeDConst.KEY_STATUS);
        if (ObjectUtil.isNull(o) || !String.valueOf(o).equals("1")) {
            threeDResult.setStatus(false);
            threeDResult.setLog("3D机房程序未开启");
            return threeDResult;
        }
        List<ShelvesReq> shelvesReqs = roomService.pushAssetByRoom(roomId1, roomId2);
        if (ObjectUtil.isNull(shelvesReqs) || shelvesReqs.isEmpty()) {
            threeDResult.setLog("未检测到设备");
            return threeDResult;
        }

        for (ShelvesReq shelvesReq : shelvesReqs) {
            getModel(shelvesReq);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", "syncShelves");
        jsonObject.put("value", shelvesReqs);
        String s1 = jsonObject.toString();
        log.info("3D机房发送信息: " + s1);
        try {
            Channel channel = getChannel();
            channel.basicPublish("dcim_3d", "dcim_3d", MessageProperties.PERSISTENT_TEXT_PLAIN, s1.getBytes());
        } catch (Exception e) {
            threeDResult.setStatus(false);
            threeDResult.setLog("同步数据失败，" + e.getMessage());
            return threeDResult;
        }
        return new ThreeDResult();
    }


    @Override
    public ThreeDResult pushAssetAdd(Asset asset) {
        ThreeDResult threeDResult = new ThreeDResult();
        if (StringUtils.isEmpty(asset.getCabinetId())) {
            return threeDResult;
        }
        Object o = redisService.get(ThreeDConst.KEY_STATUS);
        if (ObjectUtil.isNull(o) || !String.valueOf(o).equals("1")) {
            threeDResult.setStatus(false);
            threeDResult.setLog("3D机房程序未开启");
            return threeDResult;
        }
        if (!isAsset(asset.getId())) {
            return threeDResult;
        }
        ShelvesReq shelvesReq = new ShelvesReq();
        shelvesReq.setId(asset.getId());
        shelvesReq.setName(asset.getName());
        if (!StringUtils.isEmpty(asset.getIp())) {
            shelvesReq.setIp(asset.getIp());
        }
        if (!StringUtils.isEmpty(asset.getIp2())) {
            shelvesReq.setIp2(asset.getIp2());
        }
        shelvesReq.setAreaId(asset.getRoomId());
        shelvesReq.setThreeModel(asset.getAssetImage());
        getModel(shelvesReq);
        //有机柜 放入机柜信息
        if (ObjectUtil.isNull(asset.getCabinetId())) {
            shelvesReq.setCabinetId(asset.getCabinetId());
            shelvesReq.setCabinetName(cabinetService.getById(asset.getCabinetId()).getName());
        }
        shelvesReq.setStartU(String.valueOf(asset.getStartPosition()));

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", "upShelves");
            jsonObject.put("value", shelvesReq);
            String s1 = jsonObject.toString();
            log.info("3D机房发送信息: " + s1);
            Channel channel = getChannel();
/*          channel.exchangeDeclare("dcimNorth","direct", true, false, false, null);
            channel.queueDeclare("dcimNorth", true, false, false, null);
            channel.queueBind("dcimNorth","dcimNorth",  "upShelves");*/
            channel.basicPublish("dcim_3d", "dcim_3d", MessageProperties.PERSISTENT_TEXT_PLAIN, s1.getBytes());
            return threeDResult;
        } catch (Exception e) {
            threeDResult.setStatus(false);
            threeDResult.setLog("上架异常，" + e.getMessage());
            return threeDResult;
        }

    }


    @Override
    public ThreeDResult pushAssetChange(Asset asset) {
        ThreeDResult threeDResult = new ThreeDResult();
        if (StringUtils.isEmpty(asset.getCabinetId())) {
            return threeDResult;
        }
        Object o = redisService.get(ThreeDConst.KEY_STATUS);
        if (ObjectUtil.isNull(o) || !String.valueOf(o).equals("1")) {
            threeDResult.setStatus(false);
            threeDResult.setLog("3D机房程序未开启");
            return threeDResult;
        }
        if (!isAsset(asset.getId())) {
            return threeDResult;
        }
        ShelvesReq shelvesReq = new ShelvesReq();
        shelvesReq.setId(asset.getId());
        if (!StringUtils.isEmpty(asset.getIp())) {
            shelvesReq.setIp(asset.getIp());
        }
        if (!StringUtils.isEmpty(asset.getIp2())) {
            shelvesReq.setIp2(asset.getIp2());
        }
        shelvesReq.setName(asset.getName());
        shelvesReq.setThreeModel(asset.getAssetImage());
        getModel(shelvesReq);
        shelvesReq.setAreaId(asset.getRoomId());
        shelvesReq.setCabinetId(asset.getCabinetId());
        shelvesReq.setCabinetName(cabinetService.getById(asset.getCabinetId()).getName());
        shelvesReq.setStartU(String.valueOf(asset.getStartPosition()));
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", "updateShelves");
            jsonObject.put("value", shelvesReq);
            String s1 = jsonObject.toString();
            log.info("3D机房发送信息: " + s1);
            Channel channel = getChannel();
            channel.basicPublish("dcim_3d", "dcim_3d", MessageProperties.PERSISTENT_TEXT_PLAIN, s1.getBytes());
            return threeDResult;
        } catch (Exception e) {
            threeDResult.setStatus(false);
            threeDResult.setLog("迁移失败，" + e.getMessage());
            return threeDResult;
        }
    }

    @Override
    public ThreeDResult pushAssetRemove(String id) {
        ThreeDResult threeDResult = new ThreeDResult();
        Object o = redisService.get(ThreeDConst.KEY_STATUS);
        if (ObjectUtil.isNull(o) || !String.valueOf(o).equals("1")) {
            threeDResult.setStatus(false);
            threeDResult.setLog("3D机房程序未开启");
            return threeDResult;
        }
        if (!isAsset(id)) {
            return threeDResult;
        }
        try {
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("assetId", id);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", "offShelves");
            jsonObject.put("value", jsonObject1);
            String s1 = jsonObject.toString();
            log.info("3D机房发送信息: " + s1);
            Channel channel = getChannel();
            channel.basicPublish("dcim_3d", "dcim_3d", MessageProperties.PERSISTENT_TEXT_PLAIN, s1.getBytes());
            return threeDResult;
        } catch (Exception e) {
            threeDResult.setStatus(false);
            threeDResult.setLog("下架异常，" + e.getMessage());
            return threeDResult;
        }
    }

    @Override
    public ThreeDResult pushAlarm() {
        ThreeDResult threeDResult = new ThreeDResult();
        try {
            List<ThreeDAlarmReq> threeDAlarms = alarmInfoService.getThreeDAlarm(roomId1, roomId2);
            if (ObjectUtil.isNull(threeDAlarms) || threeDAlarms.isEmpty()) {
                threeDAlarms = new ArrayList<>();
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", "pushAlarm");
            jsonObject.put("value", threeDAlarms);
            String s1 = jsonObject.toString();
            log.info("3D机房发送信息: " + s1);
            Channel channel = getChannel();
            channel.basicPublish("dcim_3d", "dcim_3d", MessageProperties.PERSISTENT_TEXT_PLAIN, s1.getBytes());
            return threeDResult;
        } catch (Exception e) {
            threeDResult.setStatus(false);
            threeDResult.setLog("推送告警异常，" + e.getMessage());
            return threeDResult;
        }
    }

    @Override
    public ThreeDResult cancelAlarm(AlarmInfo alarmInfo) {
        ThreeDResult threeDResult = new ThreeDResult();
        Object o = redisService.get(ThreeDConst.KEY_STATUS);
        if (ObjectUtil.isNull(o) || !String.valueOf(o).equals("1")) {
            threeDResult.setStatus(false);
            threeDResult.setLog("3D机房程序未开启");
            return threeDResult;
        }
        if (!isAsset(alarmInfo.getAssetId())) {
            return threeDResult;
        }
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("id", alarmInfo.getId());
        jsonObject1.put("assetId", alarmInfo.getAssetId());
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", "cancelAlarm");
            jsonObject.put("value", jsonObject1);
            String s1 = jsonObject.toString();
            log.info("3D机房发送信息: " + s1);
            Channel channel = getChannel();
            channel.basicPublish("dcim_3d", "dcim_3d", MessageProperties.PERSISTENT_TEXT_PLAIN, s1.getBytes());
            return threeDResult;
        } catch (Exception e) {
            threeDResult.setStatus(false);
            threeDResult.setLog("推送告警异常，" + e.getMessage());
            return threeDResult;
        }
    }

    @Override
    public ThreeDResult pushProperty() {
        ThreeDResult threeDResult = new ThreeDResult();
        Object o = redisService.get(ThreeDConst.KEY_STATUS);
        if (ObjectUtil.isNull(o) || !String.valueOf(o).equals("1")) {
            threeDResult.setStatus(false);
            threeDResult.setLog("3D机房程序未开启");
            return threeDResult;
        }
        List<Property> properties = propertyService.selectThreeDProperty();
        if (ObjectUtil.isNull(properties) || properties.isEmpty()) {
            return threeDResult;
        }
        ArrayList<ThreeDPropertyReq> threeDPropertyReqs = new ArrayList<>();
        for (Property property : properties) {
            ThreeDPropertyReq threeDPropertyReq = new ThreeDPropertyReq();
            threeDPropertyReq.setName(property.getName());
            threeDPropertyReq.setParentID(property.getParentID());
            threeDPropertyReq.setPropertyId(property.getPropertyId());
            threeDPropertyReq.setDesc(property.getDataDesc());
            threeDPropertyReq.setStatus(property.getStatus());
            threeDPropertyReq.setUnit(property.getUnit());
            threeDPropertyReq.setValue(property.getPropertyValue());
            threeDPropertyReqs.add(threeDPropertyReq);
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", "pushProprty");
            jsonObject.put("value", threeDPropertyReqs);
            String s1 = jsonObject.toString();
            log.info("3D机房发送信息: " + s1);
            Channel channel = getChannel();
            channel.basicPublish("dcim_3d", "dcim_3d", MessageProperties.PERSISTENT_TEXT_PLAIN, s1.getBytes());
            return threeDResult;
        } catch (Exception e) {
            threeDResult.setStatus(false);
            threeDResult.setLog("推送监测值异常，" + e.getMessage());
            return threeDResult;
        }
    }


    @Override
    public ThreeDResult pushLink() {
        ThreeDResult threeDResult = new ThreeDResult();
        Object o = redisService.get(ThreeDConst.KEY_STATUS);
        if (ObjectUtil.isNull(o) || !String.valueOf(o).equals("1")) {
            threeDResult.setStatus(false);
            threeDResult.setLog("3D机房程序未开启");
            return threeDResult;
        }
        List<ThreeDLinkReq> threeDLinks = linkAssetService.getThreeDLink(roomId1, roomId2);
        if (ObjectUtil.isNull(threeDLinks) || threeDLinks.isEmpty()) {
            return threeDResult;
        }
        ArrayList<ThreeDLinkReq> threeDLinkReqs = new ArrayList<>();
        for (ThreeDLinkReq threeDLink : threeDLinks) {

            if (ObjectUtil.isNull(threeDLink.getPortAId()) || ObjectUtil.isNull(threeDLink.getPortBId()) || !threeDLink.getPortBId().contains("G")) {
                continue;
            }

            if (threeDLink.getPortAId().contains("GigabitEthernet")) {
                threeDLink.setPortAId(threeDLink.getPortAId().replace("gabitEthernet", ""));
            }

            if (threeDLink.getPortBId().contains("GigabitEthernet")) {
                threeDLink.setPortBId(threeDLink.getPortBId().replace("gabitEthernet", ""));
            }

           /* Asset asset1 = assetService.getById(threeDLink.getAssetAId());
            //查看设备状态
            if (ObjectUtil.isNotNull(asset1.getStatus()) && asset1.getStatus() == (byte) 0) {
                threeDLink.setLineState(1);
                threeDLink.setColor("F5222D");
                continue;
            }
            //查看端口状态
            Byte statusA = cacheDataService.queryInterfaceStatus(threeDLink.getAssetAId(), threeDLink.getPortAId()).getCode();
            if (statusA == (byte) 2) {
                threeDLink.setLineState(1);
                threeDLink.setColor("F5222D");
                continue;
            }
            Asset asset2 = assetService.getById(threeDLink.getAssetAId());
            //查看设备状态
            if (ObjectUtil.isNotNull(asset2.getStatus()) && asset2.getStatus() == (byte) 0) {
                threeDLink.setLineState(1);
                threeDLink.setColor("F5222D");
                continue;
            }
            //查看端口状态
           Byte statusB = cacheDataService.queryInterfaceStatus(threeDLink.getAssetBId(), threeDLink.getPortBId()).getCode();
            if (statusB == (byte) 2) {
                threeDLink.setLineState(1);
                threeDLink.setColor("F5222D");
                continue;
            }*/
            threeDLinkReqs.add(threeDLink);
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", "pushLink");
            jsonObject.put("value", threeDLinkReqs);
            String s1 = jsonObject.toString();
            log.info("3D机房发送信息: " + s1);
            Channel channel = getChannel();
            channel.basicPublish("dcim_3d", "dcim_3d", MessageProperties.PERSISTENT_TEXT_PLAIN, s1.getBytes());
            return threeDResult;
        } catch (Exception e) {
            threeDResult.setStatus(false);
            threeDResult.setLog("推送链路异常，" + e.getMessage());
            return threeDResult;
        }
    }


    /**
     * 是否是3D机房内的设备
     */
    private boolean isAsset(String id) {
        //查询
        QueryWrapper<AssetAttach> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ASSET_ID", id);
        queryWrapper.and(wrapper -> {
            wrapper.eq("ROOM_ID", roomId1)
                    .or()
                    .eq("ROOM_ID", roomId2);
        });
        List<AssetAttach> list = assetAttachService.list(queryWrapper);
        return (ObjectUtil.isNotNull(list) && !list.isEmpty());
    }


    /**
     * 型号转模型
     */
    private void getModel(ShelvesReq req) {
        if (ObjectUtil.isNull(modelMap)) {
            HashMap<String, String> map = new HashMap<>();
            String filePath = "/threeDModel.txt";
            BufferedReader reader;

            try {
                try {
                    reader = new BufferedReader(new FileReader(filePath));
                } catch (Exception e) {
                    ClassPathResource resource = new ClassPathResource("templates/system/export/threeDModel.txt");
                    InputStream inputStream = resource.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                }
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] keyValue = line.split("&&");
                    if (keyValue.length == 2) {
                        map.put(keyValue[0], keyValue[1]);
                    }
                }
                modelMap = map;
                reader.close();

            } catch (Exception e) {
                req.setThreeModel("225");
            }
        }

        String image = req.getThreeModel();
        if (modelMap.containsKey(image)) {
            String[] split = modelMap.get(image).split("&&");
            req.setThreeModel(split[0]);
        } else {
            req.setThreeModel("225");
        }
    }

    /**
     * 获取队列连接
     */
    private static Channel getChannel() {
        if (ObjectUtil.isNull(channel)) {
            channel = MQUtil.getChannel();
        }
        return channel;
    }
}