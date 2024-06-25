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

public class RunLocal {
    public static void main(String[] args) throws Exception {

        String dataDir = "D:\\WSKH\\MyData\\Research\\1D-BPP-FO\\Src\\OneD_BPP_FO_BPC\\data\\InstancesBPPFO";
        String resultDir = "results\\";
        String solutionDir = resultDir + "solutions";
        String logFolderPath = resultDir + "log";

        new File(resultDir).mkdir();
        new File(solutionDir).mkdir();
        new File(logFolderPath).mkdir();

//        Parameter.TimeLimit = 0L * 1000L;
        Parameter.TimeLimit = 3600L * 1000L;
        Parameter.ub0TimeLimit = 5 * 1000L;
        Parameter.EPS = 1e-09;
        Parameter.LogEnable = false;

        String csvPath = resultDir + "\\Res-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".csv";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        for (File file : Objects.requireNonNull(new File(dataDir).listFiles())) {
            if (file.getName().endsWith("BPPFI")) {

                Instance instance = ReadUtil.readInstance(file.getAbsolutePath());

                // N1C1W1_CL1_1_3_B.BPPFI
                // N1C1W1_CL1_1_3_D.BPPFI
                // N2C1W1_CL1_1_3_E.BPPFI
                // N1C1W1_CL2_3_3_A.BPPFI
//                if (!instance.name.equals("N1C2W2_CL2_3_4_E.BPPFI")) {
//                    continue;
//                }

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
