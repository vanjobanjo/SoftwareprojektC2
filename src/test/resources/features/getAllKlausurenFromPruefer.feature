# language: de

Funktionalität: Als Planende*r moechte ich alle Klausuren eines Pruefers abfragen koennen

  Szenario: Ich frage die Pruefungen eines Pruefers ab, aber es gibt keine Pruefungen
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich alle Pruefungen des Pruefers "Harms" abfrage
    Dann bekomme ich keine Pruefungen

  Szenario: Ich frage die Pruefungen eines Pruefers aber der Pruefer hat keine Pruefungen
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.04.2022
    Angenommen es existieren die folgenden Klausuren mit Pruefer:
      | Name        | Datum      | StartZeit | Dauer | Pruefer     |
      | AuD         | 10.01.2022 | 08:00     | 03:00 | test, test2 |
      | DM          | 11.02.2022 | 08:00     | 03:00 | test        |
      | Datascience |            |           | 03:00 | test        |
    Wenn ich alle Pruefungen des Pruefers "Harms" abfrage
    Dann bekomme ich keine Pruefungen

  Szenario: Ich frage die Pruefungen eines Pruefers mit Pruefungen, es gibt pro Pruefung nur einen Pruefer
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.04.2022
    Angenommen es existieren die folgenden Klausuren mit Pruefer:
      | Name        | Datum      | StartZeit | Dauer | Pruefer |
      | AuD         | 10.01.2022 | 08:00     | 03:00 | Saering |
      | DM          | 11.02.2022 | 08:00     | 03:00 | Saering |
      | Datascience |            |           | 03:00 | Harms   |
    Wenn ich alle Pruefungen des Pruefers "Saering" abfrage
    Dann bekomme ich die Pruefungen "AuD, DM"

  Szenario: Ich frage die Pruefungen eines Pruefers mit Pruefungen, Pruefungen sind in einem Block
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.04.2022
    Angenommen es existieren die folgenden Klausuren mit Pruefer:
      | Name        | Datum      | StartZeit | Dauer | Pruefer |
      | AuD         | 10.01.2022 | 08:00     | 03:00 | Saering |
      | DM          | 11.02.2022 | 08:00     | 03:00 | Saering |
      | Datascience |            |           | 03:00 | Harms   |
    Angenommen es existieren die folgenden Bloecke:
      | Block   | Datum      | StartZeit | Pruefungen  |
      | block 1 | 11.02.2022 | 08:30     | Datascience |
    Wenn ich alle Pruefungen des Pruefers "Harms" abfrage
    Dann bekomme ich die Pruefungen "Datascience"

  Szenario: Ich frage die Pruefungen eines Pruefers mit Pruefungen, es gibt pro Pruefung mehrere Pruefer
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.04.2022
    Angenommen es existieren die folgenden Klausuren mit Pruefer:
      | Name        | Datum      | StartZeit | Dauer | Pruefer            |
      | AuD         | 10.01.2022 | 08:00     | 03:00 | Saering, Iwanowski |
      | DM          | 11.02.2022 | 08:00     | 03:00 | Saering, Harms     |
      | Datascience |            |           | 03:00 | Harms, Saering     |
    Wenn ich alle Pruefungen des Pruefers "Saering" abfrage
    Dann bekomme ich die Pruefungen "AuD, DM, Datascience"
