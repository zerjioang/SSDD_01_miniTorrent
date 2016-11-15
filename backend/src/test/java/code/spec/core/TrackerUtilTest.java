package code.spec.core;

import es.deusto.ssdd.code.net.bittorrent.core.TrackerUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by .local on 08/11/2016.
 */
public class TrackerUtilTest {

    @Test
    public void testTrackerMacAddress() {
        assertEquals(TrackerUtil.getDeviceMacAddress(), "60-57-18-F4-C4-CD");
    }
}
