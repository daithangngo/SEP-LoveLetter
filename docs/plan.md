# Plan: Milestone 2 (release v2.0)

**Deadline:** Sun 01.11.2026, 23:59 · **Target release:** Sat 31.10.2026 (one day buffer)
**Written:** Fri 23.10.2026 · Tick boxes as you go and update the status line of each feature.

This plan says **what** to build and **in which order**. It does not repeat the requirements or the
rules; those live in the project notes (local only, not in git):

| What | Where |
|------|-------|
| Full sheet texts + MS2 checklist (IDs like `2.a.iii`) | [projekt/02-meilensteine.md](projekt/02-meilensteine.md) |
| Base rules, round flow, tutor diagram | [projekt/03-spielregeln.md](projekt/03-spielregeln.md) |
| Custom cards (sheet 2a) + analysis | [projekt/04-zusatzkarten.md](projekt/04-zusatzkarten.md) |
| Decisions (`E-xxx`) | [projekt/07-entscheidungen.md](projekt/07-entscheidungen.md) |
| Open questions (`F-xx`) | [projekt/08-offene-fragen.md](projekt/08-offene-fragen.md) |

---

## 1. Where we stand (23.10.2026)

| Area | State |
|------|-------|
| MS1 chat (login, welcome/joined/left, broadcast, `bye`, broken connection) | ✅ done, tested (`ChatIntegrationTest`) |
| JavaFX chat window (`gui` mode) | ✅ done |
| Data model (`CardType`, `Deck`, `Player`, `Round`, `Game`) + UML | ✅ data only, no rules |
| Deck with 4 custom cards, `shuffle()`, `draw()`, seeded `Random` | ✅ on `feature/card-model`, **not merged** into `develop` |
| Doppelganger (per-card random value) | ⬜ postponed by E-006 |
| Commands (`/msg`, `/create`, `/join`, `/start`, `/score`) | ⬜ only `/bye` exists |
| Game rules (turns, effects, round end, game end) | ⬜ |
| Game protocol (events, private hand, play with targets) | ⬜ |
| Error handling (2.c) | 🟡 unknown commands only; **malformed JSON or a missing `text` crashes the handler thread** (`ClientHandler.chat()` → NPE / `JsonSyntaxException`, not caught) |
| Javadoc HTML in the repo | ⬜ |

Branches: `master` = `develop` = `4edb1c8` (MS1). `feature/card-model` is 5 commits ahead.

---

## 2. Definition of Done (every feature)

A feature is done only when **all** of these are true:

- [ ] `mvn clean package` is green (all tests pass, jar builds).
- [ ] New behaviour has JUnit tests. Rule logic is tested **without** network, using a deck in a
      fixed order (stacked deck, see F1.1).
- [ ] Every new public class/method has Javadoc (`@param`, `@return`, `@throws`, `@author`).
- [ ] Every rule interpretation made along the way is logged as `E-xxx` in 07.
- [ ] The README (commands, protocol table, structure) and `uml/classes.puml` are updated if the
      feature changed them.
- [ ] The matching rows in the 02 checklist are ticked.
- [ ] The commits are small, in Conventional Commits style, with no Claude trailer. The feature branch
      is merged into `develop`.
- [ ] **I can explain every line** (Abnahme, plagiarism rule).

Branch per feature: `feature/<short-name>` from `develop`. Release: `develop` → `master`, tag
`v2.0`.

---

## 3. Timeline

| Day | Date | Phase / features | Checkpoint |
|-----|------|------------------|------------|
| Fri | 23.10 | **P0** decisions, merge card model · F1.1 `Card` class | |
| Sat | 24.10 | F1.2 command handler + `/msg` · F1.3 lobby | |
| Sun | 25.10 | F1.4 round setup + turn loop | |
| Mon | 26.10 | F1.5 original card effects | |
| Tue | 27.10 | F1.6 round + game end · F1.7 robustness | **M-A: base game playable** |
| Wed | 28.10 | F2.1 Black Mark · F2.2 Phantom | |
| Thu | 29.10 | F2.3 Occultist · F2.4 Mirror (start) | |
| Fri | 30.10 | F2.4 Mirror · F2.5 Doppelganger · F2.6 interaction tests | **M-B: feature complete** |
| Sat | 31.10 | **P3** Javadoc, README, UML, jar check, release | **M-C: v2.0 tagged** |
| Sun | 01.11 | buffer only, no new features | deadline 23:59 |

