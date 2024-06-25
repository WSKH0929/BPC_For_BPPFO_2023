package com.wskh.solver;

import com.wskh.classes.Item;
import com.wskh.classes.Parameter;
import com.wskh.classes.Pattern;
import com.wskh.utils.CheckUtil;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class IpPricingSolver {
    public static Pattern solve(int C, List<Item> itemList, double[] itemDualValues) {

        if (itemList.isEmpty()) return null;

        try {
            IloCplex cplex = new IloCplex();
            cplex.setOut(null);
            cplex.setWarning(null);
            cplex.setParam(IloCplex.IntParam.Threads, 1);
            cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, Parameter.EPS);
            cplex.setParam(IloCplex.Param.MIP.Tolerances.AbsMIPGap, Parameter.EPS);

            IloIntVar[] betas = cplex.boolVarArray(itemList.size());
            IloIntVar[] alphas = cplex.boolVarArray(itemList.size());

            IloLinearNumExpr target = cplex.linearNumExpr();
            for (int j = 0; j < itemList.size(); j++) {
                target.addTerm(itemDualValues[j], betas[j]);
                target.addTerm(itemDualValues[j], alphas[j]);
            }
            cplex.addMaximize(target);

            cplex.addEq(cplex.sum(betas), 1);

            IloLinearNumExpr expr2 = cplex.linearNumExpr();
            for (int j = 0; j < itemList.size(); j++) {
                Item item = itemList.get(j);
                expr2.addTerm(item.fragile - item.w, betas[j]);
                expr2.addTerm(-item.w, alphas[j]);
            }
            cplex.addGe(expr2, 0);

            for (int k = 0; k < itemList.size(); k++) {
                IloLinearNumExpr expr3 = cplex.linearNumExpr();
                expr3.addTerm(1, alphas[k]);
                for (int j = k; j >= 0; j--) {
                    expr3.addTerm(1, betas[j]);
                }
                cplex.addLe(expr3, 1);
            }

            if (cplex.solve()) {
                double reducedCost = 1 - cplex.getObjValue();
                if (reducedCost < -Parameter.EPS) {
                    double[] betasValues = cplex.getValues(betas);
                    double[] alphasValues = cplex.getValues(alphas);
                    Pattern pattern = new Pattern();
                    pattern.reducedCost = reducedCost;
                    pattern.packedItemIndexList = new LinkedList<>();
                    for (int j = 0; j < betasValues.length; j++) {
                        if (betasValues[j] > 0.5) {
                            pattern.packedItemIndexList.add(j);
                            break;
                        }
                    }
                    for (int j = 0; j < alphasValues.length; j++) {
                        if (alphasValues[j] > 0.5) {
                            if (j < pattern.packedItemIndexList.getFirst()) {
                                pattern.packedItemIndexList.addFirst(j);
                            } else {
                                pattern.packedItemIndexList.addLast(j);
                            }
                        }
                    }
                    pattern.genKey();
                    CheckUtil.checkPattern(pattern, itemList, itemDualValues);
                    cplex.end();
                    return pattern;
                }
            } else {
                throw new RuntimeException("定价子问题无解");
            }
            cplex.end();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
