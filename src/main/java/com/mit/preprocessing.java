package com.mit;
import com.mit.dataStructure.Req;
/**
 * Created by peijialing on 22/8/2017.
 */
public class preprocessing {
    public static  int[] createFeasibleOrder(Req[] resReqArr, int numOfChanges) {
        int[]  order = new int[numOfChanges];
        for (int i=0;i<numOfChanges;++i){
            order[i]=i;
        }
        return order;
    }
    //todo: add other cases
    public static Req parseUsrReq(String usrReq) {
        String[] reqList = usrReq.split(" ");
        int type=0;
        String[] paramList=new String[2];
        if (reqList[0].equals("ALTER")) {
            if (reqList[1].equals("TABLE")) {
                String operation = reqList[3] + " " + reqList[4];
                if (operation.equals("ADD COLUMN")) {
                    type = 0;
                    paramList = new String[2];
                    paramList[0] = reqList[2];
                    paramList[1] = reqList[5];
                }
                else if (operation.equals("DROP COLUMN")) {
                    type = 1;
                    paramList = new String[2];
                    paramList[0] = reqList[2];
                    paramList[1] = reqList[5];
                }
                else if (operation.equals("RENAME COLUMN")) {
                    type = 2;
                    paramList = new String[3];
                    paramList[0] = reqList[2];
                    paramList[1] = reqList[5];
                    paramList[2] = reqList[7];
                }
            }
            else if (reqList[1].equals("VIEW")) {

            }
        }
        else if (reqList[0].equals("CREATE")) {

        }
        Req resReq = new Req(type,paramList);
        return resReq;
    }
    //raul's data discovery
    public static void ConvertToGraph(String SchemaAddress) {


    }


}
