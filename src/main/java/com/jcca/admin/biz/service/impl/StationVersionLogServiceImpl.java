package com.jcca.admin.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.biz.dao.StationMapper;
import com.jcca.admin.biz.dao.StationVersionLogMapper;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.entity.StationVersionLog;
import com.jcca.admin.biz.enums.StationVersionStatusEnum;
import com.jcca.admin.biz.service.StationVersionLogService;
import com.jcca.admin.biz.service.bean.UpdateResult;
import com.jcca.admin.system.controller.bean.BeginUpdateReq;
import com.jcca.admin.system.dao.VersionMsgMapper;
import com.jcca.admin.system.entity.SysFile;
import com.jcca.admin.system.entity.VersionMsg;
import com.jcca.admin.system.service.SysFileService;
import com.jcca.common.bean.RestBean;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppJarFileUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.UrlUtil;
import com.jcca.common.utils.file.FileUpload;
import com.jcca.component.client.StationCollectClient;
import com.jcca.component.quartz.station.QuartzStationUploadManagerJob;
import com.jcca.web.asset.dao.AssetMapper;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.service.CollectCpuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 车站版本记录
 *
 * @author lyp
 */
@Slf4j
@Service
public class StationVersionLogServiceImpl extends ServiceImpl<StationVersionLogMapper, StationVersionLog>
        implements StationVersionLogService {

    private static final String STATION_UPLOAD_URI = "/bg/system/upload";

    @Resource
    private StationVersionLogMapper logMapper;
    @Resource
    private StationMapper stationMapper;
    @Resource
    private VersionMsgMapper versionMapper;
    @Resource
    private RedisService redisServ;
    @Resource
    private SysFileService sysFileService;
    @Resource
    private StationCollectClient stationClient;
    @Resource
    private CollectCpuService collectCpuService;
    @Resource
    private AssetMapper assetMapper;

    @Override
    public StationVersionLog findLastLogByStation(String orgId) {
        return logMapper.selectLasterLogByStationId(orgId);
    }

    @Override
    public List<String> listorgIdByVersion(String version) {

        return logMapper.selectOrgIdByVersion(version);
    }

    @Override
    public void beginUpdate(BeginUpdateReq req) throws Exception {
        List<String> stationIdList = req.getStationIdList();

        String key = "LOCKED_ADD_STATION_UPDATE_JOB";

        synchronized (key.intern()) {
            List<StationVersionLog> logList = new ArrayList<StationVersionLog>();

            for (String stationId : stationIdList) {
                // 查看此车站是否有正在升级的任务
                if (StrUtil.isEmpty(stationId)) {
                    continue;
                }
                StationVersionLog logEntity = logMapper.selectLoadingJob(stationId);
                if (Objects.nonNull(logEntity)) {
                    // 抛出异常
                    Station station = stationMapper.selectById(logEntity.getStationId());
                    throw new Exception(String.format("车站【%s】有正在进行中的任务，请点击详情处理！", station.getTitle()));
                }

                VersionMsg versionMsg = versionMapper.selectById(req.getVersionId());
                if (Objects.isNull(versionMsg)) {
                    if (LogInputUtils.inputError(ServerTypeEnum.SYSTEM_STATION)) {
                        log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.SYSTEM_STATION, ErrorCodeEnum.SYSTEM_STATION_ADD, "", "选定的版本已不存在"));
                    }
                    throw new Exception("选择的版本已经不存在！请重新上传版本");
                }
                Station station = stationMapper.selectById(stationId);

                SysFile file = sysFileService.getById(versionMsg.getSysFileId());

                // 判定文件是否上传过车站，如果上传过直接跳过重复上传操作。
                StationVersionLog entity = logMapper.selectBySuccessLog(versionMsg.getId(), file.getId());
                if (Objects.nonNull(entity)) {
                    StationVersionLog copy = EntityBeanUtil.copy(entity, StationVersionLog.class);
                    copy.setStatus(StationVersionStatusEnum.UPLOAD_OK.name());
                    copy.setUpdateRate("60");
                    copy.setStartDate(new Date());
                    copy.setRemark("车站已有记录免上传");
                    copy.setId(MyIdUtil.getId());
                    logList.add(copy);
                    continue;
                }

                // 新建记录，等待定时任务调用上传
                logEntity = new StationVersionLog();
                logEntity.setId(MyIdUtil.getId());
                logEntity.setCommitId("");
                logEntity.setStartDate(new Date());
                logEntity.setFinishSize(0);
                logEntity.setSavePath(station.getFilePath());
                logEntity.setFutureVersion(versionMsg.getVersion());
                logEntity.setIntervalTime(req.getIntervalTime().toString());
                logEntity.setRateLimi(req.getRateLimi().toString());
                logEntity.setRemark("");
                logEntity.setStationId(stationId);
                logEntity.setUpdateRate("0");
                logEntity.setVersion("");
                logEntity.setSysFileId(file.getId());
                logEntity.setJarName(file.getOrignName());
                logEntity.setVersionMsgId(versionMsg.getId());
                logEntity.setStatus(StationVersionStatusEnum.AWAIT_UPLOADING.name());

                logList.add(logEntity);
            }

            saveBatch(logList);

        }

    }

    @Async("taskExecutor")
    @Override
    public void uploadJarToStation(StationVersionLog logReq) {
        String versionMsgId = logReq.getVersionMsgId();
        String stationId = logReq.getStationId();

        Station station = stationMapper.selectById(stationId);
        VersionMsg versionMsg = versionMapper.selectById(versionMsgId);
        if (Objects.isNull(versionMsg)) {
            logReq.setStatus(StationVersionStatusEnum.UPLOAD_FAIL.name());
            logReq.setRemark("版本信息被移除，请删除此次记录重新操作");
            logReq.setUpdateRate("0");
            logMapper.updateById(logReq);
            redisServ.remove(QuartzStationUploadManagerJob.KEY_UPLOAD);
            return;
        }
        SysFile file = sysFileService.getById(versionMsg.getSysFileId());
        List<String> uploadUrlList = new ArrayList<>();
        try {
            String uploadUrl = UrlUtil.getStationUrlPrefix(station.getProxyUrl(), station.getIp(), station.getPort() + "") + STATION_UPLOAD_URI;
            uploadUrlList.add(uploadUrl);
            if (StrUtil.isNotEmpty(station.getIp2())) {
                String uploadUrl2 = UrlUtil.getStationUrlPrefix(station.getProxyUrl(), station.getIp2(), station.getPort() + "") + STATION_UPLOAD_URI;
                uploadUrlList.add(uploadUrl2);
            }

            logReq.setStatus(StationVersionStatusEnum.UPLOADING.name());
            logReq.setRemark("开始上传");
            logReq.setUpdateRate("1");
            logMapper.updateById(logReq);
        } catch (Exception e) {
            redisServ.remove(QuartzStationUploadManagerJob.KEY_UPLOAD);
            log.error("更新状态出错：" + e.getMessage(), e);
            return;
        }

        while (true) {
            Integer finishSize = logReq.getFinishSize();
            Integer limi = Integer.valueOf(logReq.getRateLimi());

            if (Objects.isNull(finishSize)) {
                finishSize = 0;
            }

            byte[] bytes = null;
            try {
                String filePath = FileUpload.getFilePath(file);
                String fileName = file.getFileName();
                String orignNames = file.getOrignName();

                // 获取文件流
                bytes = AppJarFileUtils.getBytes(file, finishSize, limi);
                if (Objects.isNull(bytes)) {

                    logReq.setStatus(StationVersionStatusEnum.UPLOAD_OK.name());
                    logReq.setRemark("上传成功");
                    logReq.setUpdateRate("60");
                    logMapper.updateById(logReq);
                    redisServ.remove(QuartzStationUploadManagerJob.KEY_UPLOAD);

                    return;
                }
                // 流转文件
                String subFilePath = AppJarFileUtils.saveByteToFile(bytes, filePath.replace(fileName, ""), orignNames);
                // 提交上传
                AppJarFileUtils.httpPost(subFilePath, orignNames, uploadUrlList, logReq.getFutureVersion(),
                        logReq.getSavePath(), finishSize);

                Integer updateRate = Integer.valueOf(logReq.getUpdateRate());
                if (updateRate < 60) {
                    Integer fileSize = versionMapper.selectFileSize(logReq.getVersionMsgId());
                    double result = new BigDecimal((float) finishSize / fileSize).setScale(4, BigDecimal.ROUND_HALF_UP)
                            .doubleValue();
                    updateRate = Integer.parseInt(new java.text.DecimalFormat("0").format((result * 60)));

                }
                logReq.setFinishSize(finishSize + limi);
                logReq.setUpdateRate(updateRate.toString());
                logMapper.updateById(logReq);
                // 更新redis缓存时间
                redisServ.set(QuartzStationUploadManagerJob.KEY_UPLOAD, MyIdUtil.getId(), 60 * 60L);

                // 上传间隔
                String intervalTime = logReq.getIntervalTime();
                try {
                    Thread.sleep(Integer.parseInt(intervalTime) * 1000);
                } catch (Exception e) {
                    log.error("上传间隔睡眠失败：" + e.getMessage(), e);
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                logReq.setStatus(StationVersionStatusEnum.UPLOAD_FAIL.name());
                logReq.setRemark("上传失败：" + e.getMessage());

                logMapper.updateById(logReq);
                redisServ.remove(QuartzStationUploadManagerJob.KEY_UPLOAD);

                return;
            }

        }

    }

    @Override
    public void updateJarToStation(StationVersionLog item) {
        String jarPath = item.getSavePath() + "/" + item.getFutureVersion() + "/" + item.getJarName();

        VersionMsg versionMsg = versionMapper.selectById(item.getVersionMsgId());
        SysFile file = sysFileService.getById(versionMsg.getSysFileId());
        String filePath = FileUpload.getFilePath(file);
        File jarFile = new File(filePath);

        RestBean updateJar = stationClient.updateJar(jarPath, item.getStationId(), jarFile.length());

        if (RestBean.SUCCESS.equals(updateJar.getCode())) {
            item.setStatus(StationVersionStatusEnum.UPDATEING.name());
            item.setRemark("提交更新请求成功");
            item.setUpdateRate("90");
        } else {
            item.setStatus(StationVersionStatusEnum.UPDATE_FAIL.name());
            item.setRemark(updateJar.getMsg());
            item.setUpdateRate("70");
        }

        logMapper.updateById(item);
    }

    @Override
    public List<String> listJarName(String orgId) {

        return logMapper.listJarName(orgId);
    }

    @Override
    public StationVersionLog lastLog(String orgId, String jarName) {

        return logMapper.lastLog(orgId, jarName);
    }

    @Override
    public void deleteJarAndAfreshUpload(StationVersionLog item) throws Exception {
        String jarPath = item.getSavePath() + "/" + item.getFutureVersion() + "/" + item.getJarName();

        RestBean removeJar = stationClient.removeJar(jarPath, item.getStationId());
        if (RestBean.ERROR.equals(removeJar.getCode())) {
            throw new Exception("刪除JAR失败：" + removeJar.getMsg());
        }

        item.setStatus(StationVersionStatusEnum.AWAIT_UPLOADING.name());
        item.setFinishSize(0);
        item.setRemark("重新排队上传JAR");
        item.setUpdateRate("0");

        updateById(item);
    }

    @Override
    public void queryUpdateResult(StationVersionLog item) {
        Station station = stationMapper.selectById(item.getStationId());

        RestBean result = stationClient.queryUpdateResult(item.getStationId());
        if (RestBean.ERROR.equals(result.getCode())) {
            log.error("查询车站升级结果失败：" + station.getTitle());
            return;
        }
        Object body = result.getBody();
        UpdateResult updateResult = JSONUtil.toBean(JSONUtil.parseObj(body), UpdateResult.class);

        RestBean updateResultFlag = updateResult.getResult();
        if (Objects.isNull(updateResultFlag)) {
            log.error("车站处于升级中，暂无结果：" + station.getTitle());
            return;
        }
        if (RestBean.SUCCESS.equals(updateResultFlag.getCode())) {
            // 更新成功
            item.setStatus(StationVersionStatusEnum.UPDATE_SUCCESS.name());
            item.setEndDate(new Date());
            item.setUpdateRate("100");
            item.setRemark("更新成功");
            item.setCommitId(updateResult.getCommitId());
            item.setVersion(updateResult.getVersion());
        } else {
            item.setStatus(StationVersionStatusEnum.UPDATE_FAIL.name());
            item.setRemark(updateResultFlag.getMsg());
            item.setCommitId(updateResult.getCommitId());
            item.setVersion(updateResult.getVersion());
        }

        updateById(item);
    }

    @Override
    public Boolean queryStationRunStatus(Station station) {
        RestBean result = stationClient.queryUpdateResult(station.getOrgId());

        if (RestBean.ERROR.equals(result.getCode())) {
            station.setRunStatus("异常");
            stationMapper.updateById(station);
            return false;
        }
        StationVersionLog lastLog = logMapper.selectLasterLogByStationId(station.getOrgId());

        Object body = result.getBody();
        UpdateResult updateResult = JSONUtil.toBean(JSONUtil.parseObj(body), UpdateResult.class);

        if (Objects.nonNull(lastLog)) {
            lastLog.setVersion(updateResult.getVersion());
            lastLog.setCommitId(updateResult.getCommitId());

            logMapper.updateById(lastLog);
        }

        station.setTargetName(updateResult.getVersion());
        station.setRunStatus("正常");
        stationMapper.updateById(station);
        return true;

    }

    @Override
    public Integer getBetweenTime(String stationId) {
        List<Asset> assets = assetMapper.findAssetByOrgIdAndWatch(stationId, 1);
        if (!assets.isEmpty()) {
            List<CollectCpu> realTimeData = collectCpuService.getRealTimeData(assets.get(0).getId());
            try {
                if (!realTimeData.isEmpty()) {
                    Date collectTime = realTimeData.get(0).getCollectTime();
                    Date date = new Date();
                    long between = (date.getTime() - collectTime.getTime()) / (3600 * 1000);
                    if (between >= 2) {
                        return 2;
                    } else {
                        return 1;
                    }
                }
            } catch (Exception e) {
                return 0;
            }

        }

        return 0;// 不改变状态
    }
}
