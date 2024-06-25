package com.wskh.utils;

import com.wskh.classes.*;

import java.util.List;

public class CheckUtil {

    public static void checkBinList(List<Bin> binList, Instance instance) {
        boolean[] used = new boolean[instance.items.length];
        for (Bin bin : binList) {
            int minF = Integer.MAX_VALUE;
            int totalW = 0;
            for (Integer i : bin.itemIndexList) {
                if (used[i]) {
                    throw new RuntimeException("出现重复的物品: " + i);
                }
                used[i] = true;
                minF = Math.min(minF, instance.items[i].fragile);
                totalW += instance.items[i].w;
            }
            if (totalW > minF) {
                throw new RuntimeException("超出容量: " + totalW + " > " + minF);
            }
            if (minF != bin.minFragile) {
                throw new RuntimeException("minFragile数据对不上");
            }
            if (totalW != bin.totalW) {
                throw new RuntimeException("totalW数据对不上");
            }
        }
        for (int i = 0; i < used.length; i++) {
            if (!used[i]) {
                throw new RuntimeException("缺少物品: " + i);
            }
        }
    }

    public static void checkSolution(BpSolution bpSolution, Instance instance) {
        checkBinList(bpSolution.binList, instance);
    }

    public static void checkPattern(Pattern pattern, List<Item> itemList, double[] dualValues) {
        double reducedCost = 1;
        int minF = Integer.MAX_VALUE;
        int totalW = 0;
        for (Integer i : pattern.packedItemIndexList) {
            Item item = itemList.get(i);
            totalW += item.w;
            minF = Math.min(minF, item.fragile);
            reducedCost -= dualValues[i];
        }
        if (CommonUtil.compareDouble(reducedCost, pattern.reducedCost) != 0) {
            throw new RuntimeException("reducedCost计算有误: 正确应该是: " + reducedCost + " , 错误的是: " + pattern.reducedCost);
        }
        if (totalW > minF) {
            throw new RuntimeException("超出容量: " + totalW + " > " + minF);
        }
    }

}