If you fall behind, cut from the **bottom of Phase 2 polish**, never from tests or Javadoc. Both are
explicit requirements.

---

## 4. Phase 0: Decisions and housekeeping (23.10)

Goal: no feature gets blocked by an undecided question.

### P0.1 Merge the card model
- [ ] Merge `feature/card-model` into `develop` (Merge Request on GitLab, so the tutors can see it).

### P0.2 Design decisions (log as E-007 …)

| # | Question | Recommendation | Why |
|---|----------|----------------|-----|
| D1 | Protocol: text commands or structured messages? (open in 07) | **Text commands.** The client keeps sending `INPUT`; the server parses `/play guard bob priest`. Add only new **server → client** types (see section 8). `Message` keeps its 3 fields. | "As simple as possible" (sheet 2). The console client and GUI stay unchanged. |
| D2 | How does the rule engine talk to the network? | The model calls an interface, e.g. `GameListener` with `broadcast(text)`, `tell(player, text)`, `ask(player, text)`. The server implements it; tests use a recording fake. | The model stays free of sockets and can be tested without a network (SRP, testability). |
| D3 | Thread safety | All game commands go through **one** object (e.g. `GameController`). Each of its methods is `synchronized`. | Handler threads call in parallel. One lock is the easiest thing to explain in the Abnahme. |
| D4 | Where do card effects live? | One class (e.g. `CardEffects`) with **one** `switch` that dispatches to one small method per card. | A switch in exactly one place. Mirror (redirect the target) and Doppelganger (run another card's effect) reuse the same dispatch. |
| D5 | Questions mid-turn (Mirror reaction, Doppelganger copy) | `Round` has a "pending decision" (who must answer, about what). While it is set, only that player's answer is accepted. | A small state machine instead of blocking a thread while waiting. |
| D6 | Player leaves mid-game | They are eliminated from the round and removed from the game. If fewer than 2 players remain, the game ends. | Never leaves the game stuck waiting for someone who is gone. |

### P0.3 Rule questions: answer from the course materials, ask the tutor only the rest

Order of authority: sheets (1, 2, 2a) → official rulebook (binding per sheet 1) → Merkblatt and
slides → tutor diagram. Implement each rule in **one** place, so that changing an answer costs one
method.

**Settled from the materials** (logged as E-007 … E-011 in 07):

| F-xx | Answer | Needed by |
|------|--------|-----------|
| F-12, F-13 jar / Javadoc | One fat jar; Javadoc HTML in `docs/javadoc/` | F3.1, F3.3 |
| F-14, F-15, F-18 turn order / start player | Join order, cyclic. First round: fewest days since last date → youngest → join order. Next round: the winner; several winners → fewest days, then as above. `/join <daysSinceLastDate> <age>` | F1.3, F1.4, F1.6 |
| F-17, F-19 Guard | Name a **number** other than 1 (0 allowed). It hits **every** card with that value, the Doppelganger by its random value | F1.5 |
| F-08 Phantom names | Any card name, "Guard" and "Phantom" included | F2.2 |
| F-09 Doppelganger | Protected players are not valid targets. Shown as e.g. `Doppelganger (5)` | F2.5 |
| F-02 Mirror reaction | Only a player holding a Mirror is asked | F2.4 |

**Ask the tutor** (meeting 25.10). Default until answered:

| F-xx | Needed by | Default until answered |
|------|-----------|------------------------|
| F-03, F-04 Mirror | F2.4 | A played Mirror lasts until it triggers or until its owner's next turn. "Reflect" = the actor becomes the target of their own effect |
| F-05, F-06 Occultist | F2.3 | Exclusions apply to revival **and** sum elimination. The revived player plays one of their 2 cards without drawing. If the deck has fewer than 2 cards, draw what is left plus the set-aside card |
| F-01 Doppelganger | F2.5 | Fewer days since the last date wins the comparison |
| F-07 Black Mark owner already out | F2.1 | Still wins (like Phantom: the effect outlives the player) |
| F-11 indentation (slide says 4, but links Google Style with 2) | P0.4 | Keep 2 spaces |

- [x] Settle what the materials answer and log it in 07 (E-007 … E-011).
- [ ] Ask the 5 remaining questions at the tutor meeting (25.10). None blocks Phase 1.

### P0.4 Reformat to 4 spaces, only if the tutor asks for it · `style/indentation`
- [ ] Reformat all Java files to 4-space indentation in **one** commit (`style: switch to 4-space indentation`), with no other changes. Do it before F1.4, where most new code lands.
- [ ] `mvn clean package` still green.

---

## 5. Phase 1: Base game playable over chat (24.10 – 27.10)

Covers: 2.2a–d, 2.2h, 2.a.i–v, 2.b.i–iii, 1.8, part of 2.c.

### F1.1 `Card` class and Doppelganger in the deck · `feature/card-class` · ✅
Do this **before** writing rules, so no rule code ever depends on bare `CardType` values.
- [x] `Card` = `CardType` + `value` (a `record`; fixed value from the type; the Doppelganger gets
      a value from 0 to 8 when the deck is created).
- [x] Add `DOPPELGANGER` to `CardType` → the deck has **32** cards.
- [x] `Deck(List<Card>)` constructor for tests (stacked deck: the exact draw order is known).
- [x] `Player.hand` / `discardPile`, `Round.setAsideCard` / `faceUpCards` use `Card`.
- [x] Tests: 32 cards, each count correct, Doppelganger value in 0..8 and fixed for a fixed seed
      (`CardTest`, `DeckTest`).
- [x] Revise E-006 in 07 (timing changed).
- Moved to F1.5: "playing a custom card = discard without effect" (there is no play code yet).

### F1.2 Command handler + direct message · `feature/commands` · ✅
- [x] Command parsing in **one** method `ClientHandler.handleCommand()` (a `switch`). Kept minimal:
      it moves into its own class in F1.3, once the game commands make it big enough.
- [x] `/msg <name> <text>`: only the receiver gets it (new type `PRIVATE`), the sender gets an
      `INFO` confirmation.
- [x] `/help` lists all commands with their arguments.
- [x] Errors: unknown name, message to yourself, missing text, unknown command → helpful `ERROR`.
- [x] **Fix the crash:** malformed JSON, `null` text, or a non-`INPUT` type after login → `ERROR`,
      and the connection stays open.
- [x] Integration tests in the style of `ChatIntegrationTest` (4 new: private message, its
      errors, `/help`, broken input).

### F1.3 Lobby: create, join, start, score · `feature/lobby` · ⬜
- [ ] Move `handleCommand()` into its own class (e.g. `CommandHandler`) before adding the game
      commands.
- [ ] `/create`: exactly one game per server (error: "a game already exists").
- [ ] `/join <days> <age>`: only before the start; max 4; not twice; argument validation.
- [ ] `/start`: only by a joined player; at least 2 players (error tells how many are missing).
- [ ] `/score`: tokens of every player, plus the goal (7 / 5 / 4 by player count).
- [ ] Chat keeps working for everyone, inside and outside the game (2.2c).
- [ ] Tests for every error case listed under "Fehlerfälle" in 02.

### F1.4 Round setup and turn loop · `feature/turns` · ⬜
- [ ] Setup: shuffle, set aside 1 card face down, with 2 players 3 face-up cards (broadcast them),
      deal 1 card each.
- [ ] Start player: fewest days since last date → youngest → join order (E-009).
- [ ] Turn start: draw → send the hand **privately** (`HAND`) → broadcast "it is X's turn" (`GAME`).
- [ ] `/play <card> [player] [number|cardName]` and `/hand` (re-send your own hand).
- [ ] Errors: not your turn, card not in hand, no game running, you are eliminated.
- [ ] Central `eliminate(player, reason)`: the **only** place that sets `inRound = false`.
      Phase 2 hooks into it.
- [ ] Handmaid protection ends at the start of your own turn.

### F1.5 Original card effects · `feature/original-cards` · ⬜
One commit + test class section per card; tests use a stacked deck.
- [ ] Guard: name a number 0–8 except 1; it hits any card with that value → eliminated (E-008).
- [ ] Priest: the target's hand is shown **only** to the actor.
- [ ] Baron: compare privately; lower value out; tie → nothing.
- [ ] Handmaid: protected until your next turn.
- [ ] Prince: anyone incl. yourself; discard without effect (Princess → out, rest of the effect
      skipped); empty deck → take the set-aside card; everyone else protected → must target yourself.
- [ ] King: swap hands with another player.
- [ ] Countess: **forced** if held with King or Prince (the server rejects the other card).
- [ ] Princess: discarding it for any reason → out.
- [ ] Shared target rules: other player required, protected, eliminated, nobody valid → discard
      without effect.
- [ ] Until a custom card's phase is done, playing it = discard **without effect** (temporary,
      marked with a short comment; removed in F2.6).

