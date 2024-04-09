package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.common.bean.ResultVo;
import com.jcca.web.asset.entity.AssetImportRecord;
import com.jcca.web.asset.vo.AssetInfoVo;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 11:28 2021/7/8
 * @ Description:
 */

public interface AssetImportService extends IService<AssetImportRecord> {

    /*获取所有失败资产*/
    List<AssetImportRecord> selectAllAsset();

    /*清空数据*/
    void deleteAllAsset();

    /*获取现有条数*/
    int countAll();

    void saveErrorAsset(AssetImportRecord assetImportRecord);

    /*判断模板是否满足导入基本条件*/
    ResultVo estimateTemplateEmpty(List<Object> headerList);

    /*
     *根据名称判断是否满足三层组织关系(满足的同时赋值)
     * */
    AssetInfoVo estimateTreble(String orgName, String roomName, String cabinetName);

    /*
     * 根据名称判断是否满足组织和机房层级关系(满足同时赋值)
     * */
    AssetInfoVo estimateDouble(String orgName, String roomName);

    /*
     * 根据名称判断是否满足类型和型号的层级关系(满足同时赋值)
     * */
    boolean estimateModeAndImage(int assetMode, String assetImage);

/*

     // 判断对象必要条件是否为空

    AssetInfoVo estimateIndispensable(Map<String, Object> rowMap);


     // 根据指定字典表 判断字典中字段是否为空

    AssetInfoVo estimateEmpty(Map<String, Object> rowMap,String assetMode);*/

}
