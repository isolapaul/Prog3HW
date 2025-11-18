package hu.prog3.offlinechatprog3.persistence;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileManagerTest {

    @Test
    void saveAndLoadRoundtrip() {
        DataStore store = new DataStore();
        store.registerUser("bob","mypass");
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
        ds.registerUser("isolapaul", "pass1");
        ds.registerUser("DondiDuo", "pass2");
        assertTrue(ds.sendFriendRequest("isolapaul", "DondiDuo"));
        FileManager.save(ds, tmp);
        DataStore loaded = FileManager.load(tmp);
        assertNotNull(loaded);
        assertFalse(loaded.getOutgoingFriendRequests("isolapaul").isEmpty());
        assertTrue(loaded.getIncomingFriendRequests("DondiDuo").contains("isolapaul"));
    }
}
