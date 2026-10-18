package de.lmu.sep.loveletter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Minimal JavaFX chat window: a graphical client for the same server and protocol as
 * {@link Client}. One read-only area shows the messages, one field takes the input.
 * Like in the console client, the first line entered is the name, every later line is chat.
 *
 * <p>Two threads are involved: the JavaFX thread builds the window and handles Enter, and a
 * reading thread waits for lines from the server. Only the JavaFX thread may change the
 * window, so the reading thread hands every message over with
 * {@link Platform#runLater(Runnable)}.
 */
public class ChatWindow extends Application {

  private final TextArea messages = new TextArea();
  private final TextField input = new TextField();
  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;
  // Only used on the JavaFX thread: false sends the input as LOGIN, true as INPUT.
  private boolean loggedIn;

  /**
   * Called by JavaFX on the JavaFX thread after {@link Application#launch}. Builds the window,
   * shows it and connects to the server.
   *
   * @param stage the window that JavaFX created for this application
   */
  @Override
  public void start(Stage stage) {
    // App passes only what comes after "gui": [host] [port].
    String host = App.DEFAULT_HOST;
    int port = App.DEFAULT_PORT;
    List<String> args = getParameters().getRaw();
    if (args.size() > 0) {
      host = args.get(0);
    }
    if (args.size() > 1) {
      port = Integer.parseInt(args.get(1));
    }

    messages.setEditable(false);
    input.setPromptText("Name");
    // Runs on the JavaFX thread when the user presses Enter in the field.
    input.setOnAction(event -> send());

    // The center of a BorderPane takes all the space that the bottom row leaves.
    BorderPane root = new BorderPane(messages);
    root.setBottom(input);
    stage.setTitle("Love Letter Chat");
    stage.setScene(new Scene(root, 400, 300));
    stage.show();
    input.requestFocus();

    connect(host, port);
  }

  /**
   * Called by JavaFX when the window is closed. Closing the socket ends the connection, so the
   * server frees the name and tells the others "name left the room".
   */
  @Override
  public void stop() throws IOException {
    // socket is null if connecting failed; then there is nothing to close.
    if (socket != null) {
      socket.close();
    }
  }

  /**
   * Opens the connection and starts the reading thread. If the server cannot be reached, the
   * reason is shown and the input field is locked.
   *
   * @param host the server host
   * @param port the server port
   */
  private void connect(String host, int port) {
    try {
      socket = new Socket(host, port);
      in = new BufferedReader(
          new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
      out = new PrintWriter(
          new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    } catch (IOException e) {
      messages.appendText("Cannot connect to " + host + ":" + port + ": " + e.getMessage() + "\n");
      input.setDisable(true);
      return;
    }
    // Reading blocks, so it needs its own thread or the window would freeze. A daemon thread
    // does not keep the program alive after the window is closed.
    Thread reader = new Thread(this::readMessages);
    reader.setDaemon(true);
    reader.start();
  }

  /**
   * Runs on the JavaFX thread (Enter). Sends the line as LOGIN before the login and as INPUT
   * after it, then clears the field.
   */
  private void send() {
    if (loggedIn) {
      out.println(new Message(Message.INPUT, null, input.getText()).toJson());
    } else {
      out.println(new Message(Message.LOGIN, input.getText(), null).toJson());
    }
    input.clear();
  }

  /** Runs in the reading thread. Hands every server message to the JavaFX thread. */
  private void readMessages() {
    try {
      String line;
      while ((line = in.readLine()) != null) {
        Message message = Message.fromJson(line);
        Platform.runLater(() -> show(message));
      }
    } catch (IOException e) {
      // A closed socket ends reading just like the end of the stream.
    }
    Platform.runLater(() -> {
      messages.appendText("Connection closed.\n");
      input.setDisable(true);
    });
  }

  /**
   * Runs on the JavaFX thread. LOGIN_OK switches the field to chat input; every other message
   * is shown the same way as in the console client.
   *
   * @param message the message from the server
   */
  private void show(Message message) {
    if (Message.LOGIN_OK.equals(message.getType())) {
      loggedIn = true;
      input.setPromptText("Message");
    } else {
      messages.appendText(Client.format(message) + "\n");
    }
  }
}
