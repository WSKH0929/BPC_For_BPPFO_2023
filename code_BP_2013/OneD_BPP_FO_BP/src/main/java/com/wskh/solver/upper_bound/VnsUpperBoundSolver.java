package com.wskh.solver.upper_bound;

import com.wskh.classes.Bin;
import com.wskh.classes.Instance;
import com.wskh.classes.Item;

import java.util.*;
import java.util.stream.Collectors;

public class VnsUpperBoundSolver {

    private boolean isTimeLimit() {
        return (System.currentTimeMillis() - startTime) >= timeLimit;
    }

    Instance instance;
    int LB;
    long timeLimit;
    long startTime;

    public VnsUpperBoundSolver(Instance instance, int LB, long timeLimit) {
        this.instance = instance;
        this.LB = LB;
        this.timeLimit = timeLimit;
    }

    private List<Bin> initBinList() {

        List<Bin> binList = XFitUpperBoundSolver.solve(instance, LB);
        System.out.println("XFitUpperBoundSolver: " + binList.size());

        if (binList.size() > LB) {
            List<Bin> tempBinList = KnapsackBasedUpperBoundSolver.solve(instance);
            if (tempBinList.size() < binList.size()) binList = tempBinList;
            System.out.println("KnapsackBasedUpperBoundSolver: " + tempBinList.size());
        }

        if (binList.size() > LB) {
            List<Bin> tempBinList = CliqueBasedUpperBoundSolver.solve(instance);
            if (tempBinList.size() < binList.size()) binList = tempBinList;
            System.out.println("CliqueBasedUpperBoundSolver: " + tempBinList.size());
        }

        if (binList.size() > LB) {
            List<Bin> tempBinList = MergingUpperBoundSolver.solve(instance, LB);
            if (tempBinList.size() < binList.size()) binList = tempBinList;
            System.out.println("MergingUpperBoundSolver: " + tempBinList.size());
        }

        return binList;
    }

    private int[] calcFai(Item item, Bin bin) {
        int[] fai = new int[2];
        int minFragile = item.fragile;
        int totalW = item.w;
        for (int i : bin.itemIndexList) {
            minFragile = Math.min(minFragile, instance.items[i].fragile);
            totalW += instance.items[i].w;
            if (item.w + instance.items[i].w > Math.min(item.fragile, instance.items[i].fragile)) {
                fai[0]++;
            }
        }
        fai[1] = Math.max(0, totalW - minFragile);
        return fai;
    }

    private List<Bin> perturbation(int t, int h, List<Bin> binList) {
        // 在原有解的基础上，移除 h 个箱子
        int newBinSize = binList.size() - 1;
        List<Bin> removeBinList = new ArrayList<>(h);
        if (t == 1 || t == 2) {
            // 概率与最小脆弱性成正比
            for (int i = 0; i < h; i++) {
                double totalF = 0d;
                for (Bin bin : binList) {
                    totalF += bin.minFragile;
                }
                double[] rates = new double[binList.size()];
                for (int j = 0; j < rates.length; j++) {
                    rates[j] = (j - 1 >= 0 ? rates[j - 1] : 0) + binList.get(j).minFragile / totalF;
                }
                double r = random.nextDouble();
                for (int j = 0; j < rates.length; j++) {
                    if (r <= rates[j]) {
                        removeBinList.add(binList.remove(j));
                        break;
                    }
                }
            }
        } else if (t == 3) {
            // 随机删除
            for (int i = 0; i < h; i++) {
                removeBinList.add(binList.remove(random.nextInt(binList.size())));
            }
        } else {
            throw new RuntimeException("不应该出现的t值: " + t);
        }

        // 获取被移除箱子中的物品集合
        List<Integer> removeItemIndexList = new ArrayList<>();
        for (Bin bin : removeBinList) {
            removeItemIndexList.addAll(bin.itemIndexList);
        }
        Collections.sort(removeItemIndexList);

        // 以原有解箱子数 -1，加入空箱，构造新解
        List<Bin> newBinList = new ArrayList<>(binList);
        while (newBinList.size() < newBinSize) {
            newBinList.add(new Bin());
        }

        // 将被删除的箱子中的物品加入新解
        if (t == 1) {
            // best-best
            List<int[]> bestFaiList = new ArrayList<>();
            List<Integer> bestBinIndexList = new ArrayList<>();
            for (int j : removeItemIndexList) {
                int[] bestFai = null;
                int bestBinIndex = -1;
                for (int i = 0; i < newBinList.size(); i++) {
                    int[] fai = calcFai(instance.items[j], newBinList.get(i));
                    if (bestBinIndex < 0 || bestFai[0] > fai[0] || (bestFai[0] == fai[0] && bestFai[1] > fai[0])) {
                        bestBinIndex = i;
                        bestFai = fai;
                    }
                    if (fai[0] == 0 && fai[1] == 0) {
                        break;
                    }
                }
                bestFaiList.add(bestFai);
                bestBinIndexList.add(bestBinIndex);
            }
            boolean[] used = new boolean[removeItemIndexList.size()];
            int cnt = 0;
            while (cnt < removeItemIndexList.size()) {
                int[] bestFai = null;
                int bestI = -1;
                int bestBinIndex = -1;
                for (int i = 0; i < bestFaiList.size(); i++) {
                    if (!used[i]) {
                        int[] fai = bestFaiList.get(i);
                        int j = bestBinIndexList.get(i);
                        if (bestBinIndex < 0 || bestFai[0] > fai[0] || (bestFai[0] == fai[0] && bestFai[1] > fai[0])) {
                            bestBinIndex = j;
                            bestI = i;
                            bestFai = fai;
                        }
                        if (fai[0] == 0 && fai[1] == 0) {
                            break;
                        }
                    }
                }
                used[bestI] = true;
                newBinList.get(bestBinIndex).itemIndexList.add(removeItemIndexList.get(bestI));
                cnt++;
            }
        } else {
            // first-first
            for (int j : removeItemIndexList) {
                int[] bestFai = null;
                int bestIndex = -1;
                for (int i = 0; i < newBinList.size(); i++) {
                    int[] fai = calcFai(instance.items[j], newBinList.get(i));
                    if (bestIndex < 0 || bestFai[0] > fai[0] || (bestFai[0] == fai[0] && bestFai[1] > fai[0])) {
                        bestIndex = i;
                        bestFai = fai;
                    }
                    if (fai[0] == 0 && fai[1] == 0) {
                        break;
                    }
                }
                newBinList.get(bestIndex).itemIndexList.add(j);
            }
        }
        return newBinList.stream().filter(bin -> !bin.itemIndexList.isEmpty()).collect(Collectors.toList());
//        return newBinList;
    }

