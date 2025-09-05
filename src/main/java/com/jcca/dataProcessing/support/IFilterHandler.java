package com.jcca.dataProcessing.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CommonEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO
 * @className IFilterHandler
 * @date 2023/10/20 11:54
 * @since 2.1.0.0
 */
@Slf4j
public abstract class IFilterHandler<T> extends ListenerManager {

    protected IFilterHandler next;


    /**
     * 处理方法
     *
     * @param info
     * @return 返回值true表示可以进入下一层处理，放回false
     * 表示整个处理结束，不会进入下层处理，不会保存缓存
     */
    public abstract boolean handler(T info) throws ResultException, Exception;

    /**
     * 直接控制下层处理
     *
     * @param flag
     * @return 返回true则需要下层处理，返回false不需要下层处理，并且不会保存缓存
     */
    public abstract boolean isNeedNextHandle(Boolean flag);


    /**
     * 设置下层处理类
     *
     * @param next
     */
    public void setNextFilter(IFilterHandler next) {
        this.next = next;
    }


    /**
     * Filter之间的相互衔接方法（此方法相当于一个回调函数，自我循环调用）
     *
     * @param info
     * @param isNeedHandle
     */
    public void handleRequest(T info, boolean isNeedHandle) throws Exception {
        //是否需要下一个处理
        boolean flag = isNeedHandle;

        //flag返回true，可以进行下一个过滤
        if (isNeedHandle) {
            try {
                flag = handler(info);
            }catch (ResultException r1){
                AppLogUtils.buildLogInfo(LogFunctionEnum.ALARM_HANDLE, "", r1.getMessage());
            }catch (Exception exception) {
                AppLogUtils.buildLogError(LogFunctionEnum.ALARM_HANDLE, JSONUtil.toJsonStr(info), exception);
            }
        }
        if (next != null) {
            //下一个继续处理
            next.handleRequest(info, isNeedNextHandle(flag));
        }
    }

    /**
     * 缓存
     * statusEvent 和 statusEventValue
     *
     * @param eventCode
     * @param valueTypeCode
     * @param flag
     * @param value
     * @param info
     * @param change
     */
    public void addEventStatus(String eventCode, String valueTypeCode, String flag, Object value, CommonEntity info, ChangeInfo change) {
        String statusEventRedisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.statusEvent.getCode();
        String eventValueRedisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.statusEventValue.getCode();
        String statusEventMapKey = "";
        String eventValueMapKey = "";
        if (StrUtil.isNotEmpty(flag)) {
            eventValueMapKey = eventCode + "." + flag + "." + valueTypeCode;
            statusEventMapKey = eventCode + "." + flag;
        } else {
            eventValueMapKey = eventCode + "." + valueTypeCode;
            statusEventMapKey = eventCode;
        }

        ChangeInfo changeEventStatus = new ChangeInfo();
        changeEventStatus.setValue(value);
        changeEventStatus.setRedisKey(statusEventRedisKey);
        changeEventStatus.setMapKey(statusEventMapKey);
        if (info.getCollectTime() != null) {
            changeEventStatus.setCollectTime(new Date(info.getCollectTime()));
        } else {
            changeEventStatus.setCollectTime(new Date());
        }
        info.getMaps().put(statusEventMapKey, changeEventStatus);

        ChangeInfo changeEventStatusValue = new ChangeInfo();
        changeEventStatusValue.setValue(change.getValue());
        changeEventStatusValue.setRedisKey(eventValueRedisKey);
        changeEventStatusValue.setMapKey(eventValueMapKey);
        if (info.getCollectTime() != null) {
            changeEventStatus.setCollectTime(new Date(info.getCollectTime()));
        } else {
            changeEventStatus.setCollectTime(new Date());
        }
        info.getMaps().put(changeEventStatusValue.getMapKey(), changeEventStatusValue);
    }

    ;

    /**
     * 缓存中存储阈值设置信息
     *
     * @param redisKey
     * @param mapKey
     * @param value
     * @param info
     */
    public void addThresholdStatus(String redisKey, String mapKey, Object value, CommonEntity info) {

        ChangeInfo changeThresholdInfo = new ChangeInfo();
        changeThresholdInfo.setValue(value);
        changeThresholdInfo.setRedisKey(redisKey);
        if (info.getCollectTime() != null) {
            changeThresholdInfo.setCollectTime(new Date(info.getCollectTime()));
        } else {
            changeThresholdInfo.setCollectTime(new Date());
        }
        changeThresholdInfo.setMapKey(mapKey);
        //内存中的MAP
        info.getMaps().put("thresholdValue_" + mapKey, changeThresholdInfo);

    }

    ;

}

