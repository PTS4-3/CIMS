/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import ServerApp.ServerMain;
import Shared.Data.SortedData;
import Shared.Tag;
import Shared.Tasks.IPlan;
import Shared.Tasks.IStep;
import Shared.Tasks.ITask;
import Shared.Tasks.Plan;
import Shared.Tasks.Step;
import Shared.Tasks.Task;
import Shared.Tasks.TaskStatus;
import Shared.Users.IServiceUser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

        List<IStep> steps = new ArrayList<>();
        steps.add(new Step(-1, "title", "desc", TaskStatus.UNASSIGNED,
                null, Tag.POLICE, null, 1, "condition"));

        plan = new Plan(-1, "title", "description", keywords, steps, true);
    }

    @BeforeClass
    public static void setUpClass() {
        if (ServerMain.connectionManager == null) {
            ServerMain.startDatabases();
        }
        myDB = ServerMain.tasksDatabaseManager;
        myDB.resetDatabase();
        ServerMain.sortedDatabaseManager.resetDatabase();
    }

    @AfterClass
    public static void tearDownClass() {
        myDB.resetDatabase();
        ServerMain.sortedDatabaseManager.resetDatabase();
    }

    @Before
    public void setUp() {
        myDB.resetDatabase();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testTasks() {
        // insert new task
        Task task = new Task(-1, "title", "desc", TaskStatus.UNASSIGNED, null, Tag.POLICE, null);
        assertNotNull("Unable to insert barebones task", myDB.insertNewTask(task));

        IServiceUser executor = (IServiceUser) myDB.getUser("firefighter01");
        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.POLICE);
        SortedData data = new SortedData(100, "title", "description",
                "location", "source", 1, 2, 3, tags);
        assertTrue("failed to insert sorted data",
                ServerMain.sortedDatabaseManager.insertToSortedData(data));
        task = new Task(-1, "title", "desc", TaskStatus.UNASSIGNED, data,
                Tag.FIREDEPARTMENT, executor);
        task = (Task) myDB.insertNewTask(task);
        assertNotNull("Unable to insert full task", task);

        // set task status
        task.setStatus(TaskStatus.INPROCESS);
        assertTrue("failed to update task status", myDB.setTaskStatus(task));

        // get task
        Task task2 = (Task) myDB.getTask(task.getId());
        assertNull("declineReason was not null",
                task2.getDeclineReason());
        assertTrue("different Description",
                task.getDescription().equals(task2.getDescription()));
        assertTrue("differentExecutorTag",
                task.getTargetExecutor() == task2.getTargetExecutor());
        assertTrue("different executor",
                task.getExecutor().getUsername().equals(task2.getExecutor().getUsername()));
        assertTrue("different sortedData ID",
                task.getSortedData().getId() == task2.getSortedData().getId());
        assertTrue("different sortedData title",
                task.getSortedData().getTitle().equals(task2.getSortedData().getTitle()));

        // get tasks
        List<ITask> tasks = myDB.getTasks(executor.getUsername());
        assertTrue("wrong number of tasks", tasks.size() == 3);

        // tests if tasks are correct
        for (ITask taskItem : tasks) {
            String expectedTitle = "?????";
            String expectedDescription = "??????";
            String expectedReason = "??????";
            Tag expectedTag = null;
            int expectedDataID = -9999;
            TaskStatus expectedStatus = null;
            boolean expectedHasData = true;

            if (taskItem.getId() == 3) {
                expectedTitle = "Zet ladder neer";
                expectedDescription = "Zet de ladder tegen de boom.";
                expectedReason = "Allergies voor katten";
                expectedTag = Tag.FIREDEPARTMENT;
                expectedHasData = false;
                expectedDataID = -1;
                expectedStatus = TaskStatus.SENT;
            } else if (taskItem.getId() == 4) {
                expectedTitle = "Kat pakken";
                expectedDescription = "Beklim de ladder en haal de kat uit de boom.";
                expectedReason = null;
                expectedTag = Tag.FIREDEPARTMENT;
                expectedHasData = false;
                expectedDataID = -1;
                expectedStatus = TaskStatus.REFUSED;
            } else if (taskItem.getId() == 11) {
                expectedTitle = task2.getTitle();
                expectedDescription = task2.getDescription();
                expectedReason = task2.getDeclineReason();
                expectedTag = task2.getTargetExecutor();
                expectedHasData = true;
                expectedDataID = task2.getSortedData().getId();
                expectedStatus = task2.getStatus();
            } else {
                fail("no recognized task ID");
            }

            assertEquals("different title", expectedTitle, taskItem.getTitle());
            assertEquals("different description",
                    expectedDescription, taskItem.getDescription());
            assertEquals("different decline reason",
                    expectedReason, taskItem.getDeclineReason());
            assertEquals("different tag", expectedTag, taskItem.getTargetExecutor());
            if (expectedHasData) {
                assertEquals("different Data",
                        expectedDataID, taskItem.getSortedData().getId());
            } else {
                assertNull("has data when he shouldn't", taskItem.getSortedData());
            }
            assertEquals("different status", expectedStatus, taskItem.getStatus());
        }

        // get sorted data tasks
        tasks = myDB.getSortedDataTasks(data);
        assertEquals("wrong number of tasks", 1, tasks.size());
        task = (Task) tasks.get(0);
        assertEquals("wrong data ID", data.getId(), task.getSortedData().getId());
        assertEquals("wrong title", task2.getTitle(), task.getTitle());
        assertEquals("wrong description", task2.getDescription(), task.getDescription());
        assertEquals("wrong decline reason", task2.getDeclineReason(), task.getDeclineReason());
        assertEquals("wrong tag", task2.getTargetExecutor(), task.getTargetExecutor());
        assertEquals("wrong status", task2.getStatus(), task.getStatus());
    }

    @Test
    public void setTaskStatus() {
        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.POLICE);
        SortedData data = new SortedData(1, "title", "description",
                "location", "source", 1, 2, 3, tags);
        Task task = new Task(-1, "title", "desc", TaskStatus.UNASSIGNED, data,
                Tag.AMBULANCE, null);
        task = (Task) myDB.insertNewTask(task);

    }

    @Test
    public void testPlans() {
        assertNotNull("Unable to insert new plan", myDB.insertNewPlan(plan));
        assertNotNull("Unable to insert other new plan", myDB.insertNewPlan(plan));

        List<IPlan> plans = myDB.getPlans(new HashSet<>());
        assertTrue("Not all plans were retrieved", plans.size() == 4);

        for (IPlan planItem : plans) {
            String expectedTitle = "??????????";
            String expectedDesc = "???????????";
            int expectedKeywordsCount = -1;
            int expectedStepCount = -1;
            // sets expected
            if (planItem.getId() == 1) {
                expectedTitle = "Brand";
                expectedDesc = "Brand in een gebouw met 3 verdiepingen.";
                expectedKeywordsCount = 3;
                expectedStepCount = 2;
            } else if (planItem.getId() == 2) {
                expectedTitle = "Auto ongeluk";
                expectedDesc = "Er is een auto ongeluk op de snelweg. "
                        + "Er zijn geen gewonden, maar wel veel schade aan de"
                        + " autos en troep op de weg.";
                expectedKeywordsCount = 6;
                expectedStepCount = 4;
            } else if (planItem.getId() == 3 || planItem.getId() == 4){
                expectedTitle = plan.getTitle();
                expectedDesc = plan.getDescription();
                expectedKeywordsCount = plan.getKeywords().size();
                expectedStepCount = plan.getSteps().size();
            } else {
                fail("id fail (testGetPlans): " + planItem.getId());
            }

            // checks values
            assertEquals("title fail (testGetPlans)", expectedTitle, planItem.getTitle());
            assertEquals("desc fail (testGetPlans)", expectedDesc, planItem.getDescription());
            assertEquals("keyword count fail (testGetPlans)", expectedKeywordsCount,
                    planItem.getKeywords().size());
            assertEquals("step count fail (testGetPlans)", expectedStepCount,
                    planItem.getSteps().size());
        }
    }

    @Test
    public void testLoginUser() {
    }

    @Test
    public void testGetUser() {

    }

    @Test
    public void testGetServiceUsers() {

    }

}
