/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import Shared.Data.DataRequest;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.ISortedData;
import Shared.Data.SortedData;
import Shared.Data.Status;
import Shared.Tag;
import Shared.Data.UnsortedData;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author Linda
 */
public class DatabaseManager {

    private DataDBManager dataManager;
    private TasksDBManager tasksManager;

    public DatabaseManager(){
        this.dataManager = new DataDBManager();
        this.tasksManager = new TasksDBManager();
    }

    // ============================= DATA MANAGER =================================================

    public Object getFromUnsortedData() {
        return dataManager.getFromUnsorted();
    }

    public Object getFromSortedData(HashSet tags) {
        return dataManager.getFromSorted(tags);
    }

    public boolean insertToSortedData(ISortedData data) {
        return dataManager.insertToSorted(data);
    }

    public boolean insertToUnsortedData(IData data) {
        return dataManager.insertToUnsorted(data);
    }

    public Object resetUnsortedData(List<IData> list) {
        return dataManager.resetUnsorted(list);
    }

    public Object updateUnsortedData(IData iData) {
        return dataManager.updateUnsorted(iData);
    }

    public Object discardUnsortedData(IData iData) {
        return dataManager.discardUnsorted(iData);
    }

    public boolean insertDataRequest(IDataRequest data) {
        return dataManager.insertRequest(data);
    }

    public Object getUpdateRequests(HashSet tags) {
        return dataManager.getRequests(tags);
    }

    public Object getDataItem(int i) {
        return dataManager.getUnsortedItem(i);
    }

    public Object getSentData(String toString) {
        return dataManager.getSent(toString);
    }
}
