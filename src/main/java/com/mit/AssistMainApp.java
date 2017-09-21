package com.mit;
import com.mit.dataStructure.AppCandidate;
import com.mit.dataStructure.Req;
import com.mit.rewriting;

/**
 * Created by peijialing on 23/8/2017.
 */
public class AssistMainApp {
    public static void main(String[] args) throws Exception {
        double alpha = 0.0;
        double beta = 0.0;
        double theta = 0.0;
        double gamma = 0.0;
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
            AppCandidate[] appArr = identification.IdentifyApplications(tableList,appAddr);

            // start re-writing
            rewriting.dealWithQuery(appArr,type,paramArr);
            //calculate possible decay and maintenance
            int appmain = calculation.calc_appmain();
            int appdecay = calculation.calc_appdecay();
            int dbmain = calculation.calc_dbmain();
            int dbdecay = calculation.calc_dbdecay();
            double totalDecay = alpha*appdecay+beta*appmain+theta*dbdecay+gamma*dbmain;
        }

    }
}
