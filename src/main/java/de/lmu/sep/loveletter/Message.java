package de.lmu.sep.loveletter;

import com.google.gson.Gson;

/**
 * One message of the chat protocol. Every message is sent as one line of JSON.
 *
 * <p>Client to server: {@link #LOGIN} (name), {@link #INPUT} (text).
 * Server to client: {@link #LOGIN_OK}, {@link #LOGIN_ERROR} (text), {@link #INFO} (text),
 * {@link #CHAT} (name, text), {@link #ERROR} (text). Fields that a type does not use are null.
 */
public class Message {

  public static final String LOGIN = "LOGIN";
  public static final String INPUT = "INPUT";
  public static final String LOGIN_OK = "LOGIN_OK";
  public static final String LOGIN_ERROR = "LOGIN_ERROR";
  public static final String INFO = "INFO";
  public static final String CHAT = "CHAT";
  public static final String ERROR = "ERROR";

  private static final Gson GSON = new Gson();

  // Not final: Gson fills the fields when reading JSON, and Java warns when a library
  // changes final fields.
  private String type;
  private String name;
  private String text;

  /**
   * Creates a message.
   *
   * @param type one of the type constants of this class
   * @param name the user name, or null if the type does not need one
   * @param text the text, or null if the type does not need one
   */
  public Message(String type, String name, String text) {
    this.type = type;
    this.name = name;
    this.text = text;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getText() {
    return text;
  }

  /**
   * Converts this message to a single line of JSON.
   *
   * @return the JSON text, without line break
   */
  public String toJson() {
    return GSON.toJson(this);
  }

  /**
   * Reads a message from one line of JSON.
   *
   * @param json the JSON text produced by {@link #toJson()}
   * @return the message
   */
  public static Message fromJson(String json) {
    return GSON.fromJson(json, Message.class);
  }
}
