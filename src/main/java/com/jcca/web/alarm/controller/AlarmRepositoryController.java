package com.jcca.web.alarm.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.common.bean.PageBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.enums.AlarmLevelEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.TestIpUtil;
import com.jcca.dataProcessing.manager.alarmRepo.AlarmRepoEvent;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.web.alarm.controller.bean.AddAlarmRepoReq;
import com.jcca.web.alarm.controller.bean.AlarmRepoPageQuery;
import com.jcca.web.alarm.controller.bean.UpdateAlarmRepoReq;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.alarm.vo.AlarmDetailVo;
import com.jcca.web.alarm.vo.AlarmRepositoryVo;
import com.jcca.web.asset.controller.bean.Repository;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.event.service.AlarmEventTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 告警知识库
 *
 * @author Lvyp
 */
@Api(tags = "告警知识库相关接口")
@RestController
@Slf4j
@RequestMapping("/api/alarm/repo")
public class AlarmRepositoryController extends ListenerManager {

    @Value("${kbUrl}")
    private String kbUrl;
    @Resource
    private AssetService assetService;
    @Resource
    private AlarmRepositoryService alarmRepoService;
    @Resource
    private AlarmEventTypeService eventTypeService;



    /**
     * 新增告警知识库记录
     *
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "告警知识库记录新增")
    @RequiresPermissions({"api:alarm:repo:add"})
    @ActionLog(name = "新增知识库记录", title = "知识库", key = LogTypeConstant.ADD)
    ResultVo<AlarmRepository> add(@Validated @RequestBody AddAlarmRepoReq req) {
        AlarmRepository copy = EntityBeanUtil.copy(req, AlarmRepository.class);

        this.dispatureEvent(new AlarmRepoEvent());

        return alarmRepoService.create(copy);
    }

    /**
     * 分页查询
     *
     * @param req
     * @return
     */
    @PostMapping("/query")
    @ApiOperation(value = "告警知识库分页查询")
    @RequiresPermissions({"api:alarm:repo:query"})
    @ActionLog(name = "查看知识库列表", title = "知识库", key = LogTypeConstant.QUERY)
    ResultVo<PageBean<AlarmRepositoryVo>> pageQuery(@RequestBody AlarmRepoPageQuery req) {
        IPage<AlarmRepository> ipage = PagePlugin.startPageT(req.getPage(), req.getSize(), AlarmRepository.class);

        QueryWrapper<AlarmRepository> queryWrapper = new QueryWrapper<AlarmRepository>();

        if (Objects.nonNull(req.getAlarmLevel())) {
            queryWrapper.eq("ALARM_LEVEL", req.getAlarmLevel());
        }
        if (StrUtil.isNotEmpty(req.getAlarmCode())) {
            queryWrapper.like("ALARM_CODE", req.getAlarmCode());
        }
        if (StrUtil.isNotEmpty(req.getName())) {
            queryWrapper.like("NAME", req.getName());
        }

        IPage<AlarmRepository> page = alarmRepoService.page(ipage, queryWrapper);
        List<AlarmRepository> records = page.getRecords();

        List<AlarmRepositoryVo> voList = new ArrayList<AlarmRepositoryVo>();
        for (AlarmRepository body : records) {
            AlarmRepositoryVo vo = EntityBeanUtil.copy(body, AlarmRepositoryVo.class);
            vo.setAlarmLevelStr(AlarmLevelEnum.getMsg(new Byte(body.getAlarmLevel().toString())));
            if (Objects.nonNull(body.getFlagType())) {
                vo.setFlagTypeStr(EventLevelEnum.getMsgByCode(body.getFlagType()));
            }
            if (Objects.nonNull(body.getEventTypeId())) {
                AlarmEventType eventType = eventTypeService.getById(body.getEventTypeId());
                if (Objects.nonNull(eventType)) {
                    vo.setEventTypeStr(eventType.getName());
                }
            }
            voList.add(vo);
        }

        PageBean<AlarmRepositoryVo> pageBean = new PageBean<AlarmRepositoryVo>();
        pageBean.setContent(voList);
        pageBean.setTotal(page.getTotal());

        return ResultVoUtil.success(pageBean);

    }

