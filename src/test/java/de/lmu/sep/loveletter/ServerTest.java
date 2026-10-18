package de.lmu.sep.loveletter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the name registry of {@link Server} without any network. */
class ServerTest {

  @Test
  void duplicateNameIsRejected() {
    Server server = new Server(0);
    ClientHandler first = new ClientHandler(null, server);
    ClientHandler second = new ClientHandler(null, server);

    assertTrue(server.register("anna", first));
    assertFalse(server.register("anna", second));
  }

  @Test
  void nameIsFreeAgainAfterRemove() {
    Server server = new Server(0);
    ClientHandler first = new ClientHandler(null, server);
    ClientHandler second = new ClientHandler(null, server);

    server.register("anna", first);
    server.remove("anna");

    assertTrue(server.register("anna", second));
  }

  @Test
  void namesAreComparedExactly() {
    Server server = new Server(0);

    assertTrue(server.register("anna", new ClientHandler(null, server)));
    assertTrue(server.register("Anna", new ClientHandler(null, server)));
  }
}
