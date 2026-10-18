# Love Letter – Vorprojekt (SEP, LMU, WiSe 2026/27)

Chatsystem über TCP: beliebig viele Clients melden sich mit einem eindeutigen Namen beim Server an
und chatten miteinander. Dazu das Datenmodell für Love Letter (2–4 Spieler).

## Voraussetzungen

- JDK 27 (`java -version` muss 27 zeigen; sonst `JAVA_HOME` auf ein JDK 27 setzen)
- Maven 3.9 oder neuer

## Build

```powershell
mvn package
```

Baut und testet das Projekt. Ergebnis: `target/loveletter-jar-with-dependencies.jar` (enthält Gson).

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

Namensregeln: Leerzeichen am Rand werden entfernt; der Name darf nicht leer sein, keine Leerzeichen
enthalten und nicht mit `/` beginnen. Groß-/Kleinschreibung zählt (`anna` und `Anna` sind verschieden).

## Struktur

```
src/main/java/de/lmu/sep/loveletter/
  App.java             Einstieg: startet Server oder Client
  Server.java          nimmt Verbindungen an, verwaltet angemeldete Clients, Broadcast
  ClientHandler.java   ein Thread pro Client auf dem Server: Login, Chat, Aufräumen
  Client.java          Konsolen-Client: Login, Lese-Thread, Eingabe senden
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
