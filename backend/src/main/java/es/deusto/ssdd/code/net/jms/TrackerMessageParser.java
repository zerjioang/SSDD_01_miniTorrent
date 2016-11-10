package es.deusto.ssdd.code.net.jms;

import javax.jms.Message;

/**
 * Created by .local on 08/11/2016.
 */
public interface TrackerMessageParser {

    void parseMessageContent(Message message);
}
