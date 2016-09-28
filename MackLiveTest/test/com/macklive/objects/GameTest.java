package com.macklive.objects;

import java.util.Date;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.macklive.exceptions.EntityMismatchException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Nick on 7/24/16.
 */
public class GameTest {

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    Game g;

    @Before
    public void setup() {
        this.helper.setUp();
        this.g = new Game(new Team("T1", "T1"), new Team("T2", "T2"));
    }

    @Test
    public void testLastModified() throws InterruptedException {
        assertTrue(this.g.getLastUpdated().after(new Date(0)));

        Date orig = this.g.getLastUpdated();
        Thread.sleep(2000);
        this.g.setPeriod(2);
        assertTrue(this.g.getLastUpdated().after(orig));
    }

    @Test
    public void testsNewKey() throws EntityMismatchException {
        assertNull(this.g.getKey());
    }

    @Test
    public void testValidJson() {
        String json = this.g.toJSON();
        try {
            JSONObject jso = new JSONObject(json);
            assertEquals("Incorrect number of JSON keys", 13, jso.length());
        } catch (Exception e) {
            fail("Syntax error in JSON");
        }
    }

    @Test(expected = EntityMismatchException.class)
    public void testEntityMismatch() throws EntityMismatchException {
        Entity e = new Message("Me", "text", 0, false).getEntity();
        new Game(e);
    }
}
