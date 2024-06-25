package com.wskh.solver.upper_bound;

import com.wskh.classes.Bin;
import com.wskh.classes.Instance;
import com.wskh.classes.Item;
import com.wskh.utils.CheckUtil;
import com.wskh.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class DpBasedUpperBoundSolver {
    public static List<Bin> solve(Instance instance) {
        List<Bin> binList = new ArrayList<>();

        int[] sum = new int[instance.items.length];
        int[] fragile = new int[instance.items.length];
        for (int i = 0; i < instance.items.length; i++) {
            sum[i] = 0;
            fragile[i] = Integer.MAX_VALUE;
        }
        boolean[] visited = new boolean[instance.items.length];
        double[][] val = new double[instance.items.length + 1][];
        for (int i = 0; i < instance.items.length; i++) {
            if (visited[i]) {
                continue;
            }
            ArrayList<Integer> leftItem = new ArrayList<Integer>();
            for (int k = i + 1; k < instance.items.length; k++) {
                if (!visited[k]) {
                    leftItem.add(k);
                }
            }
            val[leftItem.size()] = new double[instance.items[i].fragile + 1];
            val[leftItem.size()][0] = 1.0;
            boolean[][] sel = new boolean[leftItem.size() + 1][instance.items[i].fragile + 1];
            for (int k = leftItem.size() - 1; k >= 0; k--) {
                val[k] = val[k + 1].clone();
                int j = leftItem.get(k);
                for (int p = instance.items[i].fragile - instance.items[i].w - instance.items[j].w; p >= 0; p--) {
                    if (val[k][p] != 0 && val[k][p] + 1.0 * instance.items[j].w / instance.items[j].fragile >= val[k][p + instance.items[j].w]) {
                        val[k][p + instance.items[j].w] = val[k][p] + 1.0 * instance.items[j].w / instance.items[j].fragile;
                        sel[k][p + instance.items[j].w] = true;
                    }
                }
            }
            int selCap = 0;
            for (int p = 0; p <= instance.items[i].fragile; p++) {
                if (val[0][p] >= val[0][selCap]) {
                    selCap = p;
                }
            }
            ArrayList<Integer> selItem = new ArrayList<Integer>();
            selItem.add(i);
            int k = 0;
            while (selCap > 0 && k < leftItem.size()) {
                if (sel[k][selCap]) {
                    selItem.add(leftItem.get(k));
                    selCap -= instance.items[leftItem.get(k)].w;
                }
                k++;
            }
            Bin bin = new Bin();
            for (k = 0; k < selItem.size(); k++) {
                bin.totalW += instance.items[selItem.get(k)].w;
                bin.minFragile = Math.min(instance.items[selItem.get(k)].fragile, bin.minFragile);
                bin.itemIndexList.add(selItem.get(k));
                visited[selItem.get(k)] = true;
            }
            binList.add(bin);
        }

        CheckUtil.checkBinList(binList, instance);

        return binList;
    }
}
