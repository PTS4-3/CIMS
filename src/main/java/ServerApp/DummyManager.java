/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.DataRequest;
import Shared.IData;
import Shared.IDataRequest;
import Shared.ISortedData;
import Shared.SortedData;
import Shared.Status;
import Shared.Tag;
import Shared.UnsortedData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Kargathia
 */
public class DummyManager {

    private List<IData> unsortedData;
    private List<ISortedData> sortedData;
    private List<IDataRequest> dataRequests;

    public DummyManager() {
        unsortedData = new ArrayList<>();
        sortedData = new ArrayList<>();
        dataRequests = new ArrayList<>();

        HashSet<Tag> tags = new HashSet<>();
        tags.addAll(Arrays.asList(Tag.values()));
        // fill
        int i = 1;
        while (i < 50) {
            unsortedData.add(new UnsortedData(i, "title" + i, "description" + i, "location" + i, "source" + i, Status.NONE));
            sortedData.add(new SortedData(i, "title" + i, "description" + i, "location" + i, "source" + i, 1, 2, 3, tags));
            dataRequests.add(new DataRequest(i, "title" + i, "description" + i, "location" + i, "source" + i, i, tags));
            i++;
        }
    }

    List<IData> getFromUnsortedData() {
        System.out.println("getFromUnsortedData - returning list");
        return unsortedData;
    }

    List<ISortedData> getFromSortedData(HashSet<Tag> tags) {
        System.out.println("getFromSortedData - tags requested: ");
        for(Tag t: tags){
            System.out.println(t.toString());
        }
        System.out.println("---- end of tags");
        return sortedData;
    }

    boolean insertToSortedData(ISortedData data) {
        System.out.println("InsertToSortedData - title: " + data.getTitle());
        return true;
    }

    boolean insertToUnsortedData(IData data) {
        System.out.println("InsertToUnsortedData - title: " + data.getTitle());
        return true;
    }

    boolean resetUnsortedData(List<IData> list) {
        System.out.println("resetUnsortedData - amount: " + list.size());
        return true;
    }

    boolean updateUnsortedData(IData iData) {
        System.out.println("updateUnsortedData - title: " +  iData.getTitle()
                + " - id: " + iData.getId());
        return true;
    }

    boolean discardUnsortedData(IData iData) {
        System.out.println("discardUnsortedData - title: " + iData.getTitle());
        return true;
    }

    boolean insertDataRequest(IDataRequest data) {
        System.out.println("insertDataRequest - title: " +  data.getTitle());
        return true;
    }

    List<IDataRequest> getUpdateRequests(HashSet<Tag> tags) {
        System.out.println("getUpdateRequests - tags: " );
        for(Tag t: tags){
            System.out.println(t.toString());
        }
        System.out.println("---- end of tags");
        return dataRequests;
    }

    IData getDataItem(int i) {
        System.out.println("getDataItem - id: " + i);
        return unsortedData.get(i - 1);
    }

    List<IData> getSentData(String source) {
        System.out.println("getSentData - source: " + source);
        return unsortedData;
    }

}
