package de.lmu.sep.loveletter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Console chat client. The main thread reads the console and sends to the server;
 * after the login a second thread prints everything the server sends.
 */
public class Client {

  private final String host;
  private final int port;
  private final BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
  private BufferedReader in;
  private PrintWriter out;

  /**
   * Creates a client. Does not connect yet, see {@link #start()}.
   *
   * @param host the server host
   * @param port the server port
   */
  public Client(String host, int port) {
    this.host = host;
    this.port = port;
  }

  /**
   * Connects, logs in and then sends every console line as chat input.
   *
   * @throws IOException if the connection cannot be opened or reading the console fails
   */
  public void start() throws IOException {
    try (Socket socket = new Socket(host, port)) {
      in = new BufferedReader(
          new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
      out = new PrintWriter(
          new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
      if (!login()) {
        return;
      }
      new Thread(this::readMessages).start();
      sendConsoleLines();
    }
  }

  /**
   * Asks for a name and sends it until the server accepts it.
   *
   * @return true if logged in, false if the console or the connection ended before
   * @throws IOException if reading fails
   */
  private boolean login() throws IOException {
    while (true) {
      System.out.print("Name: ");
      String name = console.readLine();
      if (name == null) {
        return false;
      }
      out.println(new Message(Message.LOGIN, name, null).toJson());
      // Only the main thread reads from the server at this point, so the next line
      // is the answer to our LOGIN.
      String answer = in.readLine();
      if (answer == null) {
        System.out.println("Server closed the connection.");
        return false;
      }
      Message reply = Message.fromJson(answer);
      if (Message.LOGIN_OK.equals(reply.getType())) {
        return true;
      }
      System.out.println("Login failed: " + reply.getText());
    }
  }

  /** Sends every console line to the server until the console ends. */
  private void sendConsoleLines() throws IOException {
    String line;
    while ((line = console.readLine()) != null) {
      out.println(new Message(Message.INPUT, null, line).toJson());
    }
  }

  /** Runs in its own thread: prints server messages; ends the program when the server is gone. */
  private void readMessages() {
    try {
      String line;
      while ((line = in.readLine()) != null) {
        System.out.println(format(Message.fromJson(line)));
      }
    } catch (IOException e) {
      // A closed socket ends reading just like the end of the stream.
    }
    System.out.println("Connection closed.");
    System.exit(0);
  }

  /**
   * Turns a server message into the text shown on the console.
   *
   * @param message the message from the server
   * @return the text to print
   */
  static String format(Message message) {
    if (Message.CHAT.equals(message.getType())) {
      return message.getName() + ": " + message.getText();
    }
    if (Message.ERROR.equals(message.getType())) {
      return "Error: " + message.getText();
    }
    return message.getText();
  }
}
