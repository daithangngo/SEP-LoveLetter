package de.lmu.sep.loveletter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Talks to one connected client on the server side. Runs in its own thread:
 * first the login, then the chat, and at the end the cleanup.
 */
public class ClientHandler implements Runnable {

  private static final String INVALID_NAME_TEXT =
      "invalid name: must not be empty, contain spaces or start with /";

  private final Socket socket;
  private final Server server;
  private BufferedReader in;
  private PrintWriter out;
  private String name;

  /**
   * Creates a handler. Only stores the references; the streams are opened in {@link #run()}.
   *
   * @param socket the connection to the client
   * @param server the server that keeps all clients
   */
  public ClientHandler(Socket socket, Server server) {
    this.socket = socket;
    this.server = server;
  }

  /** Handles the whole connection: login, chat, cleanup. */
  @Override
  public void run() {
    try {
      in = new BufferedReader(
          new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
      out = new PrintWriter(
          new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
      if (login()) {
        chat();
      }
    } catch (IOException e) {
      // The connection broke. The finally block cleans up the same way as after "bye".
    } finally {
      disconnect();
    }
  }

  /**
   * Sends one message to this client. Synchronized because several handler threads may
   * send to the same client at once and their lines must not get mixed up.
   *
   * @param message the message to send
   */
  public synchronized void send(Message message) {
    out.println(message.toJson());
  }

  /**
   * Reads LOGIN messages until a name is accepted.
   *
   * @return true if the client is logged in, false if the connection ended before
   * @throws IOException if reading fails
   */
  private boolean login() throws IOException {
    String line;
    while ((line = in.readLine()) != null) {
      Message message = Message.fromJson(line);
      if (!Message.LOGIN.equals(message.getType())) {
        send(new Message(Message.LOGIN_ERROR, null, "please log in first"));
        continue;
      }
      String requestedName = message.getName() == null ? "" : message.getName().trim();
      if (!isValidName(requestedName)) {
        send(new Message(Message.LOGIN_ERROR, null, INVALID_NAME_TEXT));
        continue;
      }
      // Holding this handler's lock blocks messages from other threads to this client
      // until LOGIN_OK is sent, so LOGIN_OK is really the client's first answer.
      synchronized (this) {
        if (!server.register(requestedName, this)) {
          send(new Message(Message.LOGIN_ERROR, null, "name already taken: " + requestedName));
          continue;
        }
        name = requestedName;
        send(new Message(Message.LOGIN_OK, null, null));
        send(new Message(Message.INFO, null, "welcome " + name));
      }
      server.broadcast(new Message(Message.INFO, null, name + " joined the room"), this);
      return true;
    }
    return false;
  }

  /**
   * Reads the input of a logged-in client until "bye" or the end of the connection.
   * Normal lines go to all clients, the sender included. Lines starting with "/" are
   * commands; "/bye" is the only one, every other command gets an error.
   *
   * @throws IOException if reading fails
   */
  private void chat() throws IOException {
    String line;
    while ((line = in.readLine()) != null) {
      String text = Message.fromJson(line).getText().trim();
      if (text.equals("bye") || text.equals("/bye")) {
        return;
      }
      if (text.startsWith("/")) {
        send(new Message(Message.ERROR, null, "unknown command: " + text));
      } else {
        server.broadcast(new Message(Message.CHAT, name, text), null);
      }
    }
  }

  /**
   * Cleans up after "bye" and after a broken connection alike: frees the name,
   * tells the others and closes the connection.
   */
  private void disconnect() {
    if (name != null) {
      server.remove(name);
      server.broadcast(new Message(Message.INFO, null, name + " left the room"), this);
    }
    try {
      socket.close();
    } catch (IOException e) {
      // Nothing left to do if closing fails; the connection is gone anyway.
    }
  }

  private static boolean isValidName(String name) {
    return !name.isEmpty() && !name.contains(" ") && !name.startsWith("/");
  }
}
