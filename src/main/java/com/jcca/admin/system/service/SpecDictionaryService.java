package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SpecDictionary;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web2.vo.CollectConfigVo;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 9:58 2021/8/12
 * @ Description:
 */
public interface SpecDictionaryService extends IService<SpecDictionary> {


    /**
     * @description: 查找资产采集指标字典
     * @author: HanHW
     * @date: 2023/11/16 14:29
     * @param: [asset]
     * @return: com.jcca.admin.system.entity.SpecDictionary
     **/
    SpecDictionary queryDictByAsset(Asset asset);

    /**
     * 创建变量
     *
     * @param vo     CollectConfigVo
     * @param specId 执行代码
     * @return
     */
    SpecDictionary getSpecDict(CollectConfigVo vo, Integer specId);

    /**
     * @description: 根据厂商ID查询
     * @author: HanHW
     * @date: 2023/11/16 14:29
     * @param: [menufactureId]
     * @return: java.util.List<com.jcca.admin.system.entity.SpecDictionary>
     */
    List<SpecDictionary> findByManufacturerId(String menufactureId);
}
