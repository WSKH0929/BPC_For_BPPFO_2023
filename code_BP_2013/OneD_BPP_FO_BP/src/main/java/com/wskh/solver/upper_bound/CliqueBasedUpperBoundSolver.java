package com.wskh.solver.upper_bound;

import com.sun.corba.se.impl.orbutil.graph.Graph;
import com.wskh.classes.Bin;
import com.wskh.classes.Instance;
import com.wskh.classes.Item;
import com.wskh.utils.CheckUtil;
import com.wskh.utils.CommonUtil;

import java.util.*;


public class CliqueBasedUpperBoundSolver {

    static class BronKerboschWithPivot {
        private Set<Set<Integer>> cliques;
        private Set<Integer>[] graph;

        public BronKerboschWithPivot(Set<Integer>[] graph) {
            this.graph = graph;
            this.cliques = new HashSet<>();
        }

        public Set<Set<Integer>> findMaximalCliques() {
            Set<Integer> P = new HashSet<>();
            Set<Integer> R = new HashSet<>();
            Set<Integer> X = new HashSet<>();

            for (int i = 0; i < graph.length; i++) {
                P.add(i);
            }

            bronKerboschWithPivot(R, P, X);
            return cliques;
        }

        private void bronKerboschWithPivot(Set<Integer> R, Set<Integer> P, Set<Integer> X) {
            if (P.isEmpty() && X.isEmpty()) {
                cliques.add(new HashSet<>(R));
                return;
            }

            Integer pivot = selectPivot(P, X);
            Set<Integer> P1 = new HashSet<>(P);
            P1.removeAll(graph[pivot]); // Remove all neighbors of pivot

            for (Integer v : P1) {
                Set<Integer> R1 = new HashSet<>(R);
                R1.add(v);

                Set<Integer> P2 = intersect(P, graph[v]);
                Set<Integer> X2 = intersect(X, graph[v]);

                bronKerboschWithPivot(R1, P2, X2);

                P.remove(v);
                X.add(v);
            }
        }

        private Integer selectPivot(Set<Integer> P, Set<Integer> X) {
            Set<Integer> combined = new HashSet<>(P);
            combined.addAll(X);
            return combined.iterator().next(); // Simple pivot selection, could be optimized
        }

        private Set<Integer> intersect(Set<Integer> a, Set<Integer> b) {
            Set<Integer> result = new HashSet<>(a);
            result.retainAll(b);
            return result;
        }
    }

    public static List<Bin> solve(Instance instance) {
        List<Bin> binList = new ArrayList<>();

        Set<Integer>[] graph = new Set[instance.items.length];
        for (int i = 0; i < graph.length; i++) {
            graph[i] = new HashSet<>();
        }
        for (int i = 0; i < instance.items.length; i++) {
            for (int j = i + 1; j < instance.items.length; j++) {
                if (Math.min(instance.items[i].fragile, instance.items[j].fragile) - instance.items[i].w < instance.items[j].w) {
                    graph[i].add(j);
                    graph[j].add(i);
                }
            }
        }

        BronKerboschWithPivot bronKerboschWithPivot = new BronKerboschWithPivot(graph);
        Set<Set<Integer>> maximalCliques = bronKerboschWithPivot.findMaximalCliques();
        Set<Integer> maximalClique = null;
        for (Set<Integer> c : maximalCliques) {
            if (maximalClique == null || maximalClique.size() < c.size()) {
                maximalClique = c;
            }
        }
        List<Integer> maxClique = maximalClique == null ? new ArrayList<>() : new ArrayList<>(maximalClique);
        Collections.sort(maxClique);

        int packedNum = 0;
        boolean[] used = new boolean[instance.items.length];
        while (!maxClique.isEmpty()) {
            int j = maxClique.remove(0);
            List<Item> itemList = new ArrayList<>();
            int C = instance.items[j].fragile - instance.items[j].w;
            for (int i = j + 1; i < instance.items.length; i++) {
                if (!used[i] && instance.items[i].w <= C) {
                    itemList.add(instance.items[i]);
                }
            }
            Bin bin = new Bin();
            bin.totalW = instance.items[j].w;
            bin.minFragile = instance.items[j].fragile;
            bin.itemIndexList.add(j);
            if (!itemList.isEmpty()) {
                CommonUtil.dpSolveKP01(bin, C, itemList);
            }
            for (Integer i : bin.itemIndexList) {
                used[i] = true;
                packedNum++;
                maxClique.remove(i);
            }
            binList.add(bin);
        }

        if (packedNum < instance.items.length) {
            List<Item> itemList = new ArrayList<>(instance.items.length - packedNum);
            for (int i = 0; i < used.length; i++) {
                if (!used[i]) {
                    itemList.add(instance.items[i]);
                }
            }
            for (Item item : itemList) {
                boolean canBePack = false;
                for (Bin bin : binList) {
                    int mf = Math.min(bin.minFragile, item.fragile);
                    if (mf - bin.totalW >= item.w) {
                        bin.minFragile = mf;
                        bin.totalW += item.w;
                        bin.itemIndexList.add(item.index);
                        canBePack = true;
                        break;
                    }
                }
                if (!canBePack) {
                    Bin bin = new Bin();
                    bin.totalW = item.w;
                    bin.minFragile = item.fragile;
                    bin.itemIndexList.add(item.index);
                    binList.add(bin);
                }
            }
        }

        CheckUtil.checkBinList(binList, instance);
        return binList;
    }
}