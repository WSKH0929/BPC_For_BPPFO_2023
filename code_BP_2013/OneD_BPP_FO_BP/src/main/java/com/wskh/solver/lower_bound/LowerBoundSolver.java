package com.wskh.solver.lower_bound;

import com.wskh.classes.*;
import com.wskh.solver.DpPricingSolver;
import com.wskh.solver.IpPricingSolver;
import com.wskh.solver.RmpSolver;
import com.wskh.utils.CommonUtil;
import ilog.concert.IloException;

import java.util.*;

public class LowerBoundSolver {

    public static int lb2(Instance instance) {
        int lb = 1;
        Item[] items = Item.copy(instance.items);
        Arrays.sort(items, Comparator.comparingInt(o -> o.fragile));
        int f = items[0].fragile - items[0].w;
        for (int i = 1; i < items.length; i++) {
            if (items[i].w <= f) {
                f = f - items[i].w;
            } else {
                lb++;
                f = items[i].fragile - (items[i].w - f);
            }
        }
        return lb;
    }

    public static void lb201(Instance instance, BpNode bpNode) {

        List<Item> itemList = new ArrayList<>();
        List<Integer> y1 = new ArrayList<>();
        List<Integer> yf = new ArrayList<>();

        int removeBinSize = 0;
        for (Bin bin : bpNode.binList) {
            if (bin.C == 0) {
                removeBinSize++;
            } else {
                Item item = new Item();
                item.fragile = bin.minFragile;
                item.w = bin.totalW;
                y1.add(itemList.size());
                itemList.add(item);
            }
        }

        for (int i = bpNode.curNoPackedFirstItemIndex; i < bpNode.assignArr.length; i++) {
            yf.add(itemList.size());
            itemList.add(instance.items[i]);
        }

        int[] y = new int[itemList.size()];
        int lb = removeBinSize;
        int r = 0;
        boolean infeasible = false;
        for (int k = 0; k < itemList.size() && !infeasible; k++) {
            Item item = itemList.get(k);
            if (y1.contains(k)) {
                r = r + item.fragile - item.w;
                y[k] = 1;
                lb++;
            } else {
                while (r < item.w) {
                    int i = -1;
                    for (int h : yf) {
                        if (h <= k && y[h] == 0) {
                            i = h;
                        }
                    }
                    if (i == -1) {
                        infeasible = true; // infeasible
                        break;
                    }
                    r = r + itemList.get(i).fragile;
                    y[i] = 1;
                    lb++;
                }
                r = r - item.w;
            }
        }
        // 更新下界
        bpNode.lb = infeasible ? Integer.MAX_VALUE : Math.max(lb, bpNode.lb);
    }

    public static void lbCG(Instance instance, BpSolution bpSolution, BpNode bpNode) {
        try {
            // 构建局部实例
            int removeBinSize = 0;
            List<Item> itemList = new ArrayList<>();
            for (Bin bin : bpNode.binList) {
                if (bin.C == 0) {
                    removeBinSize++;
                } else {
                    Item item = new Item();
                    item.fragile = bin.minFragile;
                    item.w = bin.totalW;
                    itemList.add(item);
                }
            }

            for (int i = bpNode.curNoPackedFirstItemIndex; i < bpNode.assignArr.length; i++) {
                if (bpNode.assignArr[i] == null) {
                    itemList.add(instance.items[i]);
                }
            }

            itemList.sort(Item.itemComparatorByDecreaseFragile);
            int C = itemList.get(0).fragile;

            // 初始化RMP
            RmpSolver rmpSolver = new RmpSolver(itemList, bpSolution);
            for (int i = 0; i < itemList.size(); i++) {
                Pattern pattern = new Pattern();
                pattern.packedItemIndexList = new LinkedList<>();
                pattern.packedItemIndexList.add(i);
                pattern.genKey();
                rmpSolver.addColumn(pattern);
            }

            // 列生成过程
            int validIntLowerBound = 0;
            while (true) {
                rmpSolver.solveRmp();

                long startTime = System.currentTimeMillis();
                Pattern newPattern = DpPricingSolver.solve(C, itemList, rmpSolver.rmpModel.getDuals(rmpSolver.itemRanges));
                bpSolution.pricingTime += (System.currentTimeMillis() - startTime);
                bpSolution.pricingCnt++;

//                Pattern pattern = IpPricingSolver.solve(C, itemList, rmpSolver.rmpModel.getDuals(rmpSolver.itemRanges));

                if (newPattern == null) {
//                    if (pattern != null) {
//                        throw new RuntimeException();
//                    }
                    validIntLowerBound = CommonUtil.ceilToInt(rmpSolver.objValue);
//                    System.out.println("Termination By empty negative reduced cost column");
                    break;
                }

//                if (CommonUtil.compareDouble(pattern.reducedCost, newPattern.reducedCost) != 0) {
//                    System.out.println(pattern);
//                    System.out.println(newPattern);
//                    throw new RuntimeException();
//                }

                // 根据有效下界提前跳出
                double validFracLowerBound = rmpSolver.objValue / (1d - newPattern.reducedCost);
                validIntLowerBound = Math.max(validIntLowerBound, CommonUtil.ceilToInt(validFracLowerBound));
                if (validIntLowerBound == CommonUtil.ceilToInt(rmpSolver.objValue)) {
//                    System.out.println("Break by rmp.objValue: " + rmpSolver.objValue + " , " + validFracLowerBound);
                    break;
                }

                // 加负数列到Rmp中
                rmpSolver.addColumn(newPattern);
            }
            // 更新下界
//            System.out.println("validIntLowerBound: " + validIntLowerBound + " , removeBinSize: " + removeBinSize + " , validIntLowerBound + removeBinSize: " + (validIntLowerBound + removeBinSize));
            bpNode.lb = Math.max(validIntLowerBound + removeBinSize, bpNode.lb);
            rmpSolver.rmpModel.end();
        } catch (IloException e) {
            throw new RuntimeException(e);
        }
    }

}
