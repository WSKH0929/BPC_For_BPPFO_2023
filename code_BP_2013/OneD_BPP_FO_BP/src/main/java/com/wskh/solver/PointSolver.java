package com.wskh.solver;


import com.wskh.classes.Item;

import java.util.*;

public class PointSolver {

    public static List<Integer> allPoints(int C) {
        List<Integer> allPoints = new ArrayList<>(C + 1);
        for (int c = 0; c <= C; c++) {
            allPoints.add(c);
        }
        return allPoints;
    }

    public static List<Integer> normalPoints(int C, List<Item> itemList) {
        if (C <= 0) {
            List<Integer> list = new ArrayList<>();
            list.add(0);
            return list;
        }
        boolean[] D = new boolean[C + 1];
        D[0] = true;
        for (int x = 0; x <= C; x++) {
            if (D[x]) {
                for (int i = itemList.size() - 1; i >= 0; i--) {
                    Item item = itemList.get(i);
                    if (x + item.w <= item.fragile) {
                        D[x + item.w] = true;
                    }
                }
            }
        }

//        for (int i = itemList.size() - 1; i >= 0; i--) {
//            Item item = itemList.get(i);
//            for (int x = 0; x <= C; x++) {
//                if (D[x] && x + item.w <= item.fragile) {
//                    D[x + item.w] = true;
//                }
//            }
//        }

        List<Integer> normalPoints = new ArrayList<>();
        for (int c = 0; c < D.length; c++) {
            if (D[c]) {
                normalPoints.add(c);
            }
        }
        return normalPoints;
    }

    public static List<Integer> mimPoints(int C, int i, List<Item> itemList, int t) {
        Item removeItem = itemList.remove(i);
        C = Math.min(C, removeItem.fragile);
        List<Integer> listLeft = normalPoints(Math.min(t - 1, C - removeItem.w), itemList);
        List<Integer> listRightPie = normalPoints(C - t - removeItem.w, itemList);
        List<Integer> listRight = new ArrayList<>();
        for (Integer x : listRightPie) {
            listRight.add(C - removeItem.w - x);
        }
        listLeft.addAll(listRight);
        return listLeft;
    }

    public static List<Integer> mimPoints(int C, List<Item> itemList) {
        Set<Integer> bestSet = null;
        for (int t = 1; t < C / 2; t++) {
            Set<Integer> set = new HashSet<>();
            for (int i = 0; i < itemList.size(); i++) {
                set.addAll(mimPoints(C, i, new ArrayList<>(itemList), t));
            }
            if (bestSet == null || bestSet.size() > set.size()) {
                bestSet = set;
            }
        }
        List<Integer> mimPoints = new ArrayList<>(bestSet);
        Collections.sort(mimPoints);
        return mimPoints;
    }

}
