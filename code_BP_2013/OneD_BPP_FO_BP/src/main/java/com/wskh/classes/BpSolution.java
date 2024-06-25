package com.wskh.classes;

import lombok.ToString;

import java.util.List;

public class BpSolution {
    public boolean isOpt;
    public double gap;
    public int LB0, LB, UB0, UB, rootColCnt, totalColCnt, exploredNodeCnt, rmpCnt, pricingCnt;
    public long lb0Time, ub0Time, rmpTime, pricingTime, totalTime;
    public List<Bin> binList;

    @Override
    public String toString() {
        return "BpSolution{" +
                "isOpt=" + isOpt +
                ", gap=" + gap +
                ", LB0=" + LB0 +
                ", LB=" + LB +
                ", UB0=" + UB0 +
                ", UB=" + UB +
                ", rootColCnt=" + rootColCnt +
                ", totalColCnt=" + totalColCnt +
                ", exploredNodeCnt=" + exploredNodeCnt +
                ", rmpCnt=" + rmpCnt +
                ", pricingCnt=" + pricingCnt +
                ", lb0Time=" + lb0Time +
                ", ub0Time=" + ub0Time +
                ", rmpTime=" + rmpTime +
                ", pricingTime=" + pricingTime +
                ", totalTime=" + totalTime +
                '}';
    }
}