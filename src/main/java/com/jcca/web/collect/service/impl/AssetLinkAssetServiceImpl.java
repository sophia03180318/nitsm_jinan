package com.jcca.web.collect.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.LinkAssetExportVo;
import com.jcca.web.collect.controller.route.bean.AssetLinkAssetVo;
import com.jcca.web.collect.controller.route.bean.AssetLinkConst;
import com.jcca.web.collect.dao.AssetLinkAssetMapper;
import com.jcca.web.collect.entity.AssetLinkAsset;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.service.AssetLinkAssetService;
import com.jcca.web.collect.service.CollectNetworkCardService;
import com.jcca.web.collect.service.CollectRouteService;
import com.jcca.web.common.service.bean.ThreeDLinkReq;
import com.jcca.web.graph.entity.TopoEdge;
import com.jcca.web.graph.entity.TopoVertex;
import com.jcca.web.graph.service.TopoEdgeService;
import com.jcca.web.graph.service.TopoVertexService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author hanhw
 * @description 对端设备连接信息
 * @className AssetLinkAssetServiceImpl
 * @date 2023/4/27 9:38
 * @since 2.0.3.0
 */
@Service
public class AssetLinkAssetServiceImpl extends ServiceImpl<AssetLinkAssetMapper, AssetLinkAsset> implements AssetLinkAssetService {

    @Resource
    private TopoVertexService topoVertexService;
    @Resource
    private AssetService assetService;
    @Resource
    private AssetLinkAssetMapper linkAssetMapper;
    @Resource
    private TopoEdgeService topoEdgeService;
    @Resource
    private CollectNetworkCardService networkCardService;
    @Resource
    private CollectRouteService collectRouteService;

