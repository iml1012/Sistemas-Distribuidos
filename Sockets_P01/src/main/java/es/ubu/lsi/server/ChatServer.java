/**
 *
 */
package es.ubu.lsi.server;

import java.io.IOException;

import es.ubu.lsi.common.ChatMessage;
/**
 *
 */
public interface ChatServer {
	void startup() throws IOException;
    void shutdown();
    void broadcast(ChatMessage message);
    void remove(int id);
}
