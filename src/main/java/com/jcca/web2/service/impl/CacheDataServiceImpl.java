package com.jcca.web2.service.impl;

import cn.hutool.json.JSONObject;
import com.jcca.common.redis.service.RedisService;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.enums.InterfaceStatus;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web2.service.CacheDataService;
import com.jcca.web2.vo.AssetStatusDetailVo;
import com.jcca.web2.vo.AssetStatusItmVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 缓存内数据查询
 *
 * @description: 缓存内数据查询
 * @author: Lvyp
 * @create: 2023/12/21 13:58
 */
@Service
public class CacheDataServiceImpl implements CacheDataService {

    @Resource
    private RedisService redisServ;
    @Resource
    private AssetService assetServ;
    @Resource
    private CollectInterfacesService interfacesServ;


    @Override
    public List<AssetStatusItmVo> queryAssetTargetStatus(String assetId) {
        List<AssetStatusItmVo> voList = new ArrayList<>();
        List<String> filter = Arrays.asList("event:event_net", "event:event_fan", "event:event_power", "event:event_temp", "event:event_port", "event:event_process");
        Asset asset = assetServ.getById(assetId);
        if (Objects.isNull(asset)) {
            return voList;
        }
        Map<String, Object> hashMap = redisServ.getHashMap(String.format("%s:%s:statusEvent", asset.getIp()
                , asset.getId()));

        Map<String, AssetStatusItmVo> groupMap = new HashMap<>(10);
        Set<String> keySet = hashMap.keySet();
        for (String key : keySet) {
            Object mapValue = hashMap.get(key);
            if (!key.startsWith("event:")) {
                continue;
            }
            String[] split = key.split(":");
            if (split.length != 3) {
                continue;
            }
            if (mapValue instanceof Boolean) {
                mapValue = (Boolean) mapValue ? AssetStatusItmVo.NORMAL : AssetStatusItmVo.ERROR;
            }
            Integer value = Integer.valueOf(mapValue.toString());
            String eventKey = split[0] + ":" + split[1];
            if (filter.contains(eventKey)) {
                continue;
            }

            AssetStatusItmVo assetStatusItmVo = groupMap.get(eventKey);
            if (Objects.isNull(assetStatusItmVo)) {
                String replace = StatusInfoChangeTypeEnum.getName(eventKey).replace("事件", "").replace("状态", "");
                assetStatusItmVo = new AssetStatusItmVo();
                assetStatusItmVo.setCode(eventKey);
                assetStatusItmVo.setTitle(replace);
                assetStatusItmVo.setStatus(value);

            } else if (AssetStatusItmVo.ERROR.equals(value)) {
                assetStatusItmVo.setStatus(value);
            }
            groupMap.put(eventKey, assetStatusItmVo);
        }

        Set<String> groupMapSet = groupMap.keySet();
        for (String key : groupMapSet) {
            AssetStatusItmVo vo = groupMap.get(key);
            voList.add(vo);
        }

        return voList;
    }


    @Override
    public Object queryAssetPerformanceData(Asset asset, String key) {
        Object o = redisServ.hmGet(asset.getIp() + ":" + asset.getId() + ":status", key);

        return o;
    }