    /**
     * 保存对端设备信息
     * 1拓扑图配置的，2自动发现的，3手动修改的
     */
    @Override
    public synchronized void saveLinkAsset(String maOrAt) {

        List<AssetLinkAsset> finalList = this.getFinalList(null);

        // 初次保存所有数据
        int count = this.count();
        if (count == 0) {
            this.saveBatch(finalList);
            return;
        }

        // 如果是通拓扑图更新 更新连接信息
        if (AssetLinkConst.TOPO_SET.equals(maOrAt)) {
            this.savetopoSet(finalList);
        } else if (AssetLinkConst.AUTO_SNIFFER.equals(maOrAt)) {
            // 如果是通过自动的更新只更新自动更新的，不更新手动修改的和拓扑配置的
            QueryWrapper<AssetLinkAsset> query;
            List<AssetLinkAsset> slist = new ArrayList<>();
            List<AssetLinkAsset> ulist = new ArrayList<>();
            for (AssetLinkAsset linkAsset : finalList) {
                query = Wrappers.query();
                query.eq("ASSET_ID", linkAsset.getAssetId());

                //如果此设备不存在对端 则直接保存
                List<AssetLinkAsset> assetLinkList = this.list(query);
                if (assetLinkList.isEmpty()) {
                    this.save(linkAsset);
                    continue;
                }

                try {
                    query = Wrappers.query();
                    query.eq("ASSET_ID", linkAsset.getAssetId());
                    query.eq("PORT_INDEX", linkAsset.getPortIndex());
                    AssetLinkAsset one = this.getOne(query);
                    if (Objects.nonNull(one)) {
                        String ma = one.getMaOrAt();
                        if (AssetLinkConst.AUTO_SNIFFER.equals(ma)) {
                            linkAsset.setId(one.getId());
                        }
                        ulist.add(linkAsset);
                    } else {
                        slist.add(linkAsset);
                    }
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.ASSET_LINK_ASSET, "资产端口有重复数据：" + linkAsset.getAssetId(), e);
                }
            }
            if (!CollectionUtils.isEmpty(ulist)) {
                this.updateBatchById(ulist);
            }
            if (!CollectionUtils.isEmpty(slist)) {
                this.saveBatch(slist);
            }
        }
    }

    private void savetopoSet(List<AssetLinkAsset> finalList) {
        QueryWrapper<AssetLinkAsset> query;
        List<AssetLinkAsset> list = new ArrayList<>();
        for (AssetLinkAsset linkAsset : finalList) {
            query = Wrappers.query();
            query.eq("ASSET_ID", linkAsset.getAssetId());
            query.eq("PORT_INDEX", linkAsset.getPortIndex());
            try {
                //如果此设备不存在对端 则直接保存
                AssetLinkAsset one = this.getOne(query);
                if (Objects.isNull(one)) {
                    list.add(linkAsset);
                    continue;
                }
                linkAsset.setId(one.getId());
                list.add(linkAsset);
            } catch (Exception e) {
                AppLogUtils.buildLogError(LogFunctionEnum.ASSET_LINK_ASSET, "资产端口有重复数据：" + linkAsset.getAssetId(), e);
            }
        }
        if (!CollectionUtils.isEmpty(list)) {
            this.saveOrUpdateBatch(list);
        }
    }

    private List<AssetLinkAsset> getFinalList(String asetId) {
        List<AssetLinkAsset> finalList = new ArrayList<>();
        List<AssetLinkAsset> linkList = new ArrayList<>();

        try {
            // 采集端口和自动发现的对端设备及端口 2
            List<AssetLinkAssetVo> linkAtAssetList;
            if (StringUtils.isNotEmpty(asetId)) {
                linkAtAssetList = collectRouteService.findAtAssetAndPort(asetId);
            } else {
                linkAtAssetList = collectRouteService.findAtAssetAndPort(null);
            }
            // 使用自动发现的设备填充对端信息
            for (AssetLinkAssetVo atAsset : linkAtAssetList) {
                AssetLinkAsset linkAsset = this.setAtLinkAsset(atAsset);
                linkAsset.setId(MyIdUtil.getId());
                linkList.add(linkAsset);
            }
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.NETWORK_DETAIL, asetId, e);
            return new ArrayList<>();
        }


        // 使用拓扑图配置的填充对端信息
        try {
            for (AssetLinkAsset linkAsset : linkList) {
                String assetId = linkAsset.getAssetId();
                String portIndex = linkAsset.getPortIndex();
                finalList.add(linkAsset);
                if (StrUtil.isEmpty(portIndex)) continue;

                // 查拓扑图配置情况
                // 1、先查出某设备某端口的端口配置的拓扑图信息
                QueryWrapper<TopoVertex> vertexQuery = Wrappers.query();
                vertexQuery.eq("ASSET_ID", assetId);
                vertexQuery.eq("PORT_INDEX", portIndex);
                List<TopoVertex> list = topoVertexService.list(vertexQuery);
                if (CollectionUtil.isEmpty(list)) continue;
                TopoVertex vertex = list.get(0);
                // 2、查该端口的连线信息
                String peerNodeId = "";
                String nodeId = vertex.getNodeId();
                QueryWrapper<TopoEdge> edgeQuery = Wrappers.query();
                edgeQuery.eq("EDGE_SOURCE", nodeId);
                edgeQuery.eq("ORG_ID", vertex.getOrgId());
                TopoEdge topoEdge = topoEdgeService.getOne(edgeQuery);
                if (Objects.isNull(topoEdge)) {
                    edgeQuery = Wrappers.query();
                    edgeQuery.eq("EDGE_TARGET", nodeId);
                    edgeQuery.eq("ORG_ID", vertex.getOrgId());
                    topoEdge = topoEdgeService.getOne(edgeQuery);
                    if (Objects.isNull(topoEdge)) continue;
                    peerNodeId = topoEdge.getEdgeSource();
                } else {
                    peerNodeId = topoEdge.getEdgeTarget();
                }
                // 3、根据连线的node_id查找对端
                vertexQuery = Wrappers.query();
                vertexQuery.eq("NODE_ID", peerNodeId);
                vertexQuery.eq("NODE_TYPE", "net_topo");
                vertexQuery.eq("ORG_ID", vertex.getOrgId());
                vertexQuery.eq("IS_PORT", "1");
//                vertexQuery.isNotNull("PORT_INDEX");
                vertex = topoVertexService.getOne(vertexQuery);
                if (Objects.nonNull(vertex)) {
                    linkAsset.setLinkAssetId(vertex.getAssetId());
                    linkAsset.setLinkPort(vertex.getPortIndex());
                    // 4、根据assetId查找设备信息
                    Asset asset = assetService.getById(vertex.getAssetId());
                    if (Objects.isNull(asset)) {
                        continue;
                    }
                    linkAsset.setLinkAssetIp(asset.getIp());
                    linkAsset.setLinkAssetName(asset.getName());
                    linkAsset.setMaOrAt(AssetLinkConst.TOPO_SET);
                }
            }
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ASSET_LINK_ASSET, asetId, e);
            return finalList;
        }

        return finalList;
    }

    @Override
    public AssetLinkAsset findLinkAssetByAsset(String assetId, String portIndex) {
        QueryWrapper<AssetLinkAsset> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("asset_id", assetId);
        queryWrapper.eq("port_index", portIndex);
        queryWrapper.orderByDesc("CREATE_TIME");
        List<AssetLinkAsset> list = this.list(queryWrapper);
        if (CollectionUtil.isNotEmpty(list)) {
            return list.get(0);
        }
        return linkAssetMapper.selectOne(queryWrapper);
    }

    @Override
    public AssetLinkAsset findAssetByLinkAsset(String assetId, String linkAssetIp) {
        QueryWrapper<AssetLinkAsset> queryWrapper = new QueryWrapper<AssetLinkAsset>();
        queryWrapper.eq("link_asset_id", assetId);
        queryWrapper.eq("link_asset_ip", linkAssetIp);
        List<AssetLinkAsset> assetLinkAssets = linkAssetMapper.selectList(queryWrapper);
        if (assetLinkAssets.isEmpty()) {
            return null;
        }
        return assetLinkAssets.get(0);
    }

    @Override
    public String formatMsg(String assetId, String atIp) {
        StringBuilder resp = new StringBuilder("");
        if (StrUtil.isNotEmpty(assetId)) {
            Asset asset = assetService.getById(assetId);
            if (Objects.nonNull(asset)) {
                resp.append("对端设备：【");
                resp.append(asset.getName());
                resp.append(asset.getIp());
                resp.append("】");
            }
        }
        if (StringUtils.isNotEmpty(atIp)) {
            resp.append("对端IP【");
            resp.append(atIp);
            resp.append("】");
        }

        return resp.toString();
    }

    /**
     * 保存当前设备对端信息
     *
     * @param assetId
     */
    @Override
    public void saveThisAsset(String assetId) {
        List<AssetLinkAsset> finalList = this.getFinalList(assetId);

        this.savetopoSet(finalList);
    }

    private AssetLinkAsset setAtLinkAsset(AssetLinkAssetVo atAsset) {
        AssetLinkAsset linkAsset = new AssetLinkAsset();
        try {
            BeanUtils.copyProperties(linkAsset, atAsset);
            String atAssetId = atAsset.getAtAssetId();
            linkAsset.setLinkAssetId(atAssetId);
            linkAsset.setLinkAssetName(atAsset.getAtName());
            linkAsset.setLinkAssetIp(atAsset.getAtNetAddress());
            linkAsset.setLinkPort(atAsset.getAtPortIndexName());
            linkAsset.setMaOrAt(AssetLinkConst.AUTO_SNIFFER);
            if (Objects.nonNull(atAssetId)) {
                Asset one = assetService.getById(atAssetId);
                if (Objects.nonNull(one)) {
                    linkAsset.setLinkAssetName(one.getName());
                    linkAsset.setLinkAssetIp(one.getIp());
                }

                String atPhyAddress = atAsset.getAtPhysAddress();
                if (Objects.nonNull(atPhyAddress)) {
                    QueryWrapper<CollectNetworkCard> query = Wrappers.query();
                    query.eq("ASSET_ID", atAssetId);
                    query.eq("MAC_ADDRESS", atPhyAddress);
                    List<CollectNetworkCard> networkCards = networkCardService.list(query);
                    if (Objects.nonNull(networkCards) && !networkCards.isEmpty()) {
                        linkAsset.setLinkPort(networkCards.get(0).getName());
                        linkAsset.setLinkAssetIp(networkCards.get(0).getIp());
                    }
                }

            }
        } catch (Exception e) {
            log.error("设置对端信息错误，参数：" + JSONUtil.toJsonStr(atAsset), e);
        }

        return linkAsset;
    }


    @Override
    public List<ThreeDLinkReq> getThreeDLink(String roomId1, String roomId2) {
        return linkAssetMapper.getThreeDLink(roomId1, roomId2);
    }

    @Override
    public List<LinkAssetExportVo> exportManualList() {
        return linkAssetMapper.exportManualList();
    }
}
