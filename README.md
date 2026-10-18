# Love Letter – Vorprojekt (SEP, LMU, WiSe 2026/27)

Chatsystem über TCP: beliebig viele Clients melden sich mit einem eindeutigen Namen beim Server an
und chatten miteinander. Dazu das Datenmodell für Love Letter (2–4 Spieler).

## Voraussetzungen

- JDK 27 (`java -version` muss 27 zeigen; sonst `JAVA_HOME` auf ein JDK 27 setzen)
- Maven 3.9 oder neuer
- JavaFX 27 muss nicht installiert werden: Maven lädt es als Dependency `org.openjfx:javafx-controls`
  (der Maven-Weg aus den openjfx-docs), deshalb ist kein JavaFX-SDK und kein SDK-Pfad nötig.

## Build

```powershell
mvn package
```

Baut und testet das Projekt. Ergebnis: `target/loveletter-jar-with-dependencies.jar` (enthält Gson
und JavaFX für das Betriebssystem, auf dem gebaut wurde).

## Start

Server (Standard-Port 1337):

```powershell
java -jar target/loveletter-jar-with-dependencies.jar server
java -jar target/loveletter-jar-with-dependencies.jar server 4000
```

Client (Standard: `localhost`, Port 1337), in einem eigenen Terminal pro Client:

```powershell
java -jar target/loveletter-jar-with-dependencies.jar client
java -jar target/loveletter-jar-with-dependencies.jar client localhost 4000
```

Der Client fragt nach einem Namen. Danach wird jede eingegebene Zeile an alle gesendet.
`bye` oder `/bye` beendet die Verbindung.

Chatfenster (JavaFX, Standard: `localhost`, Port 1337):

```powershell
java -jar target/loveletter-jar-with-dependencies.jar gui
java -jar target/loveletter-jar-with-dependencies.jar gui localhost 4000
```

Oben Namen eingeben und *Join* klicken; danach unten Text eingeben und *Send* klicken oder Enter
drücken. Das Fenster nutzt denselben Server und dasselbe Protokoll wie der Konsolen-Client, beide
können also miteinander chatten. Fenster schließen beendet die Verbindung.

Namensregeln: Leerzeichen am Rand werden entfernt; der Name darf nicht leer sein, keine Leerzeichen
enthalten und nicht mit `/` beginnen. Groß-/Kleinschreibung zählt (`anna` und `Anna` sind verschieden).

## VS Code

Benötigt die Extension **Extension Pack for Java** (`vscjava.vscode-java-pack`); VS Code schlägt sie
beim Öffnen des Ordners vor. Die Dateien in `.vscode/` enthalten rechnerspezifische JDK-Pfade und
sind deshalb nicht im Git (`.gitignore`):

| Datei             | Zweck                                                                                    |
|-------------------|------------------------------------------------------------------------------------------|
| `settings.json`   | Java-Extension nutzt JDK 27; jedes neue Terminal bekommt `JAVA_HOME` und Java 27 im `PATH` |
| `extensions.json` | empfiehlt das Extension Pack for Java                                                    |
| `tasks.json`      | Tasks zum Bauen, Testen und Starten (Ctrl+Shift+P → *Tasks: Run Task*)                    |
| `launch.json`     | Debug-Konfigurationen mit Breakpoints (Ctrl+Shift+D → Konfiguration wählen → F5)          |

Tasks:

| Task                       | Was er tut                                                       |
|----------------------------|------------------------------------------------------------------|
| `Build jar`                | `mvn package` ohne Tests                                         |
| `Test`                     | `mvn test`                                                       |
| `Server`                   | baut die Jar und startet den Server                              |
| `Client 1`, `Client 2`     | je ein Konsolen-Client                                           |
| `Play: Server + 2 Clients` | Server, und sobald er lauscht, zwei Konsolen-Clients nebeneinander |
| `GUI Client`, `GUI Client 2` | je ein Chatfenster                                             |
| `Play: Server + 2 GUIs`    | Server, und sobald er lauscht, zwei Chatfenster                  |

Debug-Konfigurationen: `server`, `client` und `gui` starten `App` mit dem jeweiligen Modus im
integrierten Terminal (dort nimmt der Konsolen-Client Eingaben an).

## Struktur

```
src/main/java/de/lmu/sep/loveletter/
  App.java             Einstieg: startet Server, Konsolen-Client oder Chatfenster
  Server.java          nimmt Verbindungen an, verwaltet angemeldete Clients, Broadcast
  ClientHandler.java   ein Thread pro Client auf dem Server: Login, Chat, Aufräumen
  Client.java          Konsolen-Client: Login, Lese-Thread, Eingabe senden
  ChatWindow.java      JavaFX-Chatfenster: Join, Send, Lese-Thread gibt Nachrichten per runLater aus
  Message.java         eine Protokollnachricht, als JSON-Zeile (Gson)
src/main/java/de/lmu/sep/loveletter/model/
  CardType.java        die 8 Kartenarten mit Wert und Anzahl im Deck
  Deck.java            Nachziehstapel mit 16 Karten
  Player.java          Hand, Ablagestapel, Tokens, Status in der Runde
  Round.java           Deck, beiseitegelegte Karten, Spieler, wer am Zug ist
  Game.java            Spieler und aktuelle Runde
src/test/java/...      ServerTest, MessageTest, ChatIntegrationTest, model/DeckTest
uml/classes.puml       UML-Klassendiagramm (PlantUML)
```

Das Modell enthält nur Daten, noch keine Spielregeln und keinen Netzwerk-Code.

## Protokoll

TCP, UTF-8, eine JSON-Nachricht pro Zeile. Jede Nachricht hat die Felder `type`, `name`, `text`;
nicht benötigte Felder fehlen.

| Richtung        | type          | Felder     | Bedeutung                                    |
|-----------------|---------------|------------|----------------------------------------------|
| Client → Server | `LOGIN`       | name       | Anmeldung mit Namen                          |
| Client → Server | `INPUT`       | text       | eine eingegebene Zeile                       |
| Server → Client | `LOGIN_OK`    | –          | Name akzeptiert                              |
| Server → Client | `LOGIN_ERROR` | text       | Name abgelehnt, mit Grund                    |
| Server → Client | `INFO`        | text       | `welcome name`, `name joined/left the room`  |
| Server → Client | `CHAT`        | name, text | Chatzeile von `name`, an alle inkl. Absender |
| Server → Client | `ERROR`       | text       | unbekannter Command, nur an den Absender     |

Beispiel:

```
→ {"type":"LOGIN","name":"anna"}
← {"type":"LOGIN_OK"}
← {"type":"INFO","text":"welcome anna"}
→ {"type":"INPUT","text":"hallo"}
← {"type":"CHAT","name":"anna","text":"hallo"}
```

Ablauf: Nach `LOGIN_OK` bekommt der neue Client `welcome name`, alle anderen `name joined the room`.
Eingaben ohne `/` gehen als `CHAT` an alle. `bye` und `/bye` beenden die Verbindung; jeder andere
Text mit `/` am Anfang ergibt `ERROR`. Wird eine Verbindung beendet (per `bye` oder durch Abbruch),
bekommen alle anderen `name left the room` und der Name ist wieder frei.
