package de.lmu.sep.loveletter;

import java.io.IOException;
import java.util.Arrays;
import javafx.application.Application;

/**
 * Entry point. Starts the server, the console client or the JavaFX chat window, depending on
 * the first argument.
 *
 * <p>Usage: {@code server [port]}, {@code client [host] [port]} or {@code gui [host] [port]}.
 *
 * <p>App deliberately does not extend {@link Application}. If the main class of a program is
 * an Application, the java launcher refuses to start unless JavaFX is on the module path
 * ("JavaFX runtime components are missing"). In our jar JavaFX is on the normal classpath,
 * so a separate main class that calls {@link Application#launch} is needed.
 */
public class App {

  static final int DEFAULT_PORT = 1337;
  static final String DEFAULT_HOST = "localhost";

  /**
   * Reads the mode from the arguments and starts it.
   *
   * @param args {@code server [port]}, {@code client [host] [port]} or {@code gui [host] [port]}
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      printUsage();
      return;
    }
    String mode = args[0];
    try {
      if (mode.equals("server")) {
        int port = DEFAULT_PORT;
        if (args.length > 1) {
          port = Integer.parseInt(args[1]);
        }
        new Server(port).start();
      } else if (mode.equals("client")) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        if (args.length > 1) {
          host = args[1];
        }
        if (args.length > 2) {
          port = Integer.parseInt(args[2]);
        }
        new Client(host, port).start();
      } else if (mode.equals("gui")) {
        // Pass only what comes after "gui" (host and port); ChatWindow reads it with
        // getParameters().getRaw(). launch() returns when the window is closed.
        String[] guiArgs = Arrays.copyOfRange(args, 1, args.length);
        Application.launch(ChatWindow.class, guiArgs);
      } else {
        printUsage();
      }
    } catch (IOException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  private static void printUsage() {
    System.out.println("Usage: java -jar loveletter-jar-with-dependencies.jar server [port]");
    System.out.println("       java -jar loveletter-jar-with-dependencies.jar client [host] [port]");
    System.out.println("       java -jar loveletter-jar-with-dependencies.jar gui [host] [port]");
  }
}
