/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import ServerApp.ServerMain;
import Shared.Data.SortedData;
import Shared.Tag;
import Shared.Tasks.IStep;
import Shared.Tasks.Plan;
import Shared.Tasks.Step;
import Shared.Tasks.Task;
import Shared.Tasks.TaskStatus;
import java.util.HashSet;
import java.util.TreeSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Kargathia
 */
public class TasksDatabaseManagerTest {

    private static TasksDatabaseManager myDB;

    private Plan plan;

    public TasksDatabaseManagerTest() {
        HashSet<String> keywords = new HashSet<>();
        keywords.add("keyword");

        TreeSet<IStep> steps = new TreeSet<>();
        steps.add(new Step(-1, "title", "desc", TaskStatus.UNASSIGNED,
                null, Tag.POLICE, null, 1, "condition"));

        plan = new Plan(-1, "title", "description", keywords, steps, true);
    }

    @BeforeClass
    public static void setUpClass(){
        ServerMain.startDatabases();
        myDB = ServerMain.tasksDatabaseManager;
        myDB.resetDatabase();
    }

    @AfterClass
    public static void tearDownClass(){
        myDB.resetDatabase();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testInsertNewTask() {
        Task task = new Task(-1, "title", "desc", TaskStatus.UNASSIGNED, null, Tag.POLICE, null);
        assertNotNull("Unable to insert barebones task", myDB.insertNewTask(task));

        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.POLICE);
        SortedData data = new SortedData(1, "title", "description",
                "location", "source", 1, 2, 3, tags);
        task = new Task(-1, "title", "desc", TaskStatus.UNASSIGNED, data, Tag.AMBULANCE, null);
        assertNotNull("Unable to insert full task", myDB.insertNewTask(task));
    }

    @Test
    public void testInsertNewPlan() {
        assertNotNull("Unable to insert new plan", myDB.insertNewPlan(plan));
        assertNotNull("Unable to insert other new plan", myDB.insertNewPlan(plan));
    }

}
