package com.wskh.solver;

import com.wskh.classes.Item;
import com.wskh.classes.Parameter;
import com.wskh.classes.Pattern;
import com.wskh.utils.CheckUtil;

import java.util.*;

public class DpPricingSolver {
    public static Pattern solve(int C, List<Item> itemList, double[] itemDualValues) {

        if (itemList.isEmpty()) return null;

        // 创建dp数组
        int n = itemList.size();  // 获取物品的数量
        double[][] dp = new double[n + 1][C + 1];
        boolean[][] itemSelection = new boolean[n][C + 1];

//        // j = 0
//        Item item = itemList.get(0);
//        for (int c = 0; c <= C; c++) {
//            dp[0][c] = (c > item.fragile || item.w > c) ? 0d : itemDualValues[0];
//        }

        // 动态规划填表过程
        for (int j = 1; j <= n; j++) {
            Item itemJ = itemList.get(j - 1);
            for (int c = 0; c <= C; c++) {
                if (c > itemJ.fragile || itemJ.w > c) {
                    dp[j][c] = dp[j - 1][c];
                } else {
                    if (dp[j - 1][c - itemJ.w] + itemDualValues[j - 1] > dp[j - 1][c]) {
                        dp[j][c] = dp[j - 1][c - itemJ.w] + itemDualValues[j - 1];
                        itemSelection[j - 1][c] = true;
                    } else {
                        dp[j][c] = dp[j - 1][c];
                    }
                }
            }
        }

        double bestValue = 0;
        int bestC = -1;

//        for (Item item : itemList) {
//            int c = item.fragile;
//            if (dp[n][c] != null && bestValue < dp[n][c]) {
//                bestC = c;
//                bestValue = dp[n][c];
//            }
//        }

        for (int j = 1; j <= n; j++) {
            for (int c = C; c >= 1; c--) {
                if (bestValue < dp[j][c]) {
                    bestC = c;
                    bestValue = dp[j][c];
                }
            }
        }

        if (bestC != -1) {
            double reducedCost = 1 - bestValue;
            if (reducedCost < -Parameter.EPS) {
                Pattern pattern = new Pattern();
                pattern.reducedCost = reducedCost;
                pattern.packedItemIndexList = new LinkedList<>();
                for (int i = itemList.size() - 1; i >= 0 && bestC > 0; i--) {
                    if (itemSelection[i][bestC]) {
                        pattern.packedItemIndexList.addFirst(i);
                        // 选择了物品i
                        bestC -= itemList.get(i).w; // 减少背包容量
                    }
                }
                pattern.genKey();
                CheckUtil.checkPattern(pattern, itemList, itemDualValues);
                return pattern;
            }
        }

        return null;
    }
}
