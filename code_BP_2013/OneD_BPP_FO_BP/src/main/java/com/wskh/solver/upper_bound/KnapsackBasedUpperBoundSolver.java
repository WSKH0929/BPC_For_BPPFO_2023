package com.wskh.solver.upper_bound;

import com.wskh.classes.Bin;
import com.wskh.classes.Instance;
import com.wskh.classes.Item;
import com.wskh.utils.CheckUtil;
import com.wskh.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;


public class KnapsackBasedUpperBoundSolver {

    public static List<Bin> solve(Instance instance) {
        List<Bin> binList = new ArrayList<>();
        boolean[] used = new boolean[instance.items.length];
        while (true) {
            // 找到最小易碎性的物品（由于之前排序好了，所以第一个未打包的物品就是目标）
            int j = -1;
            for (int i = 0; i < instance.items.length; i++) {
                if (!used[i]) {
                    j = i;
                    break;
                }
            }
            if (j == -1) break;
            List<Item> itemList = new ArrayList<>();
            int C = instance.items[j].fragile - instance.items[j].w;
            for (int i = j + 1; i < instance.items.length; i++) {
                if (!used[i] && instance.items[i].w <= C) {
                    itemList.add(instance.items[i]);
                }
            }
            Bin bin = new Bin();
            bin.totalW = instance.items[j].w;
            bin.minFragile = instance.items[j].fragile;
            bin.itemIndexList.add(j);
            if (!itemList.isEmpty()) {
                CommonUtil.dpSolveKP01(bin, C, itemList);
            }
            for (Integer i : bin.itemIndexList) {
                used[i] = true;
            }
            binList.add(bin);
        }

        CheckUtil.checkBinList(binList, instance);

        return binList;
    }

}
