package com.wskh.run;

import com.wskh.classes.BpSolution;
import com.wskh.classes.Instance;
import com.wskh.classes.Parameter;
import com.wskh.solver.BpSolver;
import com.wskh.utils.CheckUtil;
import com.wskh.utils.ReadUtil;
import com.wskh.utils.WriteUtil;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class RunServer {
    public static void main(String[] args) throws Exception {

        String dataDir = "E:\\wskh\\BPP-FO-2013\\InstancesBPPFO";
        String resultDir = "E:\\wskh\\BPP-FO-2013\\";
        String solutionDir = resultDir + "solutions";
        String logFolderPath = resultDir + "log";

        new File(resultDir).mkdir();
        new File(solutionDir).mkdir();
        new File(logFolderPath).mkdir();

//        Parameter.TimeLimit = 0 * 1000L;
        Parameter.TimeLimit = 3600L * 1000L;
        Parameter.ub0TimeLimit = 150 * 1000L;
        Parameter.EPS = 1e-09;
        Parameter.LogEnable = true;

        String csvPath = resultDir + "\\Res-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".csv";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        int targetIndex = Integer.parseInt(args[0]);
        int index = 0;
        for (File file : Objects.requireNonNull(new File(dataDir).listFiles())) {
            if (file.getName().endsWith("BPPFI")) {

                if (targetIndex != index++) {
                    continue;
                }

                Instance instance = ReadUtil.readInstance(file.getAbsolutePath());

                PrintStream oldPrintStream = System.out;
                if (Parameter.LogEnable) {
                    System.out.print("====================================== " + simpleDateFormat.format(new Date()) + " " + instance.name + "-" + instance.items.length + " =====================================");
                    if (new File(logFolderPath + "\\" + instance.name + "-" + instance.items.length + "-log.txt").exists()) {
                        System.out.println("Exist solution");
                        continue;
                    }
                    System.setOut(new PrintStream(logFolderPath + "\\" + instance.name + "-" + instance.items.length + "-log.txt"));
                }
                System.out.println("====================================== " + simpleDateFormat.format(new Date()) + " " + instance.name + "-" + instance.items.length + " =====================================");

                BpSolution bpSolution = new BpSolver(instance).solve();
                System.out.println(bpSolution);
                CheckUtil.checkSolution(bpSolution, instance);

                WriteUtil.writeSolutionToCsv(bpSolution, instance, csvPath);
                WriteUtil.writeSolutionToJsonTxt(bpSolution, solutionDir + "\\" + instance.name.split("\\.")[0] + "-solution.txt");

                System.setOut(oldPrintStream);
                if (Parameter.LogEnable) {
                    System.out.println(" " + bpSolution.totalTime / 1000d + " s");
                }

                System.gc();
            }
        }

    }
}
