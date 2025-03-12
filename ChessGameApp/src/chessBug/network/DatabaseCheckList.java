package chessBug.network;

import java.util.ArrayList;

public class DatabaseCheckList {
    private ArrayList<DatabaseCheck> databaseCheckList = new ArrayList<>();
    
    //Methods from arrayList
    public int size(){return databaseCheckList.size();}
    public void add(DatabaseCheck item){databaseCheckList.add(item);}
    public void clear(){databaseCheckList.clear();}
    
    //Other methods
    public void preformChecks(){databaseCheckList.forEach(item -> item.check());}
}