    /**
     * 告警知识库记录删除
     */
    @PostMapping("/remove/{id}")
    @ApiOperation(value = "告警知识库记录删除")
    @RequiresPermissions({"api:alarm:repo:remove"})
    @ActionLog(name = "删除知识库记录", title = "知识库", key = LogTypeConstant.REMOVEE)
    ResultVo<String> remove(@PathVariable("id") String ids) {
        if (StrUtil.isEmpty(ids)) {
            return ResultVoUtil.paramError("请求缺失参数ID", String.class);
        }
        AlarmRepository repository = alarmRepoService.getById(ids);
        if (Objects.isNull(repository)) {
            return ResultVoUtil.paramError("该记录不存在", String.class);
        }
        alarmRepoService.removeById(repository);

        this.dispatureEvent(new AlarmRepoEvent());

        return ResultVoUtil.REMOVE_SUCCESS;
    }

    /**
     * 告警知识库更新
     */
    @PostMapping("/update")
    @ApiOperation(value = "告警知识库记录更新")
    @RequiresPermissions({"api:alarm:repo:update"})
    @ActionLog(name = "修改知识库记录", title = "知识库", key = LogTypeConstant.MODIFY)
    ResultVo<String> update(@RequestBody UpdateAlarmRepoReq req) {
        Integer alarmLevel = req.getAlarmLevel();
        String alarmLevelStr = AlarmLevelEnum.getMsg(new Byte(alarmLevel.toString()));

        if (StrUtil.equals(alarmLevelStr, alarmLevel.toString())) {
            return ResultVoUtil.paramError("请选择正确的告警级别", String.class);
        }

        QueryWrapper<AlarmRepository> queryWrapper = new QueryWrapper<AlarmRepository>();
        queryWrapper.eq("ALARM_CODE", req.getAlarmCode());
        queryWrapper.eq("STATUS_FLAG", req.getStatusFlag());

        AlarmRepository one = alarmRepoService.getOne(queryWrapper);
        if (Objects.nonNull(one)) {
            if (!one.getId().equals(req.getId())) {
                return ResultVoUtil.paramError("已存在相同的知识库", String.class);
            }
        }

        AlarmRepository repository = alarmRepoService.getById(req.getId());
        AlarmRepository body = EntityBeanUtil.replaceParameter(req, repository, AlarmRepository.class);
        alarmRepoService.updateById(body);

        this.dispatureEvent(new AlarmRepoEvent());

        return ResultVoUtil.success("更新成功");
    }


    /**
     * 为专家知识库 查询对应资产详情
     *
     * @return
     */
    @PostMapping("/getRepository")
    public ResultVo<List<Repository>> getRepository(@RequestBody AlarmDetailVo alarmInfo) {
        Boolean ping = null;
        try {
            ping = TestIpUtil.ping(kbUrl, 1);
        } catch (IOException e) {
            log.error(kbUrl + " Ping报错  请检查~");
            return ResultVoUtil.success();
        }
        if (!ping) {
            log.error(kbUrl + " Ping不通  请检查~");
            return ResultVoUtil.success();
        }
        Repository repository = new Repository();
        Asset asset = assetService.getById(alarmInfo.getAssetId());
        repository.setType(DictUtil.keyValue("ASSET_MODE", asset.getDesk() + ""));
        repository.setManufacturer(DictUtil.keyValue("ASSET_FACTORY", asset.getManufacturerId() + ""));
        repository.setModel(asset.getAssetImage());
        repository.setMsg(alarmInfo.getContent());
        try {
            String body = HttpRequest.post(kbUrl + ":9996/api/free/pushPlanList").setReadTimeout(10000).setConnectionTimeout(10000).body(JSONUtil.toJsonStr(repository)).execute().body();
            log.debug("访问专家知识库{}：响应数据：{}", kbUrl, body);
            String code = JSONUtil.parseObj(body).getStr("code", "999");
            if (!"200".equals(code)) {
                return ResultVoUtil.error("返回码:" + code + "body:" + body);
            }
            String json = JSONUtil.parseObj(body).getStr("data", "");
            JSONArray repositorys = JSONUtil.parseArray(json);
            List<Repository> repositoryList = JSONUtil.toList(repositorys, Repository.class);
            return ResultVoUtil.success(repositoryList);
        } catch (Exception e) {
            log.error(e.toString());
            return ResultVoUtil.success();

        }
    }
}
