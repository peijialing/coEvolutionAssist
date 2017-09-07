package com.mit;
import com.mit.dataStructure.AppCandidate;
import com.mit.dataStructure.variables;
import java.util.ArrayList;

/**
 * Created by peijialing on 23/8/2017.
 */
public class rewriting {
    public static ArrayList<variables> findBindingVar(AppCandidate[] appList) {
        ArrayList<variables> varRelatedArr = new ArrayList<variables>();
        for (int i=0; i<appList.length;++i) {
            AppCandidate app = appList[i];
            String pathName = app.appAddr;
            int location = app.location;
            //variables may be at those locations of apps
            String query = "";
            String varRelatedName = query.split(" ")[0];
            //todo: scan all var definitions
            variables varRelated = new variables(varRelatedName,pathName,location);
            varRelatedArr.add(varRelated);
        }
        return varRelatedArr;
    }

    // TODO: 6/9/2017
    public static AppCandidate[] interactWithUser(AppCandidate[] AppCanList) {
        return new AppCandidate[1];
    }

    // TODO: 6/9/2017
    public static void dealWithQuery(AppCandidate[] AppList, int ChangeType, String[] paramArr) {
        AppCandidate[] candidateApp = AppList;
        AppCandidate[] resAPPList = interactWithUser(candidateApp);
        switch (ChangeType) {
            case 0: // add column
                for (int i=0; i<resAPPList.length; ++i) {

                }

            case 1 :// drop column
                for (int i=0; i<resAPPList.length; ++i) {

                }
            case 2:// rename column
                for (int i=0; i<resAPPList.length; ++i) {

                }

        }
        ArrayList<variables> bindVarList = findBindingVar(resAPPList);

    }
}
