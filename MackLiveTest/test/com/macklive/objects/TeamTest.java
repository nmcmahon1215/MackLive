package com.macklive.objects;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.macklive.exceptions.EntityMismatchException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Nick on 7/24/16.
 */
public class TeamTest {

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    private Team t;

    @Before
    public void setup() {
        this.helper.setUp();
        this.t = new Team("T1", "TA");
    }

    @Test
    public void testEntity() throws EntityMismatchException {
        Entity e = this.t.getEntity();
        this.t.setKey(e.getKey());
        Team t2 = new Team(e);
        assertEquals("Entity save/load error", this.t, t2);
    }


    @Test(expected = EntityMismatchException.class)
    public void testEntityMismatch() throws EntityMismatchException {
        Entity e = new Message("Me", "text", 0, false).getEntity();
        new Team(e);
    }
}
