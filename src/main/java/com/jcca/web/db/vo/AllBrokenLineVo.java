package com.jcca.web.db.vo;

import lombok.Data;

import java.util.List;

/**
 * 所有的折线图
 *
 * @author Lvyp
 */
@Data
public class AllBrokenLineVo {

    /**
     * 锁等待率
     */
    private List<BrokenLineVo> lockWaitList;
    /**
     * 锁使用率
     */
    private List<BrokenLineVo> lockUsedList;
    /**
     * 共享池命中率
     */
    private List<BrokenLineVo> sharePoolList;
    /**
     * 缓存锁
     */
    private List<BrokenLineVo> cacheList;
    /**
     * 繁忙率
     */
    private List<BrokenLineVo> busynessList;

}
