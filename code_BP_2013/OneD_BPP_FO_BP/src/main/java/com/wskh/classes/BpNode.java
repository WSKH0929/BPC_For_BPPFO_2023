package com.wskh.classes;

import java.util.ArrayList;
import java.util.List;

public class BpNode {
    public int curNoPackedFirstItemIndex;
    public int lb;
    public Integer[] assignArr;
    public List<Bin> binList;

    public BpNode copy() {
        BpNode bp = new BpNode();
        bp.curNoPackedFirstItemIndex = curNoPackedFirstItemIndex;
        bp.lb = lb;
        bp.assignArr = assignArr.clone();
        bp.binList = Bin.copy(binList);
        return bp;
    }

}
