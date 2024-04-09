package com.jcca.admin.system.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.ImportCabinetMapper;
import com.jcca.admin.system.entity.ImportCabinet;
import com.jcca.admin.system.service.ImportCabinetService;
import com.jcca.web.asset.entity.Room;
import com.jcca.web.asset.utils.NullFieldException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ Author：sophia
 * @ Date：Created in 11:24 2021/8/12
 * @ Description:
 */
@Service
public class ImportCabinetServiceImpl extends ServiceImpl<ImportCabinetMapper, ImportCabinet> implements ImportCabinetService {

    @Resource
    private ImportCabinetMapper importCabinetMapper;

    @Override
    public void deleteAll() {
        importCabinetMapper.deleteAll();
    }

    @Override
    public String getRoomID(String org_name, String room_name) throws NullFieldException {
        Room room = importCabinetMapper.getRoom(org_name, room_name);
        if (ObjectUtil.isNull(room)) {
            throw new NullFieldException("未找到" + org_name + "下的" + room_name);
        }
        return room.getId();
    }
}
