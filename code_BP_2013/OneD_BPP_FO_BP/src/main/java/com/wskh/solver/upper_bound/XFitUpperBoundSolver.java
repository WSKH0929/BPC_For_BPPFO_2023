package com.wskh.solver.upper_bound;

import com.wskh.classes.Bin;
import com.wskh.classes.Instance;
import com.wskh.classes.Item;
import com.wskh.utils.CheckUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class XFitUpperBoundSolver {

    // 将物品按给定的顺序摆放。将当前物品放入编号最小的箱子中。如果它不适合任何打开的箱子，则打开一个新箱子并将当前物品打包到新箱子中
    private static List<Bin> firstFit(Item[] items) {
        List<Bin> binList = new ArrayList<>();
        for (Item item : items) {
            boolean packed = false;
            for (Bin bin : binList) {
                int mf = Math.min(bin.minFragile, item.fragile);
                if (mf - bin.totalW >= item.w) {
                    bin.minFragile = mf;
                    bin.totalW += item.w;
                    bin.itemIndexList.add(item.index);
                    packed = true;
                    break;
                }
            }
            if (!packed) {
                Bin bin = new Bin();
                bin.totalW = item.w;
                bin.minFragile = item.fragile;
                bin.itemIndexList.add(item.index);
                binList.add(bin);
            }
        }
        return binList;
    }

    // 将物品按给定的顺序摆放。将当前物品打包到该物品打包后未使用容量最小的箱子中。如果它不适合任何箱子，则打开一个新箱子并将当前物品打包到新箱子中
    private static List<Bin> bestFit(Item[] items) {
        List<Bin> binList = new ArrayList<>();
        for (Item item : items) {
            int bestBinIndex = -1;
            int bestRemainC = 0;
            for (int i = 0; i < binList.size(); i++) {
                Bin bin = binList.get(i);
                int mf = Math.min(bin.minFragile, item.fragile);
                int remainC = mf - bin.totalW - item.w;
                if (remainC >= 0) {
                    if (bestBinIndex == -1 || bestRemainC > remainC) {
                        bestBinIndex = i;
                        bestRemainC = remainC;
                    }
                }
            }
            if (bestBinIndex == -1) {
                Bin bin = new Bin();
                bin.totalW = item.w;
                bin.minFragile = item.fragile;
                bin.itemIndexList.add(item.index);
                binList.add(bin);
            } else {
                Bin bin = binList.get(bestBinIndex);
                bin.minFragile = Math.min(bin.minFragile, item.fragile);
                bin.totalW += item.w;
                bin.itemIndexList.add(item.index);
            }
        }
        return binList;
    }

    // 将物品按给定的顺序摆放。将当前物品放入未使用容量最大的打开的箱子中。如果它不适合任何打开的箱子，则打开一个新箱子，并将当前物品打包到新箱子中
    private static List<Bin> worstFit(Item[] items) {
        List<Bin> binList = new ArrayList<>();
        for (Item item : items) {
            int bestBinIndex = -1;
            int bestRemainC = 0;
            for (int i = 0; i < binList.size(); i++) {
                Bin bin = binList.get(i);
                int mf = Math.min(bin.minFragile, item.fragile);
                int remainC = mf - bin.totalW - item.w;
                if (remainC >= 0) {
                    if (bestBinIndex == -1 || bestRemainC < remainC) {
                        bestBinIndex = i;
                        bestRemainC = remainC;
                    }
                }
            }
            if (bestBinIndex == -1) {
                Bin bin = new Bin();
                bin.totalW = item.w;
                bin.minFragile = item.fragile;
                bin.itemIndexList.add(item.index);
                binList.add(bin);
            } else {
                Bin bin = binList.get(bestBinIndex);
                bin.minFragile = Math.min(bin.minFragile, item.fragile);
                bin.totalW += item.w;
                bin.itemIndexList.add(item.index);
            }
        }
        return binList;
    }

    // 将物品按给定的顺序摆放。如果适合，则将当前物品放入当前打开的箱子中。否则，关闭该箱子，打开一个新箱子，并将当前物品打包到新箱子中
    private static List<Bin> nextFit(Item[] items) {
        List<Bin> binList = new ArrayList<>();
        for (Item item : items) {
            boolean packed = false;
            if (!binList.isEmpty()) {
                // 只考虑当前打开的箱子，即最后一个箱子
                Bin bin = binList.get(binList.size() - 1);
                int mf = Math.min(bin.minFragile, item.fragile);
                if (mf - bin.totalW >= item.w) {
                    bin.minFragile = mf;
                    bin.totalW += item.w;
                    bin.itemIndexList.add(item.index);
                    packed = true;
                }
            }
            if (!packed) {
                Bin bin = new Bin();
                bin.totalW = item.w;
                bin.minFragile = item.fragile;
                bin.itemIndexList.add(item.index);
                binList.add(bin);
            }
        }
        return binList;
    }

    public static List<Bin> solve(Instance instance, int LB) {
        // firstFit
        List<Bin> binList = firstFit(instance.items);
        if (binList.size() > LB) {
            Item[] items = Item.copy(instance.items);
            Arrays.sort(items, Item.itemComparatorByDecreaseWAndIncreaseFragile);
            List<Bin> tempBinList = firstFit(items);
            if (tempBinList.size() < binList.size()) binList = tempBinList;
        }
        if (binList.size() > LB) {
            Item[] items = Item.copy(instance.items);
            Arrays.sort(items, Item.itemComparatorByIncreaseFragileChuW);
            List<Bin> tempBinList = firstFit(items);
            if (tempBinList.size() < binList.size()) binList = tempBinList;
        }

        // bestFit
        if (binList.size() > LB) {
            List<Bin> tempBinList = bestFit(instance.items);
            if (tempBinList.size() < binList.size()) binList = tempBinList;
        }
        if (binList.size() > LB) {
            Item[] items = Item.copy(instance.items);
            Arrays.sort(items, Item.itemComparatorByDecreaseWAndIncreaseFragile);
            List<Bin> tempBinList = bestFit(items);
            if (tempBinList.size() < binList.size()) binList = tempBinList;
        }
        if (binList.size() > LB) {
            Item[] items = Item.copy(instance.items);
            Arrays.sort(items, Item.itemComparatorByIncreaseFragileChuW);
            List<Bin> tempBinList = bestFit(items);
            if (tempBinList.size() < binList.size()) binList = tempBinList;
        }

        // worstFit
        if (binList.size() > LB) {
            List<Bin> tempBinList = worstFit(instance.items);
            if (tempBinList.size() < binList.size()) binList = tempBinList;
        }
        if (binList.size() > LB) {
            Item[] items = Item.copy(instance.items);
            Arrays.sort(items, Item.itemComparatorByDecreaseWAndIncreaseFragile);
            List<Bin> tempBinList = worstFit(items);
            if (tempBinList.size() < binList.size()) binList = tempBinList;
        }
        if (binList.size() > LB) {
            Item[] items = Item.copy(instance.items);
            Arrays.sort(items, Item.itemComparatorByIncreaseFragileChuW);
            List<Bin> tempBinList = worstFit(items);
            if (tempBinList.size() < binList.size()) binList = tempBinList;
        }

        // nextFit
        if (binList.size() > LB) {
            List<Bin> tempBinList = nextFit(instance.items);
            if (tempBinList.size() < binList.size()) binList = tempBinList;
        }
        if (binList.size() > LB) {
            Item[] items = Item.copy(instance.items);
            Arrays.sort(items, Item.itemComparatorByDecreaseWAndIncreaseFragile);
            List<Bin> tempBinList = nextFit(items);
            if (tempBinList.size() < binList.size()) binList = tempBinList;
        }
        if (binList.size() > LB) {
            Item[] items = Item.copy(instance.items);
            Arrays.sort(items, Item.itemComparatorByIncreaseFragileChuW);
            List<Bin> tempBinList = nextFit(items);
            if (tempBinList.size() < binList.size()) binList = tempBinList;
        }

        CheckUtil.checkBinList(binList, instance);

        return binList;
    }
}