package com.wskh.solver;

import com.wskh.classes.*;
import com.wskh.utils.CommonUtil;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RmpSolver {
    public List<Item> itemList;
    public BpSolution bpSolution;
    public IloCplex rmpModel;
    public IloObjective objective;
    public IloRange[] itemRanges;
    public List<IloNumVar> patternVarList;
    public Set<String> patternKeySet;
    public double objValue;

    public RmpSolver(List<Item> itemList, BpSolution bpSolution) {
        this.itemList = itemList;
        this.bpSolution = bpSolution;
        patternKeySet = new HashSet<>();
        patternVarList = new ArrayList<>(1024);
        try {
            // 声明主问题模型
            rmpModel = new IloCplex();
            rmpModel.setOut(null);
            rmpModel.setWarning(null);
            rmpModel.setParam(IloCplex.IntParam.Threads, 1);
            rmpModel.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Primal);
            rmpModel.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, Parameter.EPS);
            rmpModel.setParam(IloCplex.Param.MIP.Tolerances.AbsMIPGap, Parameter.EPS);
            // 目标函数
            objective = rmpModel.addMinimize();
            // 约束1：确保每个物品都被打包
            itemRanges = new IloRange[itemList.size()];
            for (int i = 0; i < itemRanges.length; i++) {
                itemRanges[i] = rmpModel.addGe(rmpModel.linearNumExpr(), 1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addColumn(Pattern pattern) {
        if (pattern.key == null) {
            throw new RuntimeException("Pattern's key is null: " + pattern);
        }
        if (patternKeySet.contains(pattern.key)) {
            throw new RuntimeException("Add repeat pattern: " + pattern);
        }
        int lastIndex = -1;
        for (Integer curIndex : pattern.packedItemIndexList) {
            if (lastIndex > curIndex) {
                throw new RuntimeException("packedItemIndexList的顺序错误: " + pattern);
            }
            lastIndex = curIndex;
        }
        try {
            IloColumn column = rmpModel.column(objective, 1);
            for (Integer i : pattern.packedItemIndexList) {
                column = column.and(rmpModel.column(itemRanges[i], 1));
            }
            patternVarList.add(rmpModel.numVar(column, 0, Double.MAX_VALUE));
            patternKeySet.add(pattern.key);
            bpSolution.totalColCnt++;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void solveRmp() {
        try {
            long startTime = System.currentTimeMillis();
            if (rmpModel.solve()) {
                objValue = rmpModel.getObjValue();
            } else {
                throw new RuntimeException("Rmp model has no solution: " + rmpModel.getStatus());
            }
            bpSolution.rmpTime += (System.currentTimeMillis() - startTime);
            bpSolution.rmpCnt++;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}