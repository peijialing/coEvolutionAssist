package com.mit;

import java.io.IOException;

/**
 * Created by peijialing on 23/8/2017.
 */
public class calculation {
    public calculation(String pName){
        pathName = pName;
    }
    String pathName;
    public static int calc_dbdecay() {
        return 0;
    }
    public static  int calc_dbmain() {
        return 0;
    }
    public static int calc_appdecay() {
        return 0;
    }
    public static int calc_appmain(String pathName) throws IOException{
        int count = scanApp.scan(pathName);
        return count;
    }
}
