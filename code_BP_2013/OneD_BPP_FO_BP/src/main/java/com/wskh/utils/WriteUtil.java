package com.wskh.utils;


import com.alibaba.fastjson.JSON;
import com.wskh.classes.BpSolution;
import com.wskh.classes.Instance;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class WriteUtil {
    public static void writeSolutionToJsonTxt(BpSolution bpSolution, String path) {
        try {
            String jsonString = JSON.toJSONString(bpSolution);
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            fileOutputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeSolutionToCsv(BpSolution bpSolution, Instance instance, String path) {
        try {
            FileOutputStream fileOutputStream;
            if (!new File(path).exists()) {
                fileOutputStream = new FileOutputStream(path);
                String title = "Name, class, n, " +
                        "opt, gap, lb0, lb, ub0, ub, " +
                        "rootColCnt, totalColCnt, exploredNodeCnt, rmpCnt, pricingCnt," +
                        "lb0Time, ub0Time, rmpTime, pricingTime, totalTime";
                fileOutputStream.write(title.getBytes(StandardCharsets.UTF_8));
                fileOutputStream.close();
                writeSolutionToCsv(bpSolution, instance, path);
            } else {
                fileOutputStream = new FileOutputStream(path, true);
                String line = "\n";
                line += (instance.name + ", ");
                String[] split = instance.name.split("_");
                line += (split[1] + "_" + split[2] + "_" + split[3] + ", ");
                line += (instance.items.length + ", ");

                line += ((bpSolution.isOpt ? 1 : 0) + ", ");
                line += (bpSolution.gap + ", ");
                line += (bpSolution.LB0 + ", ");
                line += (bpSolution.LB + ", ");
                line += (bpSolution.UB0 + ", ");
                line += (bpSolution.UB + ", ");

                line += (bpSolution.rootColCnt + ", ");
                line += (bpSolution.totalColCnt + ", ");
                line += (bpSolution.exploredNodeCnt + ", ");
                line += (bpSolution.rmpCnt + ", ");
                line += (bpSolution.pricingCnt + ", ");

                line += (bpSolution.lb0Time / 1000d + ", ");
                line += (bpSolution.ub0Time / 1000d + ", ");
                line += (bpSolution.rmpTime / 1000d + ", ");
                line += (bpSolution.pricingTime / 1000d + ", ");
                line += (bpSolution.totalTime / 1000d);

                fileOutputStream.write(line.getBytes(StandardCharsets.UTF_8));
            }
            fileOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
