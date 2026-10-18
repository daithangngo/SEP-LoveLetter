package de.lmu.sep.loveletter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Tests that messages survive the way to JSON and back. */
class MessageTest {

  @Test
  void allFieldsSurviveJsonRoundTrip() {
    Message original = new Message(Message.CHAT, "anna", "hello \"world\"");

    Message copy = Message.fromJson(original.toJson());

    assertEquals(Message.CHAT, copy.getType());
    assertEquals("anna", copy.getName());
    assertEquals("hello \"world\"", copy.getText());
  }

  @Test
  void missingFieldsStayNull() {
    Message copy = Message.fromJson(new Message(Message.LOGIN_OK, null, null).toJson());

    assertEquals(Message.LOGIN_OK, copy.getType());
    assertNull(copy.getName());
    assertNull(copy.getText());
  }

  @Test
  void jsonIsOneLine() {
    String json = new Message(Message.INPUT, null, "first\nsecond").toJson();

    assertFalse(json.contains("\n"));
    assertEquals("first\nsecond", Message.fromJson(json).getText());
  }
}
