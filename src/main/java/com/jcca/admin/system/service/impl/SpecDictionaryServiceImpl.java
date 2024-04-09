package com.jcca.admin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.SpecDictionaryMapper;
import com.jcca.admin.system.entity.SpecDictionary;
import com.jcca.admin.system.service.SpecDictionaryService;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web2.vo.CollectConfigVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @ Author：sophia
 * @ Date：Created in 9:59 2021/8/12
 * @ Description:
 */
@Service
public class SpecDictionaryServiceImpl extends ServiceImpl<SpecDictionaryMapper, SpecDictionary> implements SpecDictionaryService {
    @Resource
    private SpecDictionaryMapper specDictionaryMapper;

    /**
     * @description: 查找资产采集指标字典
     * @author: HanHW
     * @date: 2023/11/16 14:29
     * @param: [asset]
     * @return: com.jcca.admin.system.entity.SpecDictionary
     */
    @Override
    public SpecDictionary queryDictByAsset(Asset asset) {

        return specDictionaryMapper.queryDictByAsset(asset);
    }

    /**
     * 创建变量
     *
     * @param vo     CollectConfigVo
     * @param specId 执行代码
     * @return
     */
    @Override
    public SpecDictionary getSpecDict(CollectConfigVo vo, Integer specId) {
        SpecDictionary dictionary = new SpecDictionary();
        dictionary.setId(MyIdUtil.getId());
        dictionary.setAssetMode(vo.getAssetMode());
        dictionary.setAssetMode(vo.getAssetMode());
        dictionary.setManufacturerId(vo.getManufactureId());
        dictionary.setSystemType(vo.getSystemType());
        dictionary.setSpecId(specId);
        dictionary.setCreateDate(new Date());

        return dictionary;
    }

/*    @Override
    public SpecDictionary tranfromSDV(SpecDictionary specDictionary) {

        specDictionary.setMySpecId(DictUtil.keyValue("SPEC_ID", specDictionary.getSpecId().toString()));
        specDictionary.setMyAssetMode(DictUtil.keyValue("ASSET_MODE", specDictionary.getAssetMode().toString()));
        specDictionary.setMyManufacturerId(DictUtil.keyValue("ASSET_FACTORY", specDictionary.getManufacturerId()));
        specDictionary.setMySystemType(DictUtil.keyValue("SYSTEM_TYPE", specDictionary.getManufacturerId()));

        return specDictionary;

    }*/
}
