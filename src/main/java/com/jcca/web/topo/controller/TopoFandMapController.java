package com.jcca.web.topo.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.service.impl.topoDiscovery.CollectTopoDiscoveryHandlerService;
import com.jcca.web.graph.vo.TopoVertexVo;
import com.jcca.web.topo.controller.bean.QueryPortIndexResp;
import com.jcca.web.topo.controller.bean.TopoBeginFindReq;
import com.jcca.web.topo.controller.bean.TopoFindResp;
import com.jcca.web.topo.entity.NetworkAssetMap;
import com.jcca.web.topo.service.NetworkAssetMapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * topo自动发现
 *
 * @author lyp
 */
@Slf4j
@RestController
@RequestMapping("/api/find")
public class TopoFandMapController {

    public static final String TOPO_FAND_FLAG = "TOPO_FIND_CISCO";
    public static Boolean END_TOPO_FAND = false;

    @Autowired
    private NetworkAssetMapService mapServ;
    @Autowired
    private RedisService redisServ;
    @Autowired
    private AssetService assetServ;


    @Autowired
    private CollectTopoDiscoveryHandlerService collectTopoDiscoveryHandlerService;

    @PostMapping("/queryPortIndex")
    ResultVo<?> queryPortIndex(@RequestBody String req) {
        JSONObject reqJson = JSONUtil.parseObj(req);
        String startAssetId = reqJson.getStr("startAssetId");
        String endAssetId = reqJson.getStr("endAssetId");

        Asset startAsset = assetServ.getById(startAssetId);
        Asset endAsset = assetServ.getById(endAssetId);

        QueryPortIndexResp resp = new QueryPortIndexResp();

        if (Objects.isNull(startAsset) || Objects.isNull(endAsset)) {
            return ResultVoUtil.success(resp);
        }

        QueryWrapper<NetworkAssetMap> queryWrapper = new QueryWrapper<NetworkAssetMap>();
        queryWrapper.eq("LOCALHOST_IP", startAsset.getIp());
        queryWrapper.eq("REMOTE_IP", endAsset.getIp());
        NetworkAssetMap one = mapServ.getOne(queryWrapper);

        QueryWrapper<NetworkAssetMap> queryWrapper2 = new QueryWrapper<NetworkAssetMap>();
        queryWrapper2.eq("LOCALHOST_IP", endAsset.getIp());
        queryWrapper2.eq("REMOTE_IP", startAsset.getIp());
        NetworkAssetMap two = mapServ.getOne(queryWrapper2);

        if (Objects.nonNull(one)) {
            resp.setEndPort(one.getRemotePort());
        }
        if (Objects.nonNull(two)) {
            resp.setStartPort(two.getRemotePort());
        }

        return ResultVoUtil.success(resp);
    }

    @PostMapping("/list")
    ResultVo<?> list(@RequestBody String req) {
        JSONObject reqJson = JSONUtil.parseObj(req);
        String ip = reqJson.getStr("ip");
        String assetId = reqJson.getStr("assetId");

        List<TopoFindResp> assetList = new ArrayList<TopoFindResp>();
        if (StrUtil.isEmpty(ip)) {
            Asset asset = null;
            if (StrUtil.isNotEmpty(assetId)) {
                asset = assetServ.getById(assetId);
            }
            if (Objects.nonNull(asset)) {
                ip = asset.getIp();
            } else {
                return ResultVoUtil.success(assetList);
            }
        }

        if (StrUtil.isEmpty(assetId)) {
            Asset asset = assetServ.findOneByIp(ip);
            if (Objects.nonNull(asset)) {
                assetId = asset.getId();
            }
        }

        QueryWrapper<NetworkAssetMap> queryWrapper = new QueryWrapper<NetworkAssetMap>();
        queryWrapper.eq("LOCALHOST_IP", ip);
        List<NetworkAssetMap> ipList = mapServ.list(queryWrapper);

        fateenResp(assetList, ipList, assetId);

        return ResultVoUtil.success(assetList);
    }

