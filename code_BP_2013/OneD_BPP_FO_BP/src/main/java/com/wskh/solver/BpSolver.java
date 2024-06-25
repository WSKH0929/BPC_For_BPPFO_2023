package com.wskh.solver;


import com.wskh.classes.*;
import com.wskh.solver.lower_bound.LowerBoundSolver;
import com.wskh.solver.upper_bound.VnsUpperBoundSolver;
import com.wskh.utils.TimeUtil;

import java.util.ArrayList;
import java.util.LinkedList;

public class BpSolver {

    Instance instance;

    public BpSolver(Instance instance) {
        this.instance = instance;
    }

    private void updateBestSolutionByBpNode(BpNode bpNode) {
        if (bpNode.binList.size() < bestBpSolution.UB) {
            // 更新找到的最优解
            bestBpSolution.UB = bpNode.binList.size();
            bestBpSolution.binList = Bin.copy(bpNode.binList);
            if (bestBpSolution.UB == bestBpSolution.LB0) {
                // 如果找到了最优解，则直接手动抛异常，终止函数
                bestBpSolution.isOpt = true;
                throw new StopException();
            }
        }
    }

    private boolean computeNodeLowerBound(BpNode bpNode) {

        // 计算 L201
        LowerBoundSolver.lb201(instance, bpNode);
        if (bpNode.lb >= bestBpSolution.UB) {
            return true;
        }

        // 计算列生成下界
        LowerBoundSolver.lbCG(instance, bestBpSolution, bpNode);

        return bpNode.lb >= bestBpSolution.UB;
    }

    private void speedUpPacking(int binIndex, Bin bin, BpNode bpNode) {
        LinkedList<Integer> canPackItemIndexList = new LinkedList<>();
        for (Integer i : suitableItems[bin.C]) {
            if (bpNode.assignArr[i] == null) {
                canPackItemIndexList.add(i);
            }
        }
        if (canPackItemIndexList.isEmpty()) {
            bin.C = 0;
        } else if (canPackItemIndexList.size() == 1) {
            int itemIndex = canPackItemIndexList.getFirst();
            Item item = instance.items[itemIndex];
            bin.totalW += item.w;
            bin.C -= item.w;
            bin.itemIndexList.add(itemIndex);

            bpNode.assignArr[itemIndex] = binIndex;

            bin.C = 0;
        }
    }

    private void openNewBinAndPackItem(BpNode bpNode, int itemIndex) {
        Item item = instance.items[itemIndex];
        Bin bin = new Bin();
        bin.minFragile = item.fragile;
        bin.totalW = item.w;
        bin.C = bin.minFragile - bin.totalW;
        bin.itemIndexList.add(itemIndex);

        bpNode.assignArr[itemIndex] = bpNode.binList.size();
        bpNode.binList.add(bin);

        // 加速装箱
        speedUpPacking(bpNode.binList.size() - 1, bin, bpNode);
    }

    private void packItemIntoBinI(int binIndex, int itemIndex, BpNode bpNode) {
        Item item = instance.items[itemIndex];
        Bin bin = bpNode.binList.get(binIndex);

        bin.totalW += item.w;
        bin.C -= item.w;
        bin.itemIndexList.add(itemIndex);

        bpNode.assignArr[itemIndex] = binIndex;

        // 加速装箱
        speedUpPacking(binIndex, bin, bpNode);
    }

    private void branchAndBoundByDFS(BpNode bpNode) {

        // 如果达到时间限制，则直接手动抛异常，终止函数
        if (TimeUtil.isTimeLimit()) {
            throw new StopException();
        }

        bestBpSolution.exploredNodeCnt++;

        // 叶子节点，构成一个完整可行解
        if (bpNode.curNoPackedFirstItemIndex >= instance.items.length) {
            updateBestSolutionByBpNode(bpNode);
            return;
        }

        // 如果当前物品已经被打包，则继续递归
        if (bpNode.assignArr[bpNode.curNoPackedFirstItemIndex] != null) {
            bpNode.curNoPackedFirstItemIndex++;
            branchAndBoundByDFS(bpNode);
            return;
        }

        // 计算下界
        if (computeNodeLowerBound(bpNode)) {
            return;
        }

        // 分支
        // 第一种分支，把第curNoPackedFirstItemIndex个物品分配到每个已经打开的Bin中
        Item item = instance.items[bpNode.curNoPackedFirstItemIndex];
        for (int i = 0; i < bpNode.binList.size(); i++) {
            Bin bin = bpNode.binList.get(i);
            if (bin.C > 0) {
                if (bin.minFragile - bin.totalW >= item.w) {
                    BpNode childNode = bpNode.copy();
                    packItemIntoBinI(i, childNode.curNoPackedFirstItemIndex, childNode);
                    childNode.curNoPackedFirstItemIndex++;
                    // 递归组合分支定界过程
                    branchAndBoundByDFS(childNode);
                }
            }
        }
        // 第二种分支，把第curNoPackedFirstItemIndex个物品分配到新打开的箱子
        openNewBinAndPackItem(bpNode, bpNode.curNoPackedFirstItemIndex);
        bpNode.curNoPackedFirstItemIndex++;
        branchAndBoundByDFS(bpNode);
    }

