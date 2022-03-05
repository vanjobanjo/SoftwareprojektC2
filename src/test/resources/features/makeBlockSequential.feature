# language: de
Funktionalität: Als Planende*r moechte ich Bloecke sequentiell machen koennen

  Szenario: Ein paralleler Block ist nach dem Aufruf sequentiell
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante parallele Block "Bloeckchen" am 03.02.2022 um 15:00 Uhr mit den Pruefungen
      | Pruefung |
      | Analysis |
      | DM       |
    Wenn ich den Block "Bloeckchen" auf sequentiell stelle
    Dann ist der Block "Bloeckchen" sequentiell

  Szenario: Ein paralleler Block kann nicht auf sequentiell geaendert werden, weil harte Kriterien verletzt werden
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante parallele Block "Bloeckchen" am 03.02.2022 um 15:00 Uhr mit den Pruefungen
      | Pruefung | Studiengang | Semester |
      | Analysis | Inf         | 1        |
      | DM       | Inf         | 1        |
      | CG       | Inf         | 1        |
    Und es existiert die geplante Pruefung "pruefung" am 03.02.2022 um 16:30 Uhr mit dem Teilnehmerkreis "Inf" und dem Semester 1
    Wenn ich den Block "Bloeckchen" auf sequentiell stelle
    Dann bekomme ich eine Fehlermeldung HartesKriteriumException

  Szenario: Beeinflusste Pruefungen werden zurueck geliefert
    Angenommen es existiert eine Pruefungsperiode
    Und es existieren die folgenden Klausuren:
      | Name         | Datum      | StartZeit | Dauer |
      | Analysis     | 03.02.2022 | 15:00     | 01:00 |
      | DM           | 03.02.2022 | 15:00     | 01:00 |
      | CG           | 03.02.2022 | 15:00     | 01:00 |
      | RechnerNetze | 03.02.2022 | 15:00     | 01:00 |
      | BeWoop       | 03.02.2022 | 15:00     | 01:00 |
      | Other        | 03.02.2022 | 15:00     | 01:00 |
      | Later        | 03.02.2022 | 19:00     | 01:00 |
    Und es existiert der geplante parallele Block "Bloeckchen" am 03.02.2022 um 13:00 Uhr mit den Pruefungen
      | Pruefung |
      | Analysis |
      | IT       |
    Wenn ich den Block "Bloeckchen" auf sequentiell stelle
    Dann enthaelt das Ergebnis als einzige Pruefungen "Analysis", "DM", "CG", "RechnerNetze", "BeWoop", "Other", "Analysis", "IT"

  Szenario: Beeinflusste Bloecke werden mit ihren Pruefungen zurueckgeliefert
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante sequentielle Block "Blockomotive" am 03.02.2022 um 15:00 Uhr mit den Pruefungen
      | Pruefung |
      | CG       |
    Und es existieren die folgenden Klausuren:
      | Name         | Datum      | StartZeit | Dauer |
      | Analysis     | 03.02.2022 | 15:00     | 01:00 |
      | DM           | 03.02.2022 | 15:00     | 01:00 |
      | RechnerNetze | 03.02.2022 | 15:00     | 01:00 |
      | BeWoop       | 03.02.2022 | 15:00     | 01:00 |
      | Other        | 03.02.2022 | 15:00     | 01:00 |
    Und es existiert der geplante parallele Block "Bloeckchen" am 03.02.2022 um 13:00 Uhr mit den Pruefungen
      | Pruefung |
      | Analysis |
      | IT       |
    Wenn ich den Block "Bloeckchen" auf sequentiell stelle
    Dann enthaelt das Ergebnis als einzige Pruefungen "Analysis", "DM", "CG", "RechnerNetze", "BeWoop", "Other", "Analysis", "IT"
    Dann enthaelt das Ergebnis als einzige Bloecke "Blockomotive", "Bloeckchen"

  Szenario: Ein unbekannter Block kann nicht umgestellt werden
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich einen unbekannten Block auf sequentiell stelle
    Dann erhalte ich einen Fehler
