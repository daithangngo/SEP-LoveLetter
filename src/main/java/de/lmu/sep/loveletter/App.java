package de.lmu.sep.loveletter;

import java.io.IOException;

/**
 * Entry point. Starts either the server or a client, depending on the first argument.
 *
 * <p>Usage: {@code server [port]} or {@code client [host] [port]}.
 */
public class App {

  static final int DEFAULT_PORT = 1337;
  static final String DEFAULT_HOST = "localhost";

  /**
   * Reads the mode from the arguments and starts it.
   *
   * @param args {@code server [port]} or {@code client [host] [port]}
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
  }
}