    private List<Bin> swap(List<Bin> solution, int i, int j) {
        if (i >= solution.get(0).itemIndexList.size() || j >= solution.get(1).itemIndexList.size()) {
            return null;
        }
        int removeI = solution.get(0).itemIndexList.remove(i);
        int removeJ = solution.get(1).itemIndexList.remove(j);
        solution.get(0).itemIndexList.add(removeJ);
        solution.get(1).itemIndexList.add(removeI);
        solution.get(0).sortItem(instance.items);
        solution.get(1).sortItem(instance.items);
        solution.get(0).updateObj_W_F(instance.items);
        solution.get(1).updateObj_W_F(instance.items);
        return solution;
    }

    private List<Bin> localSearch(List<Bin> binList) {
        for (Bin bin : binList) {
            bin.updateObj_W_F(instance.items);
        }
        if (binList.size() == 1) {
            return binList;
        }
        binList.sort(Comparator.comparingInt(o -> o.obj));
        for (int n = 0; n < 4 && (System.currentTimeMillis() - startTime) < timeLimit; ) {
            List<Bin> temp = null;
            if (n == 0) {
                temp = swap(Bin.copy(binList), 1, 0);
            } else if (n == 1) {
                temp = swap(Bin.copy(binList), 1, 1);
            } else if (n == 2) {
                temp = swap(Bin.copy(binList), 1, 2);
            } else if (n == 3) {
                temp = swap(Bin.copy(binList), 2, 1);
            } else {
                throw new RuntimeException("不存在的邻域");
            }
            if (temp != null && Math.max(temp.get(0).obj, temp.get(1).obj) < Math.max(binList.get(0).obj, binList.get(1).obj)) {
                binList = temp;
                binList.sort(Comparator.comparingInt(o -> o.obj));
                n = 0;
            } else {
                n++;
            }
        }
        return binList;
    }

    private boolean judgeFeasible(List<Bin> binList) {
        for (Bin bin : binList) {
            if (bin.obj > 0) {
                return false;
            }
        }
        return true;
    }

    Random random;
    public List<Bin> binList;

    public List<Bin> solve() {
        startTime = System.currentTimeMillis();
        this.random = new Random(929);
        binList = initBinList();

        // VNS过程
        int h = 1;
        while (!isTimeLimit() && binList.size() > LB) {
            int t = 1;
            boolean isFeasible = false;
            List<Bin> binListPiePie = null;
            while (t <= 3 && !isFeasible && !isTimeLimit()) {
                List<Bin> binListPie = perturbation(t, h, Bin.copy(binList));
                binListPiePie = localSearch(Bin.copy(binListPie));
                isFeasible = judgeFeasible(binListPiePie);
                t++;
            }
            if (isFeasible) {
                binList = binListPiePie;
                h = 1;
            } else {
                h++;
                if (h >= binList.size()) {
                    h = 1;
                }
            }
        }

        return binList;
    }


}