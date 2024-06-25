package com.wskh.classes;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ToString
@Data
public class Bin {
    public int obj;
    public int totalW;
    public int minFragile = Integer.MAX_VALUE;
    public int C; // 剩余容量
    public List<Integer> itemIndexList = new ArrayList<>();

    public Bin copy() {
        Bin copyBin = new Bin();
        copyBin.obj = obj;
        copyBin.minFragile = minFragile;
        copyBin.totalW = totalW;
        copyBin.itemIndexList = new ArrayList<>(itemIndexList);
        copyBin.C = C;
        return copyBin;
    }

    public static List<Bin> copy(List<Bin> binList) {
        return binList.stream().map(bin -> bin.copy()).collect(Collectors.toList());
    }

    public void check(Item[] items) {
        updateObj_W_F(items);
        if (totalW > minFragile) {
            throw new RuntimeException("解不可行: " + totalW + " > " + minFragile);
        }
    }

    public void sortItem(Item[] items) {
        itemIndexList.sort((index1, index2) -> -Integer.compare(items[index1].fragile, items[index2].fragile));
    }

    public void updateObj_W_F(Item[] items) {
        minFragile = items[itemIndexList.get(0)].fragile;
        totalW = items[itemIndexList.get(0)].w;
        for (int i = 1; i < itemIndexList.size(); i++) {
            minFragile = Math.min(minFragile, items[itemIndexList.get(i)].fragile);
            totalW += items[itemIndexList.get(i)].w;
        }
        obj = Math.max(0, totalW - minFragile);
    }

}
