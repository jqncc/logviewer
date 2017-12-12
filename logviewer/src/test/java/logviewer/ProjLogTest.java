package logviewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jflame.logviewer.model.ProjLogInfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class ProjLogTest {

    public static void main(String[] args) {

        List<ProjLogInfo> projs = new ArrayList<>();
        ProjLogInfo logInfo = new ProjLogInfo();
        logInfo.setProjName("闪士多");
        logInfo.setLogPaths(new String[]{ "10.26.126.119/zpgo","10.30.222.30/zpgo" });
        projs.add(logInfo);

        try {
            FileUtils.write(new File("D:\\proj.json"), JSON.toJSONString(projs, SerializerFeature.PrettyFormat));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
