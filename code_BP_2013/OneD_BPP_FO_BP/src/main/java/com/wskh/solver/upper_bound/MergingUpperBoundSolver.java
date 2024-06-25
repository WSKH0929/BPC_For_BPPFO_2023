package com.wskh.solver.upper_bound;

import com.wskh.classes.Bin;
import com.wskh.classes.Instance;
import com.wskh.classes.Item;
import com.wskh.utils.CheckUtil;

import java.util.ArrayList;
import java.util.List;


public class MergingUpperBoundSolver {

    static class MergeItem {
        int weight;
        int fragility;
        List<Integer> itemIndexList = new ArrayList<>();

        public MergeItem(int weight, int fragility) {
            this.weight = weight;
            this.fragility = fragility;
        }

        // Method to merge this item with another item.
        public MergeItem merge(MergeItem other) {
            MergeItem mergeItem = new MergeItem(this.weight + other.weight, Math.min(this.fragility, other.fragility));
            mergeItem.itemIndexList = new ArrayList<>(itemIndexList);
            mergeItem.itemIndexList.addAll(other.itemIndexList);
            return mergeItem;
        }
    }

    private static List<Bin> solve1(Instance instance) {
        List<MergeItem> mergeItems = new ArrayList<>();
        for (Item item : instance.items) {
            MergeItem mergeItem = new MergeItem(item.w, item.fragile);
            mergeItem.itemIndexList.add(item.index);
            mergeItems.add(mergeItem);
        }

        boolean merged = true;
        while (merged) {
            merged = false;
            int[] pairToMerge = new int[]{-1, -1};
            int minDifference = Integer.MAX_VALUE;

            // Find the best pair to merge
            for (int i = 0; i < mergeItems.size(); i++) {
                for (int j = i + 1; j < mergeItems.size(); j++) {
                    MergeItem item1 = mergeItems.get(i);
                    MergeItem item2 = mergeItems.get(j);
                    int currentDifference = Math.abs(item1.fragility - item2.fragility);

                    if (item1.weight + item2.weight <= Math.min(item1.fragility, item2.fragility) && currentDifference < minDifference) {
                        minDifference = currentDifference;
                        pairToMerge[0] = i;
                        pairToMerge[1] = j;
                    }
                }
            }

            // Merge the pair and update the list of mergeItems
            if (pairToMerge[0] != -1) {
                MergeItem mergedItem = mergeItems.get(pairToMerge[0]).merge(mergeItems.get(pairToMerge[1]));
                mergeItems.set(pairToMerge[0], mergedItem); // Replace one item with merged
                mergeItems.remove(pairToMerge[1]); // Remove the other item
                merged = true;
            }
        }
        List<Bin> binList = new ArrayList<>(mergeItems.size());
        for (MergeItem mergeItem : mergeItems) {
            Bin bin = new Bin();
            bin.totalW = mergeItem.weight;
            bin.minFragile = mergeItem.fragility;
            bin.itemIndexList = mergeItem.itemIndexList;
            binList.add(bin);
        }
        CheckUtil.checkBinList(binList, instance);
        return binList;
    }

    private static List<Bin> solve2(Instance instance) {
        List<MergeItem> mergeItems = new ArrayList<>();
        for (Item item : instance.items) {
            MergeItem mergeItem = new MergeItem(item.w, item.fragile);
            mergeItem.itemIndexList.add(item.index);
            mergeItems.add(mergeItem);
        }

        boolean merged = true;
        while (merged) {
            merged = false;
            int[] pairToMerge = new int[]{-1, -1};
            int minDifference = Integer.MAX_VALUE;

            // Find the best pair to merge
            for (int i = 0; i < mergeItems.size(); i++) {
                for (int j = i + 1; j < mergeItems.size(); j++) {
                    MergeItem item1 = mergeItems.get(i);
                    MergeItem item2 = mergeItems.get(j);
                    int currentDifference = Math.min(item1.fragility, item2.fragility) - item1.weight - item2.weight;

                    if (item1.weight + item2.weight <= Math.min(item1.fragility, item2.fragility) && currentDifference < minDifference) {
                        minDifference = currentDifference;
                        pairToMerge[0] = i;
                        pairToMerge[1] = j;
                    }
                }
            }

            // Merge the pair and update the list of mergeItems
            if (pairToMerge[0] != -1) {
                MergeItem mergedItem = mergeItems.get(pairToMerge[0]).merge(mergeItems.get(pairToMerge[1]));
                mergeItems.set(pairToMerge[0], mergedItem); // Replace one item with merged
                mergeItems.remove(pairToMerge[1]); // Remove the other item
                merged = true;
            }
        }
        List<Bin> binList = new ArrayList<>(mergeItems.size());
        for (MergeItem mergeItem : mergeItems) {
            Bin bin = new Bin();
            bin.totalW = mergeItem.weight;
            bin.minFragile = mergeItem.fragility;
            bin.itemIndexList = mergeItem.itemIndexList;
            binList.add(bin);
        }
        CheckUtil.checkBinList(binList, instance);
        return binList;
    }

    public static List<Bin> solve(Instance instance, int LB) {
        List<Bin> binList1 = solve1(instance);
        if (LB == binList1.size()) {
            return binList1;
        }
        List<Bin> binList2 = solve2(instance);
        return binList1.size() < binList2.size() ? binList1 : binList2;
    }
}
