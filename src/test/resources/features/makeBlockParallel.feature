# language: de
Funktionalität: Als Planende*r moechte ich sequentielle Bloecke parallel machen koennen

  Szenario: Ein umgestellter Block ist hinterher vom Typ parallel
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante sequentielle Block "Bloeckchen" am 03.02.2022 um 15:00 Uhr mit den Pruefungen
      | Pruefung |
      | Analysis |
      | DM       |
    Wenn ich den Block "Bloeckchen" auf parallel stelle
    Dann ist der Block "Bloeckchen" parallel

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
    Und es existiert der geplante sequentielle Block "Bloeckchen" am 03.02.2022 um 13:00 Uhr mit den Pruefungen
      | Pruefung |
      | Analysis |
      | IT       |
    Wenn ich den Block "Bloeckchen" auf parallel stelle
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
    Und es existiert der geplante sequentielle Block "Bloeckchen" am 03.02.2022 um 13:00 Uhr mit den Pruefungen
      | Pruefung |
      | Analysis |
      | IT       |
    Wenn ich den Block "Bloeckchen" auf parallel stelle
    Dann enthaelt das Ergebnis als einzige Pruefungen "Analysis", "DM", "CG", "RechnerNetze", "BeWoop", "Other", "Analysis", "IT"
    Dann enthaelt das Ergebnis als einzige Bloecke "Blockomotive", "Bloeckchen"

  Szenario: Ein unbekannter Block kann nicht umgestellt werden
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich einen unbekannten Block auf parallel stelle
    Dann erhalte ich einen Fehler
