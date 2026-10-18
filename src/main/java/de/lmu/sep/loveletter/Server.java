package de.lmu.sep.loveletter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat server. Accepts connections and starts one {@link ClientHandler} thread per client.
 * Keeps the logged-in clients by name.
 */
public class Server {

  private final int port;
  // Many ClientHandler threads read and change this map at the same time.
  private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

  /**
   * Creates a server. Does not open a socket yet, see {@link #start()}.
   *
   * @param port the TCP port to listen on
   */
  public Server(int port) {
    this.port = port;
  }

  /**
   * Opens the server socket and accepts clients until the program ends.
   *
   * @throws IOException if the port cannot be opened
   */
  public void start() throws IOException {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Server listening on port " + port);
      while (true) {
        Socket socket = serverSocket.accept();
        ClientHandler handler = new ClientHandler(socket, this);
        new Thread(handler).start();
      }
    }
  }

  /**
   * Registers a client under a name if the name is free.
   *
   * @param name the requested name
   * @param handler the handler of the client
   * @return true if the name was free and is now taken by this client
   */
  public boolean register(String name, ClientHandler handler) {
    // putIfAbsent checks and inserts in one atomic step, so two clients
    // can never get the same name at the same time.
    return clients.putIfAbsent(name, handler) == null;
  }

  /**
   * Removes a client, so its name is free again.
   *
   * @param name the name of the client
   */
  public void remove(String name) {
    clients.remove(name);
  }
}
