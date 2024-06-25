package com.wskh.utils;

import com.wskh.classes.*;

import java.util.List;

public class CommonUtil {

    public static int compareDouble(double a, double b) {
        double diff = a - b;
        if (diff > Parameter.EPS) {
            return 1;
        }
        if (diff < -Parameter.EPS) {
            return -1;
        }
        return 0;
    }

    public static int ceilToInt(double x) {
        return (int) Math.ceil(x - Parameter.EPS);
    }

    public static void dpSolveKP01(Bin bin, int bagWeight, List<Item> itemList) {
        int wLen = itemList.size();
        // dp[j] 表示背包容量为j时可以获得的最大价值
        int[] dp = new int[bagWeight + 1];
        // itemSelection[i][j] 用来记录在容量为j时选择物品i是否可以获得最大价值
        boolean[][] itemSelection = new boolean[wLen][bagWeight + 1];

        // 遍历所有物品
        for (int i = 0; i < wLen; i++) {
            // 对每个物品，逆向遍历背包容量以保证每个物品只被计算一次
            Item item = itemList.get(i);
            int weight = item.w;
            int value = weight;
            for (int j = bagWeight; j >= weight; j--) {
                // 如果选择当前物品后价值增加，则更新dp[j]和itemSelection记录
                if (dp[j] < dp[j - weight] + value) {
                    dp[j] = dp[j - weight] + value;
                    itemSelection[i][j] = true; // 记录在当前背包容量下，选择了这个物品
                }
            }
        }

        int capacity = bagWeight;
        boolean[] used = new boolean[wLen];
        for (int i = wLen - 1; i >= 0 && capacity > 0; i--) {
            // 检查在当前容量下是否有记录选择当前物品
            if (!used[i] && itemSelection[i][capacity]) {
                used[i] = true;
                Item item = itemList.get(i);
                capacity -= item.w; // 选择了这个物品后，减少相应的背包容量
                i = wLen; // 重置循环变量，重新从最后一个物品开始检查，以确保不遗漏任何组合
                bin.itemIndexList.add(item.index);
                bin.totalW += item.w;
            }
        }
    }

}
