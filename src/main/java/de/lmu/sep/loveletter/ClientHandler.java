package de.lmu.sep.loveletter;

import com.google.gson.JsonParseException;
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
  private static final String HELP_TEXT = String.join("\n",
      "commands:",
      "  /msg <name> <text>   send a private message to one user",
      "  /help                show this list",
      "  /bye                 leave the chat (bye works too)",
      "anything else is sent to everyone");

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
      Message message = parse(line);
      if (message == null || !Message.LOGIN.equals(message.getType())) {
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
   * commands, see {@link #handleCommand(String)}.
   *
   * @throws IOException if reading fails
   */
  private void chat() throws IOException {
    String line;
    while ((line = in.readLine()) != null) {
      Message message = parse(line);
      // Without this check, broken JSON or a missing text would crash this thread.
      if (message == null || !Message.INPUT.equals(message.getType())
          || message.getText() == null) {
        sendError("invalid message: expected type INPUT with a text");
        continue;
      }
      String text = message.getText().trim();
      if (text.equals("bye") || text.equals("/bye")) {
        return;
      }
      if (text.startsWith("/")) {
        handleCommand(text);
      } else {
        server.broadcast(new Message(Message.CHAT, name, text), null);
      }
    }
  }

  /**
   * Runs one command, for example "/msg ben hello". Unknown commands get an error.
   *
   * @param text the whole input line, starting with "/"
   */
  private void handleCommand(String text) {
    // At most 3 parts: the command, the first argument and the rest of the line, so the text
    // of a private message keeps its spaces.
    String[] parts = text.split("\\s+", 3);
    switch (parts[0]) {
      case "/msg" -> sendPrivate(parts);
      case "/help" -> send(new Message(Message.INFO, null, HELP_TEXT));
      default -> sendError("unknown command: " + parts[0] + ", type /help for a list");
    }
  }

  /**
   * Sends a private message to one user and confirms it to the sender.
   *
   * @param parts "/msg", the receiver and the text
   */
  private void sendPrivate(String[] parts) {
    if (parts.length < 3) {
      sendError("usage: /msg <name> <text>");
      return;
    }
    String receiver = parts[1];
    if (receiver.equals(name)) {
      sendError("you cannot send a private message to yourself");
      return;
    }
    if (!server.sendTo(receiver, new Message(Message.PRIVATE, name, parts[2]))) {
      sendError("no user named " + receiver + " is online");
      return;
    }
    send(new Message(Message.INFO, null, "private message sent to " + receiver));
  }

  private void sendError(String text) {
    send(new Message(Message.ERROR, null, text));
  }

  /**
   * Reads one line of JSON without crashing on bad input.
   *
   * @param line the line from the client
   * @return the message, or null if the line is empty or not valid JSON
   */
  private static Message parse(String line) {
    try {
      return Message.fromJson(line);
    } catch (JsonParseException e) {
      return null;
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
