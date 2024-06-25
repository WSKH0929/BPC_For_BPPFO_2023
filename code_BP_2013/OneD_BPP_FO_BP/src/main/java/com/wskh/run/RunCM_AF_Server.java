package com.wskh.run;

import com.wskh.classes.Bin;
import com.wskh.classes.Instance;
import com.wskh.classes.Parameter;
import com.wskh.solver.AF_Solver;
import com.wskh.solver.CM_Solver;
import com.wskh.solver.upper_bound.DpBasedUpperBoundSolver;
import com.wskh.utils.CommonUtil;
import com.wskh.utils.ReadUtil;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class RunCM_AF_Server {

    public static void writeLine(FileOutputStream fileOutputStream, Instance instance, int ub, double time, IloCplex cplex) throws IOException, IloException {
        String[] split = instance.name.split("_");
        String className = split[1] + "_" + split[2] + "_" + split[3];
//        String className = split[2];
        int lb = CommonUtil.ceilToInt(cplex.getBestObjValue());

        if (cplex.getStatus().equals(IloCplex.Status.Optimal)) {
            lb = ub;
        }

        String line = instance.name + " , " + className + " , " + instance.C + " , " + instance.items.length + " , "
                + cplex.getNcols() + " , " + cplex.getNrows() + " , "
                + lb + " , " + ub + " , " + (ub - lb) / (double) ub + " , " + (cplex.getStatus().equals(IloCplex.Status.Optimal) ? 1 : 0) + " , "
                + time;
        System.out.println(line);
        fileOutputStream.write((line + "\n").getBytes(StandardCharsets.UTF_8));
    }

    public static void main(String[] args) throws Exception {

        Parameter.EPS = 1e-09;

//        String dataDir = "D:\\WSKH\\MyData\\Research\\1D-BPP-FO\\Src\\OneD_BPP_FO_BPC\\data\\InstancesBPPFO";
//        String dataDir = "D:\\WSKH\\MyData\\Research\\1D-BPP-FO\\Src\\OneD_BPP_FO_BP\\src\\main\\java\\com\\wskh\\solver";
//        String dataDir = "D:\\WSKH\\MyData\\Research\\1D-BPP-FO\\Src\\OneD_BPP_FO_BPC\\data\\randomInstance";

        String dataDir = "E:\\wskh\\BPP-FO-2013\\InstancesBPPFO";
//        String dataDir = "E:\\wskh\\BPPFO-CM-AF\\randomInstance";

        String resultDir = "results\\";

        new File(resultDir).mkdir();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


        FileOutputStream cmResults = new FileOutputStream(resultDir + "\\Res-CM-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".csv");
        FileOutputStream afResults = new FileOutputStream(resultDir + "\\Res-AF-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".csv");
        FileOutputStream afNResults = new FileOutputStream(resultDir + "\\Res-AF_NP-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".csv");
        String titleLine = "Instance, Class, C, |N|, Col, Row, LB, UB, Gap, Opt, Time(s)\n";
        cmResults.write(titleLine.getBytes(StandardCharsets.UTF_8));
        afResults.write(titleLine.getBytes(StandardCharsets.UTF_8));
        afNResults.write(titleLine.getBytes(StandardCharsets.UTF_8));

        for (File file : Objects.requireNonNull(new File(dataDir).listFiles())) {
            if (file.getName().endsWith("BPPFI")) {

                Instance instance = ReadUtil.readInstance(file.getAbsolutePath());

                if (instance.items.length != 50) {
                    continue;
                }

                System.out.println("====================================== " + simpleDateFormat.format(new Date()) + " " + instance.name + "-" + instance.items.length + " =====================================");

                List<Bin> binList = DpBasedUpperBoundSolver.solve(instance);

                System.out.println("------ CM_Solver ------");
                CM_Solver cmSolver = new CM_Solver();
                binList = cmSolver.solve(instance, 360, binList);
                writeLine(cmResults, instance, binList.size(), cmSolver.time / 1000d, cmSolver.cplex);
                cmSolver.cplex.end();

                System.gc();

                System.out.println("------ AF_Solver (normalPoints) ------");
                AF_Solver afSolver = new AF_Solver();
                binList = afSolver.solve(instance, 360, "normalPoints", binList);
                writeLine(afNResults, instance, binList.size(), afSolver.time / 1000d, afSolver.cplex);
                afSolver.cplex.end();

                System.gc();

//                System.out.println("------ AF_Solver (allPoints) ------");
//                afSolver = new AF_Solver();
//                binList = afSolver.solve(instance, 360, "allPoints", binList);
//                writeLine(afResults, instance, binList.size(), afSolver.time / 1000d, afSolver.cplex);
//                afSolver.cplex.end();
//
//                System.gc();

            }
        }

        cmResults.close();
        afResults.close();
        afNResults.close();
    }
}
