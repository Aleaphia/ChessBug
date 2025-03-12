package chessBug.controllerInterfaces;

import chessBug.network.DatabaseCheck;

public interface IDatabaseCheckController {
    /** addToDatbaseCheckList - adds a DatabaseCheck to the list of Database Checks to be performed
     * @param - item : lambda function for tasks to be preformed during database check
     */
    public void addToDatabaseCheckList(DatabaseCheck item);
}
