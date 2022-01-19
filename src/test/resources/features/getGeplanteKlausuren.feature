# language: de
Funktionalität: Als Planender möchte ich erfahren können welche Klausuren eingeplant sind.

  Szenario: Es gibt keine Klausuren
    Angenommen es existiert eine Pruefungsperiode
    Angenommen es existieren keine Klausuren
    Wenn ich alle geplanten Klausuren anfrage
    Dann bekomme ich keine Klausuren

  Szenario: Es gibt nur ungeplante Klausuren
    Angenommen es existiert eine Pruefungsperiode
    Angenommen es existieren die folgenden Klausuren:
      | Name           | Datum | StartZeit | Dauer |
      | Analysis       |       |           | 03:00 |
      | Diskrete Mathe |       |           | 01:30 |
    Wenn ich alle geplanten Klausuren anfrage
    Dann bekomme ich keine Klausuren

  Szenario: Es gibt nur geplante Klausuren
    Angenommen es existiert eine Pruefungsperiode
    Angenommen es existieren die folgenden Klausuren:
      | Name           | Datum      | StartZeit | Dauer |
      | Analysis       | 01.02.2022 | 08:00     | 03:00 |
      | Diskrete Mathe | 02.02.2022 | 08:30     | 01:30 |
    Wenn ich alle geplanten Klausuren anfrage
    Dann bekomme ich die Klausuren "Analysis, Diskrete Mathe"

  Szenario: Es gibt geplante und ungeplante Klausuren
    Angenommen es existiert eine Pruefungsperiode
    Angenommen es existieren die folgenden Klausuren:
      | Name         | Datum      | StartZeit | Dauer |
      | AuD          | 15.02.2022 | 08:00     | 03:00 |
      | Datascience  |            |           | 03:00 |
      | KI           |            |           | 01:30 |
      | Rechnernetze | 12.02.2022 | 08:30     | 01:30 |
    Wenn ich alle geplanten Klausuren anfrage
    Dann bekomme ich die Klausuren "AuD, Rechnernetze"

  Szenario: Es gibt geplante Pruefungen in Blöcken
    Angenommen es existiert eine Pruefungsperiode
    Angenommen es existieren die folgenden Klausuren:
      | Name         | Datum | StartZeit | Dauer |
      | AuD          |       |           | 03:00 |
      | Datascience  |       |           | 03:00 |
      | KI           |       |           | 01:30 |
      | Rechnernetze |       |           | 01:30 |
    Angenommen es existieren die folgenden Bloecke:
      | Block | Datum      | StartZeit | Pruefungen       |
      | 1     | 12.02.2022 | 08:30     | KI, Rechnernetze |
    Wenn ich alle geplanten Klausuren anfrage
    Dann bekomme ich die Klausuren "KI, Rechnernetze"