    /**
     * 封装响应信息
     *
     * @param assetList
     * @param ipList
     */
    private void fateenResp(List<TopoFindResp> assetList, List<NetworkAssetMap> ipList, String localId) {
        for (NetworkAssetMap item : ipList) {
            String remoteIp = item.getRemoteIp();
            // 查询远端IP对应的管理口IP
            String assetIp = mapServ.findAssetIpByRemoteIp(remoteIp);
            if (StrUtil.isEmpty(assetIp)) {
                log.error("拓扑发现-未查询到端口IP对应的资产ip");
                continue;
            }

            QueryWrapper<NetworkAssetMap> queryWrapper = new QueryWrapper<NetworkAssetMap>();
            queryWrapper.eq("LOCALHOST_IP", assetIp);
            queryWrapper.eq("REMOTE_IP", item.getLocalhostIp());
            List<NetworkAssetMap> list = mapServ.list(queryWrapper);

            Asset asset = assetServ.findOneByIp(assetIp);
            if (Objects.nonNull(asset)) {
                TopoFindResp resp = new TopoFindResp();
                resp.setLocalhostIp(item.getLocalhostIp());
                resp.setRemoteIp(remoteIp);
                resp.setRemoteAssetName(asset.getName());
                resp.setRemotePortIndex(item.getRemotePort());
                resp.setRemotePortIp(remoteIp);
                resp.setRemoteAssetId(asset.getId());
                resp.setLocalAssetId(localId);

                if (Objects.nonNull(list) && !list.isEmpty()) {
                    resp.setLocalPortIndex(list.get(0).getRemotePort());
                }
                assetList.add(resp);
            }
        }
    }

    @PostMapping("/beginFind")
    @ActionLog(name = "开始拓扑发现", title = "拓扑发现", key = LogTypeConstant.QUERY)
    ResultVo<?> beginFind() {
        if (collectTopoDiscoveryHandlerService.getDiscoverStatus()) {
            return ResultVoUtil.success("拓扑发现正在执行");
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    collectTopoDiscoveryHandlerService.getAllNetAssetInfo();
                }
            }).start();
            return ResultVoUtil.success("拓扑发现开始执行");
        }
    }

    @PostMapping("/begin")
    ResultVo<?> begin() {
        return ResultVoUtil.success(collectTopoDiscoveryHandlerService.getDiscoverStatus());


//        Object flag = redisServ.get(TOPO_FAND_FLAG);
//        if (Objects.nonNull(flag)) {
//            return;
//        }
//        END_TOPO_FAND = false;
//
//        redisServ.set(TOPO_FAND_FLAG, TOPO_FAND_FLAG, 20 * 60L);
//
//        try {
//            mapServ.beginSeachAll();
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }

    }

//    @PostMapping("/end")
//    ResultVo<?> end() {
//        END_TOPO_FAND = true;
//        return ResultVoUtil.success("已中断");
//    }

    /**
     * 查询topo发现是否执行结束
     *
     * @return
     */
    @PostMapping("/verifyStatus")
    ResultVo<?> verifyStatus() {
        Object flag = redisServ.get(TOPO_FAND_FLAG);
        if (Objects.nonNull(flag)) {
            return ResultVoUtil.success(false);
        }

        return ResultVoUtil.success(true);
    }

    @PostMapping("/getAssetTopoFindStatus")
    ResultVo<?> getAssetTopoFindStatus(@RequestBody TopoBeginFindReq req) {
        List<String> assetIdList = req.getAssetIdList();
        if (Objects.isNull(assetIdList)) {
            return ResultVoUtil.success(new ArrayList<JSONObject>());
        }

        List<JSONObject> assetList = new ArrayList<JSONObject>();
        for (String id : assetIdList) {
            JSONObject resp = new JSONObject();
            Boolean status = mapServ.verifyTopoFindStatus(id);
            resp.put("assetId", id);
            resp.put("findFinishFlag", status ? TopoVertexVo.FIND_OK : TopoVertexVo.FIND_NO);
            assetList.add(resp);
        }

        return ResultVoUtil.success(assetList);
    }

}