### F1.6 Round end and game end · `feature/round-end` · ⬜
- [ ] Deck empty at the end of a turn → reveal, highest value wins → tie: sum of discards → tie:
      all win.
- [ ] Only one player left → they win.
- [ ] 1 token per round winner; check the game goal after every token.
- [ ] Tie-break round: if several players reach the goal at the same time, only they play on.
- [ ] Next round: the winner starts (several winners → fewest days since last date, E-009); everything reshuffled.
- [ ] Game over → broadcast the winner; a new `/create` is possible.

### F1.7 Robustness · `feature/disconnect` · ⬜
- [ ] A player disconnects or sends `bye` mid-game → D6.
- [ ] Every remaining case from the "Fehlerfälle" list in 02 has a test or is ticked off by hand.

**✔ Checkpoint M-A (27.10):** three console clients play a complete game with the original rules.
Show it to the tutor if the meeting falls in this week.

---

## 6. Phase 2: Custom cards (28.10 – 30.10)

Covers: 2.2e, 2.2f, 2.2g. Ordered from the simplest hook to the most complex. Each card is its
own branch with its own test class (e.g. `BlackMarkTest`).

### F2.1 Black Mark · `feature/black-mark` · ⬜
- [ ] Public target: any unprotected player still in the round, yourself included.
- [ ] The mark lasts until the start of the owner's next turn (stored in `Round`).
- [ ] Hook in `eliminate()`: the marked player is out → the round ends **immediately**, the owner
      wins + 1 token.
