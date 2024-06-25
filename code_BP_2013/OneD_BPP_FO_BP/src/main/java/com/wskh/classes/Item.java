package com.wskh.classes;

import com.wskh.utils.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Comparator;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Item {
    public int id, index, w, fragile;

    public Item copy() {
        return new Item(id, index, w, fragile);
    }

    public static Item[] copy(Item[] items) {
        Item[] copy = new Item[items.length];
        for (int i = 0; i < items.length; i++) {
            copy[i] = items[i].copy();
        }
        return copy;
    }

    public static Comparator<Item> itemComparatorByIncreaseFragileAndDecreaseW = (o1, o2) -> {
        int c = Integer.compare(o1.fragile, o2.fragile);
        return c == 0 ? -Integer.compare(o1.w, o2.w) : c;
    };

    public static Comparator<Item> itemComparatorByDecreaseFragile = (o1, o2) -> {
        return -Integer.compare(o1.fragile, o2.fragile);
    };

    public static Comparator<Item> itemComparatorByDecreaseWAndIncreaseFragile = (o1, o2) -> {
        int c = -Integer.compare(o1.w, o2.w);
        return c == 0 ? Integer.compare(o1.fragile, o2.fragile) : c;
    };

    public static Comparator<Item> itemComparatorByIncreaseFragileChuW = (o1, o2) -> {
        return CommonUtil.compareDouble((double) o1.fragile / o1.w, (double) o2.fragile / o2.w);
    };

}
