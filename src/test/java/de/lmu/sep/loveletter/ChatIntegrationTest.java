package de.lmu.sep.loveletter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Starts a real server on a free port and talks to it over sockets, like the real clients do.
 * Every test gets its own server, so the tests cannot see each other's messages.
 */
class ChatIntegrationTest {

  private int port;

  @BeforeEach
  void startServer() throws IOException {
    try (ServerSocket probe = new ServerSocket(0)) {
      port = probe.getLocalPort();
    }
    Thread serverThread = new Thread(() -> {
      try {
        new Server(port).start();
      } catch (IOException e) {
        // connect() fails then, which makes the test fail with a clear message.
      }
    });
    // A daemon thread does not keep the test JVM alive after the tests.
    serverThread.setDaemon(true);
    serverThread.start();
  }

  @Test
  void newClientIsWelcomed() throws Exception {
    try (Socket anna = connect()) {
      BufferedReader annaIn = reader(anna);
      PrintWriter annaOut = writer(anna);

      send(annaOut, Message.LOGIN, "anna", null);

      assertEquals(Message.LOGIN_OK, receive(annaIn).getType());
      assertEquals("welcome anna", receive(annaIn).getText());
    }
  }

  @Test
  void duplicateNameIsRejectedAndClientCanTryAgain() throws Exception {
    try (Socket anna = connect(); Socket second = connect()) {
      login(reader(anna), writer(anna), "anna");
      BufferedReader secondIn = reader(second);
      PrintWriter secondOut = writer(second);

      send(secondOut, Message.LOGIN, "anna", null);
      assertEquals(Message.LOGIN_ERROR, receive(secondIn).getType());

      login(secondIn, secondOut, "ben");
    }
  }

  @Test
  void invalidNamesAreRejected() throws Exception {
    try (Socket client = connect()) {
      BufferedReader in = reader(client);
      PrintWriter out = writer(client);

      send(out, Message.LOGIN, "   ", null);
      assertEquals(Message.LOGIN_ERROR, receive(in).getType());
      send(out, Message.LOGIN, "anna maria", null);
      assertEquals(Message.LOGIN_ERROR, receive(in).getType());
      send(out, Message.LOGIN, "/anna", null);
      assertEquals(Message.LOGIN_ERROR, receive(in).getType());

      login(in, out, "  anna  ");
    }
  }

  @Test
  void othersAreToldWhenSomeoneJoins() throws Exception {
    try (Socket anna = connect(); Socket ben = connect()) {
      BufferedReader annaIn = reader(anna);
      login(annaIn, writer(anna), "anna");

      login(reader(ben), writer(ben), "ben");

      assertEquals("ben joined the room", receive(annaIn).getText());
    }
  }

  @Test
  void chatLineReachesEveryoneIncludingSender() throws Exception {
    try (Socket anna = connect(); Socket ben = connect(); Socket carl = connect()) {
      BufferedReader annaIn = reader(anna);
      PrintWriter annaOut = writer(anna);
      BufferedReader benIn = reader(ben);
      BufferedReader carlIn = reader(carl);
      // Each join message is read before the next login, so their order is fixed
      // and the next message after them is the chat line.
      login(annaIn, annaOut, "anna");
      login(benIn, writer(ben), "ben");
      assertEquals("ben joined the room", receive(annaIn).getText());
      login(carlIn, writer(carl), "carl");
      assertEquals("carl joined the room", receive(annaIn).getText());
      assertEquals("carl joined the room", receive(benIn).getText());

      send(annaOut, Message.INPUT, null, "hello all");

      assertChat(receive(annaIn), "anna", "hello all");
      assertChat(receive(benIn), "anna", "hello all");
      assertChat(receive(carlIn), "anna", "hello all");
    }
  }

  private static void assertChat(Message message, String name, String text) {
    assertEquals(Message.CHAT, message.getType());
    assertEquals(name, message.getName());
    assertEquals(text, message.getText());
  }

  /** Logs in and checks LOGIN_OK and the welcome text. */
  private static void login(BufferedReader in, PrintWriter out, String name) throws IOException {
    send(out, Message.LOGIN, name, null);
    assertEquals(Message.LOGIN_OK, receive(in).getType());
    assertEquals("welcome " + name.trim(), receive(in).getText());
  }

  /** Connects to the test server; retries for a short time because it starts in another thread. */
  private Socket connect() throws Exception {
    for (int attempt = 0; attempt < 50; attempt++) {
      try {
        Socket socket = new Socket("localhost", port);
        // Without a timeout a missing message would make the test hang forever.
        socket.setSoTimeout(5000);
        return socket;
      } catch (ConnectException e) {
        Thread.sleep(100);
      }
    }
    throw new IllegalStateException("server did not start on port " + port);
  }

  private static BufferedReader reader(Socket socket) throws IOException {
    return new BufferedReader(
        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
  }

  private static PrintWriter writer(Socket socket) throws IOException {
    return new PrintWriter(
        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
  }

  private static void send(PrintWriter out, String type, String name, String text) {
    out.println(new Message(type, name, text).toJson());
  }

  private static Message receive(BufferedReader in) throws IOException {
    String line = in.readLine();
    assertNotNull(line, "connection was closed");
    return Message.fromJson(line);
  }
}
