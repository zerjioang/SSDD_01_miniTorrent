package code.spec.persistent;

import es.deusto.ssdd.code.spec.persistent.PersistenceHandler;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by .local on 08/11/2016.
 */
public class PersistenceHandlerTest {

    private PersistenceHandler handler;

    @Before
    public void load() {
        handler = PersistenceHandler.getInstance();
    }

    @Test
    public void init() {
        assertEquals(handler.isLoaded(), true);
    }
}
