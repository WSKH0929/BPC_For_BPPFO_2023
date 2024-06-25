package com.wskh.solver;

import com.wskh.classes.Bin;
import com.wskh.classes.Instance;
import com.wskh.classes.Item;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CM_Solver {
    public IloCplex cplex;
    public long time;

    public List<Bin> solve(Instance instance, double timeLimit, List<Bin> initBinList) throws IloException {
        cplex = new IloCplex();
        cplex.setOut(null);
        cplex.setWarning(null);
        cplex.setParam(IloCplex.IntParam.Threads, 1);
        cplex.setParam(IloCplex.DoubleParam.TimeLimit, timeLimit);
        Item[] items = instance.items;
        int n = items.length;
        IloIntVar[] y = cplex.boolVarArray(n);
        IloIntVar[][] x = new IloIntVar[n][n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                if (j > i) {
                    x[j][i] = cplex.boolVar();
                }
            }
        }
        // 约束1：所有物品都要放入
        for (int j = 0; j < n; j++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int i = 0; i < j; i++) {
                expr.addTerm(1, x[j][i]);
            }
            cplex.addEq(expr, cplex.diff(1, y[j]));
        }
        // 约束2：满足脆弱性约束
        for (int i = 0; i < n; i++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int j = i + 1; j < n; j++) {
                expr.addTerm(items[j].w, x[j][i]);
            }
            cplex.addLe(expr, cplex.prod(items[i].fragile - items[i].w, y[i]));
        }
        // 约束3：必须要先用前面的箱子
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                cplex.addLe(x[j][i], y[i]);
            }
        }
        // 目标函数
        cplex.addMinimize(cplex.sum(y));
        // 设置初始解
//        int size = y.length + x.length * x[0].length;
//        double[] values = new double[size];
//        IloNumVar[] vars = new IloNumVar[size];
//        cplex.addMIPStart(vars, values);

        // 开始求解
        time = System.currentTimeMillis();
        if (cplex.solve()) {
            time = System.currentTimeMillis() - time;
            List<Bin> binList = new ArrayList<>();
            for (int i = 0; i < (int) Math.round(cplex.getObjValue()); i++) {
                binList.add(new Bin());
            }
            Map<Integer, Integer> map = new HashMap<>();
            int c = 0;
            for (int i = 0; i < y.length; i++) {
                if (cplex.getValue(y[i]) > 0.5) {
                    binList.get(c).itemIndexList.add(i);
                    map.put(i, c++);
                }
            }
            for (int j = 0; j < x.length; j++) {
                for (int i = 0; i < x[j].length; i++) {
                    if (x[j][i] != null) {
                        if (cplex.getValue(x[j][i]) > 0.5) {
                            binList.get(map.get(i)).itemIndexList.add(j);
                            break;
                        }
                    }
                }
            }
            for (Bin bin : binList) {
                bin.check(items);
            }
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
}
