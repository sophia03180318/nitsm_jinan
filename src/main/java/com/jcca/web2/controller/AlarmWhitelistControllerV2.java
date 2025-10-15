package com.jcca.web2.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.dataProcessing.manager.cache.CacheEvent;
import com.jcca.dataProcessing.manager.cache.CacheOptEnum;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.web2.dto.AlarmWhitelistAddDto;
import com.jcca.web2.dto.WhitePageQueryDto;
import com.jcca.web2.entity.AlarmWhitelist;
import com.jcca.web2.service.AlarmWhitelistService;
import com.jcca.web2.vo.WhitePageQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 告警规则白名单
 *
 * @description: 告警规则白名单
 * @author: Lvyp
 * @create: 2023/11/30 11:10
 */
@RestController
@RequestMapping("/api/v2/white")
@Api(tags = "告警规则白名单V2")
public class AlarmWhitelistControllerV2 extends ListenerManager {

    @Resource
    private AlarmWhitelistService whitelistServ;

    @ApiOperation("添加白名单")
    @PostMapping("/add")
    public ResultVo add(@RequestBody @Validated AlarmWhitelistAddDto req) {
        if (ObjectUtil.isEmpty(req.getAssetId())) {
            return ResultVoUtil.error("资产数据为空");
        }
        whitelistServ.addWhite(req);
        return ResultVoUtil.success();
    }

    @ApiOperation("移除白名单")
    @PostMapping("/remove")
    public ResultVo remove(@RequestBody AlarmWhitelistAddDto req) {
        String id = req.getId();
        if (StrUtil.isEmpty(id)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        AlarmWhitelist entity = whitelistServ.getById(id);
        if(Objects.isNull(entity)){
            return ResultVoUtil.success();
        }
        int typeMark = entity.getTypeMark();
        whitelistServ.removeByIdV2(id, typeMark);

        CacheEvent event = new CacheEvent();
        event.setOpt(CacheOptEnum.REMOVE);
        event.setEventKey(entity.getAlarmCode());
        event.setMapKey(entity.getFlag());
        this.dispatureEvent(event);

        return ResultVoUtil.success();
    }

    @ApiOperation("分页查询白名单")
    @GetMapping("/pageQuery")
    public ResultVo pageQuery(WhitePageQueryDto query) {
        IPage<WhitePageQueryVo> result = whitelistServ.pageV2(query);

        return ResultVoUtil.success(result);
    }

    @ApiOperation("获取屏蔽名单详情")
    @GetMapping("/detail/{whiteId}")
    public ResultVo add(@PathVariable(value = "whiteId") String whiteId) {
        return ResultVoUtil.success(whitelistServ.queryDetailById(whiteId));
    }

}
