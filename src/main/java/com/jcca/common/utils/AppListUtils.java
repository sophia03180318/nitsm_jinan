package com.jcca.common.utils;

import com.jcca.web.asset.entity.Asset;

import java.util.ArrayList;
import java.util.List;

public class AppListUtils {

    public static List<List<String>> inSplit(List<String> resources, Integer size) {
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

}
