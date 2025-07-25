package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SysDict;
import com.jcca.common.enums.StatusEnum;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-06 12:14:00
 **/
public interface SysDictService extends IService<SysDict> {


    /**
     * 根据字典标识查询是否重复
     *
     * @param dict
     * @return
     */
    boolean repeatByName(SysDict dict);

    /**
     * 根据字典标识获取状态正常的字典
     *
     * @param name
     * @return
     */
    SysDict getByNameOk(String name);

    /**
     * 根据字典ID更新字典状态
     *
     * @param statusEnum
     * @param ids
     * @return
     */
    boolean updateStatus(StatusEnum statusEnum, List<String> ids);

    /**
     * 获取全部的列表
     * @param name
     * @return
     */
    List<SysDict> getAllLikeName(String name);

    /**
     * 通过NAME查title
     *
     * @param name
     * @return
     */
    SysDict getByName(String name);

    /**
     * 通过name名模糊查询
     *
     * @param name
     * @return
     */
    List<SysDict> listByNames(String name);
}
