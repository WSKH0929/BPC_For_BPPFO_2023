package com.wskh.solver;

import com.wskh.classes.Bin;
import com.wskh.classes.Instance;
import com.wskh.classes.Item;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.*;

public class AF_Solver {

    @AllArgsConstructor
    @ToString
    class ArcItem {
        int w;
        int f;
        int num;
    }

    @AllArgsConstructor
    @ToString
    class IntArr {
        int[] arr;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            IntArr intArr = (IntArr) o;
            return Arrays.equals(arr, intArr.arr);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(arr);
        }
    }

    public IloCplex cplex;
    public long time;
    public Set<Integer> F;
    public List<ArcItem> itemList;

    public List<Bin> solve(Instance instance, double timeLimit, String pointType, List<Bin> initBinList) throws IloException {
        // 预处理
        itemList = new ArrayList<>();
        HashMap<IntArr, Integer> map = new HashMap<>();
        for (Item item : instance.items) {
            IntArr key = new IntArr(new int[]{item.w, item.fragile});
            if (!map.containsKey(key)) {
                map.put(key, itemList.size());
                itemList.add(new ArcItem(item.w, item.fragile, 1));
            } else {
                itemList.get(map.get(key)).num++;
            }
        }
        F = new HashSet<>();
        for (ArcItem arcItem : itemList) {
            F.add(arcItem.f);
        }

        List<Integer> packingPoints;
        if (pointType.equals("allPoints")) {
            packingPoints = PointSolver.allPoints(instance.C);
        } else if (pointType.equals("normalPoints")) {
            packingPoints = PointSolver.normalPoints(instance.C, Arrays.asList(instance.items));
        } else if (pointType.equals("midPoints")) {
            packingPoints = PointSolver.mimPoints(instance.C, Arrays.asList(instance.items));
        } else {
            throw new RuntimeException();
        }

        int C = packingPoints.get(packingPoints.size() - 1);

        System.out.println("itemList.size() = " + itemList.size());
        System.out.println("F.size() = " + F.size());

        // 建模求解
        cplex = new IloCplex();
        cplex.setOut(null);
        cplex.setWarning(null);
        cplex.setParam(IloCplex.IntParam.Threads, 1);
        cplex.setParam(IloCplex.DoubleParam.TimeLimit, timeLimit);
        // 声明变量
        // 构造弧（变量）
        HashMap<IntArr, IloIntVar> x = new HashMap<>();
        // 初始化：A_{source}
        for (ArcItem itemU : itemList) {
            IntArr key = new IntArr(new int[]{0, C, itemU.w, itemU.f});
            if (!x.containsKey(key)) {
                x.put(key, cplex.intVar(0, Integer.MAX_VALUE));
            }
        }
        // 初始化：A_{item}
        for (Integer i : packingPoints) {
            if (i > 0) {
                for (ArcItem itemU : itemList) {
                    int j = itemU.w + i;
                    if (j < C + 1) {
                        int fj = itemU.f;
                        if (j <= fj) {
                            for (int fi : F) {
                                if (fj <= fi) {
                                    IntArr key = new IntArr(new int[]{i, fi, j, fj});
                                    if (!x.containsKey(key)) {
                                        x.put(key, cplex.intVar(0, Integer.MAX_VALUE));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // 初始化：A_{loss}
        for (Integer i : packingPoints) {
            if (i >= 1 && i < C) {
                for (int f : F) {
                    IntArr key = new IntArr(new int[]{i, f, C, 0});
                    if (!x.containsKey(key)) {
                        x.put(key, cplex.intVar(0, Integer.MAX_VALUE));
                    }
                }
            }
        }
        System.out.println("A.size: " + x.size());
        // 目标函数
        IloIntVar z = cplex.intVar(0, Integer.MAX_VALUE);
//        IloIntVar z = cplex.intVar(lb, instance.n);
        cplex.addMinimize(z);
        // 声明约束
        // 约束1：流平衡约束
        IloLinearNumExpr expr_z = cplex.linearNumExpr();
        IloLinearNumExpr exprz = cplex.linearNumExpr();
        HashMap<IntArr, IloLinearNumExpr> exprs = new HashMap<>();
        for (Map.Entry<IntArr, IloIntVar> entry : x.entrySet()) {
            IntArr key = entry.getKey();
            // expr_z
            if (key.arr[2] == 0 && key.arr[3] == C) {
                throw new RuntimeException();
            }
            if (key.arr[0] == 0 && key.arr[1] == C) {
                expr_z.addTerm(-1, entry.getValue());
            }
            // exprz
            if (key.arr[0] == C) {
                throw new RuntimeException();
            }
            if (key.arr[2] == C) {
                exprz.addTerm(1, entry.getValue());
            }
            // exprs
            if (isMidPoint(key.arr[2], key.arr[3], C)) {
                IntArr keyKey = new IntArr(new int[]{key.arr[2], key.arr[3]});
                if (!exprs.containsKey(keyKey)) {
                    exprs.put(keyKey, cplex.linearNumExpr());
                }
                exprs.get(keyKey).addTerm(1, entry.getValue());
            }
            if (isMidPoint(key.arr[0], key.arr[1], C)) {
                IntArr keyKey = new IntArr(new int[]{key.arr[0], key.arr[1]});
                if (!exprs.containsKey(keyKey)) {
                    exprs.put(keyKey, cplex.linearNumExpr());
                }
                exprs.get(keyKey).addTerm(-1, entry.getValue());
            }
        }
        cplex.addEq(expr_z, cplex.prod(-1, z));
        cplex.addEq(exprz, z);
        for (Map.Entry<IntArr, IloLinearNumExpr> entry : exprs.entrySet()) {
            cplex.addEq(entry.getValue(), 0);
        }
        // 约束2：需求约束
        for (ArcItem itemU : itemList) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (Map.Entry<IntArr, IloIntVar> entry : x.entrySet()) {
                int[] key = entry.getKey().arr;
                if (key[3] <= key[1] && key[2] <= key[3]) {
                    if (key[2] - key[0] == itemU.w
                            && itemU.f == key[3]) {
                        expr.addTerm(1, entry.getValue());
                    }
                } else {
                    if (key[3] > 0) {
                        throw new RuntimeException(Arrays.toString(key));
                    }
                }
            }
            cplex.addEq(expr, itemU.num);
        }
        // 求解
        time = System.currentTimeMillis();
        if (cplex.solve()) {
            time = System.currentTimeMillis() - time;
            System.out.println("AF1模型求得最优解为: " + cplex.getObjValue());

            Map<IntArr, Integer> resultMap = new HashMap<>();
            List<IntArr> sourceList = new ArrayList<>();
            for (Map.Entry<IntArr, IloIntVar> entry : x.entrySet()) {
                double v = cplex.getValue(entry.getValue());
                if (v > 0.5) {
//                    System.out.println(entry.getKey()+" : "+v);
                    resultMap.put(entry.getKey(), (int) Math.round(v));
                    if (entry.getKey().arr[0] == 0) {
                        sourceList.add(entry.getKey());
                    }
                }
            }

            List<Bin> binList = new ArrayList<>();
            short[] used = new short[instance.items.length];
            for (IntArr source : sourceList) {
                int[] curV = new int[]{source.arr[2], source.arr[3]};
                List<int[]> packItems = new ArrayList<>();
                packItems.add(new int[]{source.arr[2] - source.arr[0], source.arr[3]});
                while (true) {
                    HashMap<IntArr, Integer> copyMap = new HashMap<>(resultMap);
                    for (Map.Entry<IntArr, Integer> entry : copyMap.entrySet()) {
                        int[] arr = entry.getKey().arr;
                        if (curV[0] == arr[0] && curV[1] == arr[1]) {
                            if (arr[3] > 0) {
                                int w = arr[2] - arr[0];
                                int f = arr[3];
                                packItems.add(new int[]{w, f});
                            }
                            curV = new int[]{arr[2], arr[3]};
                            resultMap.remove(entry.getKey());
                            break;
                        }
                    }
                    if (resultMap.size() == copyMap.size()) {
                        break;
                    }
                }

                int cnt = resultMap.remove(source);
                for (int i = 0; i < cnt; i++) {
                    Bin bin = new Bin();
                    for (int[] packItem : packItems) {
                        for (int j = 0; j < instance.items.length; j++) {
                            if (used[j] == 0 && packItem[0] == instance.items[j].w && packItem[1] == instance.items[j].fragile) {
                                bin.itemIndexList.add(j);
                                used[j] = 1;
                                break;
                            }
                        }
                    }
                    binList.add(bin);
                }
            }
            System.out.println("binList.size = " + binList.size());
//            for (Bin bin : binList) {
//                bin.check(instance.items);
//            }
            return binList;
        } else {
            System.err.println("此题无解");
            List<Bin> binList = new ArrayList<>();
            for (Item item : instance.items) {
                Bin bin = new Bin();
                bin.minFragile = item.fragile;
                bin.totalW = item.fragile;
                bin.C = item.w - item.fragile;
                bin.itemIndexList.add(item.index);
                binList.add(bin);
            }
            System.out.println("binList.size = " + binList.size());
            return binList;
        }
    }

    public boolean isMidPoint(int i, int f, int C) {
        if (i == 0 && f == C) {
            return false;
        }
        return i != C;
    }
}