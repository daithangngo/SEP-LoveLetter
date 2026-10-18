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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

/**
 * JavaFX chat window: a graphical client for the same server and protocol as {@link Client}.
 *
 * <p>The network code (socket, reader, writer, reading thread) is copied from {@link Client}
 * on purpose instead of changing {@link Client} so that both can use it. That keeps
 * {@link Client} unchanged and each class can be read and explained on its own.
 *
 * <p>Two threads are involved: the JavaFX thread builds the window and runs the button
 * handlers, and a reading thread waits for lines from the server. Only the JavaFX thread may
 * change the window, so the reading thread hands every message over with
 * {@link Platform#runLater(Runnable)}.
 */
public class ChatWindow extends Application {

  // Top row: name and "Join". Usable only before the login.
  private TextField nameField;
  private Button joinButton;
  // Middle: all messages, read-only.
  private TextArea messageArea;
  // Bottom row: chat input and "Send". Usable only after the login.
  private TextField inputField;
  private Button sendButton;

  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;

  /**
   * Called by JavaFX on the JavaFX thread after {@link Application#launch}. Builds the window,
   * shows it and connects to the server.
   *
   * @param stage the window that JavaFX created for this application
   */
  @Override
  public void start(Stage stage) {
    // Host and port come from the command line: "gui [host] [port]". App passes only the
    // values after "gui", and getRaw() returns them unchanged.
    String host = App.DEFAULT_HOST;
    int port = App.DEFAULT_PORT;
    List<String> args = getParameters().getRaw();
    if (args.size() > 0) {
      host = args.get(0);
    }
    if (args.size() > 1) {
      port = Integer.parseInt(args.get(1));
    }

    // Top: name field and Join button. The field grows with the window width.
    nameField = new TextField();
    nameField.setPromptText("Name");
    joinButton = new Button("Join");
    HBox top = new HBox(5, nameField, joinButton);
    HBox.setHgrow(nameField, Priority.ALWAYS);

    // Middle: the message area. Not editable, because it only shows what the server sends.
    messageArea = new TextArea();
    messageArea.setEditable(false);
    messageArea.setWrapText(true);

    // Bottom: input field and Send button.
    inputField = new TextField();
    sendButton = new Button("Send");
    HBox bottom = new HBox(5, inputField, sendButton);
    HBox.setHgrow(inputField, Priority.ALWAYS);

    // Before the login only the top row can be used; LOGIN_OK switches it around.
    setLoginEnabled(true);
    setChatEnabled(false);

    // Event handlers run on the JavaFX thread when the user clicks or presses Enter.
    // Each lambda just calls the method that does the work.
    joinButton.setOnAction(event -> join());
    sendButton.setOnAction(event -> send());
    inputField.setOnAction(event -> send()); // Enter in the input field also sends

    // Put the three parts into a BorderPane and show the window.
    BorderPane root = new BorderPane();
    root.setTop(top);
    root.setCenter(messageArea);
    root.setBottom(bottom);
    root.setPadding(new Insets(10));
    BorderPane.setMargin(messageArea, new Insets(10, 0, 10, 0));
    stage.setTitle("Love Letter Chat");
    stage.setScene(new Scene(root, 500, 400));
    stage.show();

    connect(host, port);
  }

  /**
   * Called by JavaFX when the window is closed. Closing the socket ends the connection, so the
   * server frees the name and tells the others "name left the room".
   */
  @Override
  public void stop() {
    // socket is null if connecting failed; then there is nothing to close.
    if (socket != null) {
      try {
        socket.close();
      } catch (IOException e) {
        // The program ends anyway; nothing left to do.
      }
    }
  }

  /**
   * Opens the connection and starts the reading thread. If the server cannot be reached, a
   * message is shown instead and the window stays open without crashing.
   *
   * @param host the server host
   * @param port the server port
   */
  private void connect(String host, int port) {
    // Open the socket with the same UTF-8 reader and writer as in Client.
    try {
      socket = new Socket(host, port);
      in = new BufferedReader(
          new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
      out = new PrintWriter(
          new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    } catch (IOException e) {
      // No server: show the reason and lock everything, because nothing can be sent.
      messageArea.appendText("Cannot connect to " + host + ":" + port + ": "
          + e.getMessage() + "\n");
      setLoginEnabled(false);
      return;
    }

    // Reading blocks until the server sends a line, so it needs its own thread; otherwise
    // the window would freeze. A daemon thread does not keep the program alive after the
    // window is closed. this::readMessages is the same method reference as in Client.
    Thread reader = new Thread(this::readMessages);
    reader.setDaemon(true);
    reader.start();
  }

  /** Runs on the JavaFX thread (Join button). Sends the entered name as LOGIN. */
  private void join() {
    // The server checks and trims the name and answers LOGIN_OK or LOGIN_ERROR.
    out.println(new Message(Message.LOGIN, nameField.getText(), null).toJson());
  }

  /**
   * Runs on the JavaFX thread (Send button or Enter). Sends the line as INPUT and clears the
   * field. "bye" and "/bye" are sent like any other line; the server then ends the connection.
   */
  private void send() {
    out.println(new Message(Message.INPUT, null, inputField.getText()).toJson());
    inputField.clear();
  }

  /**
   * Runs in the reading thread. Reads lines from the server until the connection ends and
   * hands each message to the JavaFX thread.
   */
  private void readMessages() {
    try {
      String line;
      while ((line = in.readLine()) != null) {
        Message message = Message.fromJson(line);
        // This thread must not touch the window. runLater queues the lambda, and the
        // JavaFX thread runs it as soon as it can.
        Platform.runLater(() -> showMessage(message));
      }
    } catch (IOException e) {
      // A closed socket ends reading just like the end of the stream.
    }
    // Stream ended (server closed the connection, e.g. after "bye"): tell the JavaFX thread.
    Platform.runLater(() -> connectionClosed());
  }

  /**
   * Runs on the JavaFX thread. Shows one message from the server and switches the fields
   * after a successful login.
   *
   * @param message the message from the server
   */
  private void showMessage(Message message) {
    String type = message.getType();
    if (Message.LOGIN_OK.equals(type)) {
      // Logged in: the name can no longer be changed, chatting is now possible.
      setLoginEnabled(false);
      setChatEnabled(true);
      inputField.requestFocus();
    } else if (Message.LOGIN_ERROR.equals(type)) {
      // Name rejected: show why; the top row stays enabled for the next try.
      messageArea.appendText("Login failed: " + message.getText() + "\n");
    } else if (Message.CHAT.equals(type)) {
      messageArea.appendText(message.getName() + ": " + message.getText() + "\n");
    } else if (Message.ERROR.equals(type)) {
      messageArea.appendText("Error: " + message.getText() + "\n");
    } else {
      // INFO, e.g. "welcome anna" or "ben joined the room".
      messageArea.appendText(message.getText() + "\n");
    }
  }

  /** Runs on the JavaFX thread when the connection has ended. Locks all input. */
  private void connectionClosed() {
    messageArea.appendText("Connection closed.\n");
    setLoginEnabled(false);
    setChatEnabled(false);
  }

  /**
   * Enables or disables the top row (name field and Join button).
   *
   * @param enabled true to allow input
   */
  private void setLoginEnabled(boolean enabled) {
    nameField.setDisable(!enabled);
    joinButton.setDisable(!enabled);
  }

  /**
   * Enables or disables the bottom row (input field and Send button).
   *
   * @param enabled true to allow input
   */
  private void setChatEnabled(boolean enabled) {
    inputField.setDisable(!enabled);
    sendButton.setDisable(!enabled);
  }
}