- [ ] Tests: self-mark, mark expires, eliminated by Guard / Baron / Prince-Princess.

### F2.2 Phantom · `feature/phantom` · ⬜
- [ ] Target another unprotected player + any card name, Guard and Phantom included (E-010).
- [ ] The curse is stored on the target. It is checked at their next turn **before drawing**, then
      removed.
- [ ] The curse persists if the Phantom player is eliminated. Several curses → check all.
- [ ] The card is compared at the moment it is revealed (after a King/Prince swap). Empty hand → no hit.

### F2.3 Occultist · `feature/occultist` · ⬜
- [ ] When played → lies face up on the table, separate from the discard pile (`Round`).
- [ ] Hook in `eliminate()`, **before** the Black Mark check: revive, the hand is lost, draw 2,
      eliminate everyone else with a higher value (exclusions per F-05), the revived player is next.
- [ ] Only after a revival → the Occultist goes onto the discard pile. Several on the table → the
      oldest one triggers.
- [ ] Tests: deck with fewer than 2 cards, interplay with Black Mark, turn order afterwards.

### F2.4 Mirror · `feature/mirror` · ⬜
Introduces the pending-decision infrastructure (D5), which the Doppelganger reuses.
- [ ] Played normally → reflects the next effect aimed at its owner, once.
- [ ] Reaction: a targeted player who holds a Mirror is asked (`PROMPT`) and answers `/yes` or `/no`.
- [ ] Reflection = the actor becomes the target (F-04). No ping-pong. The Doppelganger is not
      reflectable.
- [ ] Empty hand after a reaction → draws 2 at their next turn. An empty hand counts as 0 in every
      comparison.
- [ ] Tests: each original card reflected; reaction declined; timeout-free flow.

### F2.5 Doppelganger · `feature/doppelganger` · ⬜
- [ ] Compare "days since last date" with another player (data from `/join`).
- [ ] Loss **or tie** → the actor is out.
- [ ] Win → see the target's card privately → `PROMPT` copy yes/no → yes: run that card's effect
      through `CardEffects` (asking for new targets, e.g. a Guard name).
- [ ] Copying the Princess / Countess: decide and log the outcome (E-xxx).

### F2.6 Interaction tests · `feature/interaction-tests` · ⬜
- [ ] One test per non-empty cell of the interaction table in 04 (Handmaid × each card, Mirror ×
      each card, Occultist × Black Mark …).
