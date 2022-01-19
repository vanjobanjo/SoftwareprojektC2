# language: de
Funktionalität: Als Planender möchte ich erfahren können welche Klausuren nicht eingeplant sind.

  Szenario: Es gibt keine Klausuren
    Angenommen es existieren keine Klausuren
    Wenn ich alle ungeplanten Klausuren anfrage
    Dann bekomme ich keine Klausuren

  Szenario: Es gibt nur ungeplante Klausuren
    Angenommen es existiert eine Pruefungsperiode
    Angenommen es existieren die folgenden Klausuren:
      | Name           | Datum | StartZeit | Dauer |
      | Analysis       |       |           | 03:00 |
      | Diskrete Mathe |       |           | 01:30 |
    Wenn ich alle ungeplanten Klausuren anfrage
    Dann bekomme ich die Klausuren "Analysis, Diskrete Mathe"

  Szenario: Es gibt nur geplante Klausuren
    Angenommen es existiert eine Pruefungsperiode
    Angenommen es existieren die folgenden Klausuren:
      | Name           | Datum      | StartZeit | Dauer |
      | Analysis       | 25.02.2022 | 08:00     | 03:00 |
      | Diskrete Mathe | 02.02.2022 | 08:30     | 01:30 |
    Wenn ich alle ungeplanten Klausuren anfrage
    Dann bekomme ich keine Klausuren

  Szenario: Es gibt geplante und ungeplante Klausuren
    Angenommen es existiert eine Pruefungsperiode
    Angenommen es existieren die folgenden Klausuren:
      | Name           | Datum      | StartZeit | Dauer |
      | Analysis       | 15.02.2022 | 08:00     | 03:00 |
      | Datascience    |            |           | 03:00 |
      | KI             |            |           | 01:30 |
      | Diskrete Mathe | 02.02.2022 | 08:30     | 01:30 |
    Wenn ich alle ungeplanten Klausuren anfrage
    Dann bekomme ich die Klausuren "Datascience, KI"

  Szenario: Es gibt ungeplante Pruefungen in Bloecken
    Angenommen es existiert eine Pruefungsperiode
    Angenommen es existieren die folgenden Klausuren:
      | Name         | Datum | StartZeit | Dauer |
      | AuD          |       |           | 03:00 |
      | Datascience  |       |           | 03:00 |
      | KI           |       |           | 01:30 |
      | Rechnernetze |       |           | 01:30 |
    Angenommen es existieren die folgenden Bloecke:
      | Block | Datum | StartZeit | Pruefungen       |
      | 1     |       |           | KI, Rechnernetze |
    Wenn ich alle ungeplanten Klausuren anfrage
    Dann bekomme ich die Klausuren "AuD, KI, Rechnernetze, Datascience"
