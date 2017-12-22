package com.mit;

import java.awt.peer.SystemTrayPeer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static gudusoft.gsqlparser.ERaiseLevel.exception;

/**
 * Created by peijialing on 17/12/2017.
 */
public class scanApp {
    public static int find(String pathName) throws IOException {
        int countVar=0;
        File dirFile = new File(pathName);
        if (!dirFile.exists()) {
            System.out.println("do not exit");
            return countVar;
        }
        if (!dirFile.isDirectory()) {
            if (dirFile.isFile() && dirFile.getName().contains(".java")) {
                countVar+= calcVar(dirFile.getCanonicalPath());
                //System.out.println(dirFile.getCanonicalFile());
            }
        }
        else {
            String[] fileList = dirFile.list();
            for (int i = 0; i < fileList.length; i++) {
                String string = fileList[i];
                File file = new File(dirFile.getPath(),string);
                String name = file.getName();
                if (file.isDirectory()) {
                    String p = file.getCanonicalPath();
                    countVar += find(file.getCanonicalPath());
                }else{
                    if (file.isFile() && file.getName().contains(".java")) {
                        countVar+= calcVar(file.getCanonicalPath());
                        //System.out.println(dirFile.getCanonicalFile());
                    }

                }
            }
        }
        return countVar;
    }
    public static int calcVar(String fileName){
        BufferedReader br = null;
        String line = null;
        String newLine = null;
        int countVar = 0;
        //StringBuffer buf = new StringBuffer();
        try {
            br = new BufferedReader(new FileReader(fileName));
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().contains("jdbctemplate.query") || line.toLowerCase().contains("template.query")) {
                    countVar++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }
        }
        return countVar;
    }
    public static int scan(String args) throws IOException{
        String pathName = args;
        int count = 0;
        count = find(pathName);
        return count;
    }
    public static void main(String args[]) throws IOException{
        String pathName = args[0];
        int countVar = 0;
        countVar = find(pathName);
        System.out.println("# of binding variables");
        System.out.println(countVar);
    }
}
