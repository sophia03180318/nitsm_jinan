package com.jcca.web.test.dicovery;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.service.CollectRouteService;
import com.jcca.web.collect.service.CollectTopoDiscoveryService;
import com.jcca.web.collect.service.bean.TopoRouteReturnVo;
import com.jcca.web.collect.service.bean.TopoRouteTarget;
import com.jcca.web.collect.service.bean.TopoRouteVo;
import com.jcca.web.collect.service.impl.topoDiscovery.CollectTopoDiscoveryHandlerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class discoveryTest {
    @Resource
    private CollectTopoDiscoveryService collectTopoDiscoveryService;
    @Resource
    private CollectTopoDiscoveryHandlerService collectTopoDiscoveryHandlerService;
    @Resource
    private AssetService assetServ;
    @Resource
    private CollectRouteService routeServ;


    @Test
    public void testAssetArp() {
       Asset asset=new Asset();
        asset.setOsUser("cisco");
       asset.setIp("192.168.1.1");
        collectTopoDiscoveryService.arpInfo(asset,null);
    }
    @Test
    public void testAssetCDP() {
        Asset asset=new Asset();
        asset.setOsUser("cisco");
        asset.setIp("192.168.1.1");
        collectTopoDiscoveryService.cdpInfo(asset);
    }

    @Test
    public void testAssetBridge() {
        Asset asset=new Asset();
        asset.setOsUser("cisco");
        asset.setIp("192.168.61.2");
        collectTopoDiscoveryService.bridgeInfo(asset,null);
    }


    @Test
    public void testAssetRoute() {
        Asset asset=new Asset();
        asset.setOsUser("public");
        asset.setIp("192.168.79.2");
        collectTopoDiscoveryService.routeInfo(asset);
    }

    @Test
    public void testAssetVlan() {
        Asset asset=new Asset();
        asset.setOsUser("cisco");
        asset.setIp("192.168.1.1");
        collectTopoDiscoveryService.vlanInfo(asset);
    }

    @Test
    public void getAllNetAssetInfo() {

        collectTopoDiscoveryHandlerService.getAllNetAssetInfo();
        List<TopoRouteVo> list= routeServ.queryTopoRoute("2022051015190638400000005");
         List<TopoRouteReturnVo> topoRouteReturnVos=new ArrayList<>();
        TopoRouteReturnVo topoRouteVoLast=null;
        for(TopoRouteVo topoRouteVo:list){
            if(topoRouteVoLast!=null&&topoRouteVoLast.getPortIndexRank().equals(topoRouteVo.getPortIndexRank())){
                if(topoRouteVo.getAtNetAddress()!=null&&!"".equals(topoRouteVo.getAtNetAddress())){
                    TopoRouteTarget topoRouteTarget= new TopoRouteTarget();
                    topoRouteTarget.setAtNetAddress(topoRouteVo.getAtNetAddress());
                    topoRouteTarget.setAtPhysAddress(topoRouteVo.getAtPhysAddress());
                    topoRouteTarget.setAtAssetId(topoRouteVo.getAtAssetId());
                    topoRouteTarget.setAtName(topoRouteVo.getAtName());
                    topoRouteTarget.setAtPortName(topoRouteVo.getPortName());
                    topoRouteTarget.setAtPortIndexName(topoRouteVo.getAtPortIndexName());
                    topoRouteTarget.setAtType(topoRouteVo.getAtType());
                    if(topoRouteVoLast.getTargetList()==null){
                        List<TopoRouteTarget> targetList=new ArrayList<>();
                        targetList.add(topoRouteTarget);
                        topoRouteVoLast.setTargetList(targetList);
                    }else{
                        topoRouteVoLast.getTargetList().add(topoRouteTarget);
                    }
                }
            }else{
                TopoRouteReturnVo topoRouteReturnVo=new TopoRouteReturnVo();
                topoRouteReturnVo.setId(topoRouteVo.getId());
                topoRouteReturnVo.setAssetId(topoRouteVo.getAssetId());
                topoRouteReturnVo.setPortIndexRank(topoRouteVo.getPortIndexRank());
                topoRouteReturnVo.setPortSendNum(topoRouteVo.getPortSendNum());
                topoRouteReturnVo.setPortReceiveNum(topoRouteVo.getPortReceiveNum());
                topoRouteReturnVo.setPortOrgName(topoRouteVo.getPortOrgName());
                topoRouteReturnVo.setPortName(topoRouteVo.getPortName());
                topoRouteReturnVo.setPortIp(topoRouteVo.getPortIp());
                topoRouteReturnVo.setPortMacAddress(topoRouteVo.getPortMacAddress());
                topoRouteReturnVo.setPortIndexName(topoRouteVo.getAtPortIndexName());
                topoRouteReturnVo.setRemark(topoRouteVo.getRemark());
                if(topoRouteVo.getAtNetAddress()!=null&&!"".equals(topoRouteVo.getAtNetAddress())){
                    TopoRouteTarget topoRouteTarget= new TopoRouteTarget();
                    topoRouteTarget.setAtNetAddress(topoRouteVo.getAtNetAddress());
                    topoRouteTarget.setAtPhysAddress(topoRouteVo.getAtPhysAddress());
                    topoRouteTarget.setAtAssetId(topoRouteVo.getAtAssetId());
                    topoRouteTarget.setAtName(topoRouteVo.getAtName());
                    topoRouteTarget.setAtPortName(topoRouteVo.getPortName());
                    topoRouteTarget.setAtPortIndexName(topoRouteVo.getAtPortIndexName());
                    topoRouteTarget.setAtType(topoRouteVo.getAtType());
                    if(topoRouteReturnVo.getTargetList()==null){
                        List<TopoRouteTarget> targetList=new ArrayList<>();
                        targetList.add(topoRouteTarget);
                        topoRouteReturnVo.setTargetList(targetList);
                    }else{
                        topoRouteReturnVo.getTargetList().add(topoRouteTarget);
                    }
                }
                topoRouteVoLast=topoRouteReturnVo;
                topoRouteReturnVos.add(topoRouteReturnVo);
            }




        }
        System.out.println(list);
    }

    @Test
    public void getCollectRoute() {

        QueryWrapper<Asset> queryWrapper = new QueryWrapper<Asset>();
        queryWrapper.eq("IS_DEL",1);
        queryWrapper.in("ASSET_MODE", Arrays.asList(42,201));
        List<Asset> list = assetServ.list(queryWrapper);

        for (Asset asset : list) {
            routeServ.collectRoute(asset);
        }
    }
}
