package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.TopoPcb;

/**
 * @description: 自定义板卡
 * @author: Lvyp
 * @create: 2024/01/10 14:07
 */
public interface TopoPcbService extends IService<TopoPcb> {

    /**
     * 删除自定义板卡
     * @param pcb
     */
    void removeEntity(TopoPcb pcb);
}
