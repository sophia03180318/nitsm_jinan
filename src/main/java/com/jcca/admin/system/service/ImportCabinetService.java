package com.jcca.admin.system.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.ImportCabinet;
import com.jcca.web.asset.utils.NullFieldException;

/**
 * @ Author：sophia
 * @ Date：Created in 11:22 2021/8/12
 * @ Description:
 */

public interface ImportCabinetService extends IService<ImportCabinet> {
    /*清空所有数据*/
    void deleteAll();

    String getRoomID(String org_name, String room_name) throws NullFieldException;
}