    BpSolution bestBpSolution;
    LinkedList<Integer>[] suitableItems;

    public BpSolution solve() {
        TimeUtil.startTime = System.currentTimeMillis();
        System.out.println("----------------------- Start the branch-and-price algorithm -----------------------");
        bestBpSolution = new BpSolution();

        // 计算下界
        long startTime = System.currentTimeMillis();
        bestBpSolution.LB0 = LowerBoundSolver.lb2(instance);
        bestBpSolution.LB = bestBpSolution.LB0;
        bestBpSolution.lb0Time = System.currentTimeMillis() - startTime;
        System.out.println("LB0: " + bestBpSolution.LB0 + " , time cost: " + bestBpSolution.lb0Time + " ms");

        // 计算初始上界
        startTime = System.currentTimeMillis();
        bestBpSolution.binList = new VnsUpperBoundSolver(instance, bestBpSolution.LB, Parameter.ub0TimeLimit).solve();
//        bestBpSolution.binList = DpBasedUpperBoundSolver.solve(instance);
        bestBpSolution.UB0 = bestBpSolution.binList.size();
        bestBpSolution.UB = bestBpSolution.UB0;
        bestBpSolution.ub0Time = System.currentTimeMillis() - startTime;
        System.out.println("UB0: " + bestBpSolution.UB0 + " , time cost: " + bestBpSolution.ub0Time + " ms");

        if (bestBpSolution.LB0 == bestBpSolution.UB0) {
            bestBpSolution.isOpt = true;
        } else {
            // 组合分支定界过程
            if (!bestBpSolution.isOpt && !TimeUtil.isTimeLimit()) {
                // 计算每个在不同容量下哪些物品j可以放进容器
                suitableItems = new LinkedList[instance.items[instance.items.length - 1].fragile + 1];
                for (int c = 0; c < suitableItems.length; c++) {
                    suitableItems[c] = new LinkedList<>();
                }
                for (int i = 0; i < instance.items.length; i++) {
                    for (int c = instance.items[i].w; c <= instance.items[i].fragile; c++) {
                        suitableItems[c].add(i);
                    }
                }
                // DFS
                BpNode rootNode = new BpNode();
                rootNode.lb = bestBpSolution.LB;
                rootNode.assignArr = new Integer[instance.items.length];
                rootNode.binList = new ArrayList<>(bestBpSolution.UB);
                openNewBinAndPackItem(rootNode, 0); // 根节点，把第0个物品打包到新箱子中
                rootNode.curNoPackedFirstItemIndex++;
                try {
                    branchAndBoundByDFS(rootNode);
                } catch (StopException e) {

                }
                // 如果时间限制还没到就停止了分支定界过程，则说明下界=上界，从而证明最优（因为我们已经遍历了所有有希望的方案）
                if (!TimeUtil.isTimeLimit()) {
                    bestBpSolution.isOpt = true;
                    bestBpSolution.LB = bestBpSolution.UB;
                }
            }
        }

        // 结束BPC算法
        bestBpSolution.totalTime = TimeUtil.getCurTime();
        System.out.println("----------------------- End the branch-and-price algorithm -----------------------");

        bestBpSolution.binList.forEach(System.out::println);

        // 判断下界是否错误
        if (bestBpSolution.LB > bestBpSolution.UB) {
            throw new RuntimeException("Lower bound (" + bestBpSolution.LB + ") > Upper bound (" + bestBpSolution.UB + ")");
        }

        bestBpSolution.gap = bestBpSolution.isOpt ? 0 : (double) (bestBpSolution.UB - bestBpSolution.LB) / bestBpSolution.UB;

        return bestBpSolution;
    }

}
