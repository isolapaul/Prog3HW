package hu.prog3.offlinechatprog3.persistence;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileManagerTest {

    @Test
    void saveAndLoadRoundtrip() {
        DataStore store = new DataStore();
        store.registerUser("x","p");
        File tmp = new File(System.getProperty("java.io.tmpdir"), "ds-test.dat");
        try {
            assertTrue(FileManager.save(store, tmp));
            DataStore loaded = FileManager.load(tmp);
            assertNotNull(loaded);
            assertEquals(store.userCount(), loaded.userCount());
        } finally {
            tmp.delete();
        }
    }
    
    @org.junit.jupiter.api.Test
    void testOutgoingRequestPersistence() throws Exception {
        File tmp = File.createTempFile("ds-test-2", ".dat");
        tmp.deleteOnExit();
        DataStore ds = new DataStore();
        ds.registerUser("a", "1");
        ds.registerUser("b", "2");
        assertTrue(ds.sendFriendRequest("a", "b"));
        // save
        FileManager.save(ds, tmp);
        DataStore loaded = FileManager.load(tmp);
        assertNotNull(loaded);
        assertFalse(loaded.getOutgoingFriendRequests("a").isEmpty());
        assertTrue(loaded.getIncomingFriendRequests("b").contains("a"));
    }
}
