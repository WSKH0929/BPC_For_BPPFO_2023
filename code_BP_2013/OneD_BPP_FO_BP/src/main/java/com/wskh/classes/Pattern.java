package com.wskh.classes;

import com.wskh.utils.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Pattern {
    public String key;
    public double reducedCost;
    public LinkedList<Integer> packedItemIndexList;

    public Pattern copy() {
        return new Pattern(key, reducedCost, new LinkedList<>(packedItemIndexList));
    }

    public static List<Pattern> copy(List<Pattern> patternList) {
        return patternList.stream().map(pattern -> pattern.copy()).collect(Collectors.toList());
    }

    public void genKey() {
        key = "";
        for (Integer i : packedItemIndexList) {
            key = key + i + "@";
        }
    }

}
