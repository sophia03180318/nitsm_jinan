package com.jcca.web2.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.utils.EncryptUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.db.entity.ManageDb;
import com.jcca.web.db.service.ManageDbService;
import com.jcca.web.ibmMQ.entity.IBMConnection;
import com.jcca.web.ibmMQ.service.ConnectionService;
import com.jcca.web2.vo.MiddlewareVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description: 中间件接口
 * @author: Lvyp
 * @create: 2023/12/26 10:24
 */
@RestController
@RequestMapping("/api/v2/middleware")
public class MiddlewareControllerV2 {

    @Resource
    private ManageDbService dbServ;
    @Resource
    private ConnectionService connServ;
    @Resource
    private AssetService assetService;

    @GetMapping("/list")
    @ApiOperation("中间件列表")
    public ResultVo<Object> list(String name) {
        List<ManageDb> list = dbServ.list();
        List<IBMConnection> mqList = connServ.list();
        List<MiddlewareVo> respList = new ArrayList<>();
        for (ManageDb manageDb : list) {
            MiddlewareVo vo = new MiddlewareVo();
            vo.setId(manageDb.getId());
            vo.setName(manageDb.getName());
            vo.setType(MiddlewareVo.Type.ORACLE.getCode());
            vo.setCreateTime(manageDb.getCreateTime());
            Asset asset = assetService.getById(manageDb.getAssetId());
            if (ObjectUtil.isNotNull(asset)){
                vo.setIp(asset.getIp());
            }
            respList.add(vo);
        }

        for (IBMConnection ibmConnection : mqList) {
            MiddlewareVo vo = new MiddlewareVo();
            vo.setId(ibmConnection.getId());
            vo.setName(ibmConnection.getConnectName());
            vo.setType(MiddlewareVo.Type.MQ.getCode());
            vo.setIp(ibmConnection.getConnectHost());
            vo.setRemark(ibmConnection.getDescription());
            respList.add(vo);
        }

        if(!respList.isEmpty() && StrUtil.isNotEmpty(name)){
            List<MiddlewareVo> collect = respList.stream().filter(item -> item.getName().contains(name)).collect(Collectors.toList());
            return ResultVoUtil.success(collect);
        }

        return ResultVoUtil.success(respList);
    }

    @GetMapping("/getDetail")
    @ApiOperation("中间件列表")
    public ResultVo<Object> list(String id,Integer type) {
        if(StrUtil.isEmpty(id)|| Objects.isNull(type)){
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        if(type.intValue()==MiddlewareVo.Type.ORACLE.getCode()){
            ManageDb db = dbServ.getById(id);
            if(Objects.nonNull(db)){
                String pwd = EncryptUtil.aesDecryptStr(db.getPassword());
                db.setPassword(pwd);
            }
            return ResultVoUtil.success(db);
        }else{
            IBMConnection mq = connServ.getById(id);
            return ResultVoUtil.success(mq);
        }

    }

}
