package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.entity.PromptInfo;
import com.jcca.web2.vo.ProcessPlateVo;

import java.util.List;

/**
 * 进程阈值
 *
 * @author syt
 */
public interface PromptInfoService extends IService<PromptInfo> {

    /*
     * 根据字典名称获取提示信息
     * @Param [dictName]
     * @return java.util.List<java.lang.String>
     * @Author syt
     * @Date 2021/10/27 14:50
     */
    List<String> getPromptByDictName(String dictName, String type);

    /*
     * 查询配置过的进程
     * @Param [key]
     * @return java.util.List<java.lang.String>
     * @Author syt
     * @Date 2021/10/27 17:40
     */
    List<String> getExistPrompt(String key);

    /**
     * @description: 添加进程模板
     * @author: HanHW
     * @date: 2023/11/10 15:34
     * @param: [vo]
     * @return: void
     **/
    void addPlateV2(ProcessPlateVo vo);

    /**
     * @description: 根据业务类型查找进程模板
     * @author: HanHW
     * @date: 2023/11/13 15:30
     * @param: [softwareTypeId]
     * @return: java.util.List<com.jcca.web2.vo.ProcessPlateVo>
     **/
    List<ProcessPlateVo> findBySoftTypeIdV2(String softwareTypeId);

    /**
     * 删除业务类型
     *
     * @param id ID
     */
    void removePrompt(String id);
}
