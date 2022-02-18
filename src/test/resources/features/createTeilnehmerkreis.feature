# language: de
Funktionalität: Als Planender moechte ich einen neuen Teilnehmerkrs erstellen können.

# funktioniert
# leere Argumente
# Semester <= 0

  Szenariogrundriss: Ein Teilnehmerkreis wird korrekt erstellt.
    Wenn ich einen Bachelor Teilnehmerkreis erstelle mit <Studiengang>, <Studienordnung> und <Semester> erstelle
    Dann erhalte ich einen Bachelor Teilnehmerkreis mit <Studiengang>, <Studienordnung> und <Semester>

    Beispiele:
      | Studiengang | Studienordnung | Semester |
      | "Inf"       | "14.0"         | 1        |

  Szenario: Ein Teilnehmerkreis kann nicht ohne Studiengang erstellt werden
    Wenn ich einen Teilnehmerkreis ohne Studiengang erstelle
    Dann erhalte ich einen Fehler

  Szenario: Ein Teilnehmerkreis kann nicht ohne Studienordnung erstellt werden
    Wenn ich einen Teilnehmerkreis ohne Studienordnung erstelle
    Dann erhalte ich einen Fehler

  Szenario: Ein Teilnehmerkreis kann nicht ohne Semester erstellt werden
    Wenn ich einen Teilnehmerkreis ohne Semester erstelle
    Dann erhalte ich einen Fehler
