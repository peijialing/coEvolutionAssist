package com.mit;
import com.mit.dataStructure.Req;
import com.mit.rewriting;

/**
 * Created by peijialing on 23/8/2017.
 */
public class AssistMainApp {
    public static void main(String[] args) throws Exception {
        String dbAddr = args[0];
        String appAddr = args[1];
        int numOfChanges = Integer.parseInt(args[2]);
        String[] usrReqArr =  new String[numOfChanges];
        Req[] resReqArr = new Req[numOfChanges];
        for (int i=0; i<numOfChanges; ++i){
            resReqArr[i] = preprocessing.parseUsrReq(usrReqArr[i]);
        }
        int[] feasibleOrder = preprocessing.createFeasibleOrder(resReqArr,numOfChanges);

        preprocessing.ConvertToGraph(dbAddr);
        for (int i=0; i<numOfChanges;++i){
            int executeId = feasibleOrder[i];
            int type = resReqArr[executeId].type;
            String[] paramArr = resReqArr[executeId].paramArray;
            String[] tableList = identification.IdentifySubSchema(type,paramArr);
            String[] appArr = identification.IdentifyApplications(tableList,appAddr);

            // start re-writing

        }

    }
}
