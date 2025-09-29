package com.jcca.admin.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.ImportCabinet;
import com.jcca.web.asset.entity.Room;
import org.apache.ibatis.annotations.Param;

/**
 * @ Author：sophia
 * @ Date：Created in 11:20 2021/8/12
 * @ Description:
 */
public interface ImportCabinetMapper extends BaseMapper<ImportCabinet> {
    void deleteAll();


    /*通过组织机房名称 确认机房 */
    Room getRoom(@Param("orgName") String orgName, @Param("roomName") String roomName);

}