- [ ] One full scripted game with a stacked deck, from `/create` to the game winner.
- [ ] Remove the temporary "custom card without effect" code from F1.1.

**✔ Checkpoint M-B (30.10):** all 13 card types work; `mvn clean package` is green.

---

## 7. Phase 3: Documentation and release (31.10)

Covers: 2.1a–d, 2.G1–G3, 1.19.

### F3.1 Javadoc · `docs/javadoc` · ⬜
- [ ] Every public and protected element documented; the `mvn javadoc:javadoc` output has **no warnings**.
- [ ] Configure `maven-javadoc-plugin` (standard plugin, allowed) to write to a tracked folder,
      `docs/javadoc/` (E-007), including private members if wanted.
- [ ] Commit the generated HTML (`docs: generate javadoc html`).

### F3.2 README and UML · ⬜
- [ ] README: a "How to play" section, a command table, the new protocol message types, the updated
      structure; remove "The model contains only data, no game rules yet".
- [ ] `uml/classes.puml` with the new classes → re-render `.png` / `.svg`.

### F3.3 Release check · ⬜
- [ ] Indentation is consistent everywhere, as settled by F-11 (P0.4 done if the tutor asked for 4).
- [ ] Jar on other machines (E-007 / E-004): either build on the OS used for the Abnahme, or add the JavaFX
      platform jars for Windows, macOS and Linux to the fat jar. Ask before changing the pom.
- [ ] Fresh clone → `mvn clean package` → server + 3 clients → play one full game.
- [ ] Update 05-ist-stand.md and the "already in the code" table in 03 (both are stale now).

### F3.4 Release v2.0 · ⬜
- [ ] Merge `develop` → `master` (Merge Request).
- [ ] `git tag -a v2.0 -m "Milestone 2: Love Letter logic"` and push the tag.
- [ ] Check on gitlab2 that `master`, the tag and `docs/javadoc/` are visible to the tutors.

**✔ Checkpoint M-C (31.10):** v2.0 tagged and pushed. 01.11 is only for fixes.

---

## 8. Proposed command set and message types (decide in D1)

Client → server: still only `LOGIN` and `INPUT`. Commands are text:

| Command | Effect |
|---------|--------|
| `/help` | list all commands |
| `/msg <name> <text>` | direct message |
| `/create` | create the one game of this server |
| `/join <daysSinceLastDate> <age>` | join before the start |
| `/start` | start with 2–4 joined players |
| `/play <card> [player] [cardName]` | play a card; extra arguments only if the card needs them |
| `/hand` | show your own hand again |
| `/yes`, `/no` | answer a prompt (Mirror reaction, Doppelganger copy) |
| `/score` | tokens of all players and the goal |
| `/bye` | leave |

Server → client: the existing `LOGIN_OK`, `LOGIN_ERROR`, `INFO`, `CHAT`, `ERROR`, plus:

| type | Fields | To | Meaning |
|------|--------|----|---------|
| `WHISPER` | name, text | receiver + sender | direct message from `name` |
| `GAME` | text | all | public game event (turn, played card, elimination, round / game end) |
| `HAND` | text | one player | your current cards (secret) |
| `PROMPT` | text | one player | the server waits for `/yes` or `/no` |

No new fields in `Message`, so the console client and the GUI only need a case in `Client.format`.

---

## 9. Risks

| Risk | Counter-measure |
|------|-----------------|
| Tutor answers arrive late or contradict the defaults | Each rule in one method; the defaults are logged, so a change is a small fix + test update |
| Mirror / Doppelganger prompts get complicated | D5 decided up front; Mirror is built before the Doppelganger; worst case, Mirror as a reaction is limited to the obvious cases and documented |
| Time | M-A is the critical checkpoint. If it slips past 28.10, start Phase 2 anyway and do F1.7 in parallel |
| The jar fails on the examiner's machine | F3.3 fresh-clone test; decide the JavaFX platform jars early |
| Can't explain your own code in the Abnahme | Small commits; read every diff before merging; a short "how it works" walk-through rehearsal after M-B |

---

## 10. After v2.0

- Optional sheet 3: add it to 02 when it is published, then extend this plan.
- Abnahme: prepare a 5-minute demo script (3 clients, one full round with custom cards) and be ready
  to explain the decision log (07).
