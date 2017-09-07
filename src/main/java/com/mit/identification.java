package com.mit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by peijialing on 23/8/2017.
 */
public class identification {
    public static void callScript(String script, String[] args, String... workspace){
        try {
            String cmd = "sh " + script + " " + args;
            File dir = null;
            if(workspace[0] != null){
                dir = new File(workspace[0]);
                System.out.println(workspace[0]);
            }
            String[] evnp = {"val=2", "call=Bash Shell"};
            Process process = Runtime.getRuntime().exec(cmd, evnp, dir);
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
            File resFile = new File("scanRes.txt");

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public static String[] IdentifyApplications(String[] tableList, String appAddr) {

            String pathName = appAddr;
            int numOfApp = 0;
            callScript("scanApp.sh",tableList,appAddr);
            return new String[numOfApp];



    }
    public static  String[] IdentifySubSchema(int type, String[] paramArr) {
        return new String[1];
    }
}
