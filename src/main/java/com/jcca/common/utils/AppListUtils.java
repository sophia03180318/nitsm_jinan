package com.jcca.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcca.web.asset.entity.Asset;

import java.util.ArrayList;
import java.util.List;

public class AppListUtils {

    public static List<List<String>> inSplit(List<String> resources, Integer size) {
        //统一改 达梦支持600以内
        size = 600;
        List<List<String>> resp = new ArrayList<List<String>>();
        List<String> itemList = new ArrayList<String>();
        for (String item : resources) {
            itemList.add(item);
            if (itemList.size() == size) {
                resp.add(itemList);
                itemList = new ArrayList<String>();
            }
        }
        if (!itemList.isEmpty()) {
            resp.add(itemList);
        }


        return resp;
    }

    public static List<List<Asset>> inSplitAsset(List<Asset> resources, Integer size) {
        List<List<Asset>> resp = new ArrayList<List<Asset>>();
        List<Asset> itemList = new ArrayList<Asset>();
        for (Asset item : resources) {
            itemList.add(item);
            if (itemList.size() == size) {
                resp.add(itemList);
                itemList = new ArrayList<Asset>();
            }
        }
        if (!itemList.isEmpty()) {
            resp.add(itemList);
        }


        return resp;
    }

    /**
     * 分页方法，同时返回分页后的列表和总页数
     *
     * @param list       原始列表
     * @param pageNumber 当前页码（从1开始）
     * @param pageSize   每页显示的记录数
     * @param <T>        列表元素的类型
     * @return 包含分页后的列表和总页数的结果
     */
    public static IPage pageList(List list, int pageNumber, int pageSize) {
        IPage pageInfo = new Page();
        pageInfo.setCurrent(pageNumber);
        if (list == null || list.isEmpty() || pageNumber <= 0 || pageSize <= 0) {
            pageInfo.setSize(0);
            pageInfo.setTotal(0);
            pageInfo.setRecords(new ArrayList());
            return pageInfo;
        }


        int totalItems = list.size();
        // 计算总页数
        int totalPages = (totalItems + pageSize - 1) / pageSize;

        int fromIndex = (pageNumber - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        if (toIndex < fromIndex) {
            List result = new ArrayList<>();
            pageInfo.setRecords(result);
        } else {
            List result = new ArrayList<>(list.subList(fromIndex, toIndex));
            pageInfo.setRecords(result);
        }

        pageInfo.setTotal(totalItems);
        pageInfo.setSize(totalPages);

        return pageInfo;
    }

}
