import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GenBat {
    public static void main(String[] args) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream("out/artifacts/OneD_BPP_FO_BP_jar/run.bat");
        for (int i = 0; i < 675; i++) {
            String data = "java -Xms28G -Xmx28G -Djava.library.path=E:\\Cplex1263\\x64_win64 -jar OneD_BPP_FO_BP.jar " + i + " & \n";
            fileOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
        }
        fileOutputStream.close();
    }
}