    @Override
    public AssetStatusDetailVo getAssetStatusDetailV2(String assetId, String code) {
        String defName = StatusInfoChangeTypeEnum.getName(code).replace("状态", "");


        AssetStatusDetailVo assetStatusDetailVo = new AssetStatusDetailVo();
        Asset asset = assetServ.getById(assetId);
        if (Objects.isNull(asset)) {
            return assetStatusDetailVo;
        }
        Map<String, Object> hashMap = redisServ.getHashMap(String.format("%s:%s:statusEventValue", asset.getIp()
                , asset.getId()));
        Set<String> cacheKeySet = hashMap.keySet();
        //数据Map
        Map<String, JSONObject> dataGroupMap = new HashMap<>(10);
        //表头的对应关系
        List<JSONObject> titleList = new ArrayList<>();
        List<String> filterList = new ArrayList<>();

        JSONObject json = new JSONObject();
        json.put("property", "name");
        json.put("title", "项");
        titleList.add(json);

        //便利所有的 statusEventValue
        for (String key : cacheKeySet) {
            if (!key.startsWith(code)) {
                continue;
            }
            //值
            Object value = hashMap.get(key);

            String[] keyArray = key.split("\\.");
            //元素的名字 多个的就是例如：磁盘名称  端口名称
            String item = "";
            //元素的属性
            String property = "";
            String statusRedisKey = "";
            if (keyArray.length == 3) {
                item = keyArray[1];
                property = keyArray[2];

                statusRedisKey = keyArray[0] + "." + keyArray[1];
            } else if (keyArray.length == 2) {
                property = keyArray[1];
                item = defName;

                statusRedisKey = keyArray[0];
            } else {
                continue;
            }

            JSONObject dataVo = dataGroupMap.get(item);
            if (Objects.isNull(dataVo)) {
                dataVo = new JSONObject();
                String name = StatusInfoChangeTypeEnum.getName(item);
                dataVo.put("name", name);
            }

            List<String> trueList = Arrays.asList("1", "true", "0");
            if (StatusInfoChangeTypeEnum.STATUS.getCode().equals(property) && Objects.nonNull(value)) {
                dataVo.put(property, value.toString());
            } else if (Objects.nonNull(value)) {
                dataVo.put(property, value);
            }
            //从缓存statusEvent中判定其状态
            Object status = redisServ.hmGet(String.format("%s:%s:statusEvent", asset.getIp()
                    , asset.getId()), statusRedisKey);
            String cacheStatusKey = property + "_VALUE_STATUS";
            dataVo.put(cacheStatusKey, trueList.contains(status.toString()) ? "正常" : "异常");

            if (!filterList.contains(property)) {
                JSONObject titleJson = new JSONObject();
                titleJson.put("property", property);
                titleJson.put("title", StatusInfoChangeTypeEnum.getName(property));
                titleList.add(titleJson);
                filterList.add(property);

                JSONObject titleJson2 = new JSONObject();
                titleJson2.put("property", cacheStatusKey);
                if (StatusInfoChangeTypeEnum.STATUS.getCode().equals(property)) {
                    titleJson2.put("title", "状态判定");
                } else {
                    titleJson2.put("title", StatusInfoChangeTypeEnum.getName(property) + "状态判定");
                }
                titleList.add(titleJson2);

            }

            dataGroupMap.put(item, dataVo);
        }

        Collection<JSONObject> values = dataGroupMap.values();

        //以下循环纯粹为了实现给前端数据排序功能，等到前端可以排序了可以删除此逻辑。
        List<JSONObject> normalList = new ArrayList<>();
        List<JSONObject> errorList = new ArrayList<>();
        for1:
        for (JSONObject item : values) {
            Set<String> keySet = item.keySet();
            for (String str : keySet) {
                if (str.contains("_VALUE_STATUS")) {
                    String valueStr = item.getStr(str);
                    if ("异常".equals(valueStr)) {
                        errorList.add(item);
                        continue for1;
                    }
                }
            }
            normalList.add(item);
        }

        errorList.addAll(normalList);
        assetStatusDetailVo.setDataList(errorList);
        assetStatusDetailVo.setTitleList(titleList);

        return assetStatusDetailVo;
    }

    @Override
    public Map<String, Object> getProcessTop5V2(Asset asset, String type) {
        String cacheKey = String.format("%s:%s:status:%s", asset.getIp(), asset.getId(), type);
        Map<String, Object> hashMap = redisServ.getHashMap(cacheKey);
        if (Objects.isNull(hashMap)) {
            hashMap = new HashMap<>();
        }
        return hashMap;
    }


    @Override
    public InterfaceStatus queryInterfaceStatus(String assetId, String portFullName) {
        Asset asset = assetServ.getById(assetId);
        if (Objects.isNull(asset)) {
            return InterfaceStatus.UNKINOW;
        }
        String portName = interfacesServ.getPortNameByFullName(assetId, portFullName);


        String key = asset.getIp() + ":" + asset.getId() + ":interface_up_down";
        Object status = redisServ.hmGet(key, portName);
        if (Objects.isNull(status)) {
            return InterfaceStatus.UNKINOW;
        }

        Boolean up = InterfaceStatus.isUp(Byte.parseByte(status.toString()));
        if (up) {
            return InterfaceStatus.OK;
        }
        return InterfaceStatus.NO;
    }

}
