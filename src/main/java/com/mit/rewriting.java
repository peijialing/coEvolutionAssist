package com.mit;
import com.mit.dataStructure.AppCandidate;
import com.mit.dataStructure.alter_info;
import com.mit.dataStructure.variables;
import com.sun.org.apache.xpath.internal.operations.Variable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Created by peijialing on 23/8/2017.
 */
public class rewriting {
    public static void rewrite(String fileName) {
        ArrayList<alter_info> alterInfoList = identification.alterInfoList;
        for (int i=0; i<alterInfoList.size();++i) {
            alter_info info = alterInfoList.get(i);
            switch (info.type) {
                case AddColumn:

            }
        }
    }
}
