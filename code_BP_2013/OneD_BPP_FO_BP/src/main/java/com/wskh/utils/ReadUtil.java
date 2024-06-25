package com.wskh.utils;

import com.wskh.classes.Instance;
import com.wskh.classes.Item;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

public class ReadUtil {
    public static Instance readInstance(String filePath) throws Exception {
        Instance instance = new Instance();
        instance.name = new File(filePath).getName();
        String line = null;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
        int index = 0;

        while ((line = bufferedReader.readLine()) != null) {
            if (index == 0) {
                instance.items = new Item[Integer.parseInt(line)];
            } else if (index > 1) {
                int i = index - 2;
                String[] split = line.split(" ");
                instance.items[i] = new Item(i, i, Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            }
            index++;
        }

        Arrays.sort(instance.items, Item.itemComparatorByIncreaseFragileAndDecreaseW);

        instance.C = instance.items[0].fragile;
        for (int i = 0; i < instance.items.length; i++) {
            instance.items[i].index = i;
            instance.C = Math.max(instance.C, instance.items[i].fragile);
        }

        bufferedReader.close();
        return instance;
    }

}
