#language: de
Funktionalität: Als Planer moechte ich Zugriff auf alle ungeplanten Bloecke haben

  Szenario: Alle Bloecke sind geplant
    Angenommen es existiert eine Pruefungsperiode
    Angenommen es existieren die folgenden Klausuren:
      | Name         | Datum | StartZeit | Dauer |
      | AuD          |       |           | 03:00 |
      | Datascience  |       |           | 03:00 |
      | KI           |       |           | 01:30 |
      | Rechnernetze |       |           | 01:30 |
    Angenommen es existieren die folgenden Bloecke:
      | Block   | Datum      | StartZeit | Pruefungen   |
      | block 1 | 12.02.2022 | 08:30     | KI           |
      | block 2 | 11.02.2022 | 08:30     | Datascience  |
      | block 3 | 10.02.2022 | 08:30     | AuD          |
      | block 4 | 09.02.2022 | 08:30     | Rechnernetze |
    Wenn ich alle ungeplanten Bloecke anfrage
    Dann erhalte ich keine Bloecke

  Szenario: Alle Bloecke sind ungeplant
    Angenommen es existiert eine Pruefungsperiode
    Und es existieren die folgenden Bloecke:
      | Block   | Datum | StartZeit | Pruefungen |
      | block 1 |       |           |            |
      | block 2 |       |           |            |
      | block 3 |       |           |            |
      | block 4 |       |           |            |
    Wenn ich alle ungeplanten Bloecke anfrage
    Dann erhalte ich die Bloecke "block 1, block 2, block 3, block 4"

  Szenario: Es gibt geplante und ungeplante Bloecke
    Angenommen es existiert eine Pruefungsperiode
    Und es existieren die folgenden Klausuren:
      | Name         | Datum | StartZeit | Dauer |
      | AuD          |       |           | 03:00 |
      | Datascience  |       |           | 03:00 |
      | KI           |       |           | 01:30 |
      | Rechnernetze |       |           | 01:30 |
    Und es existieren die folgenden Bloecke:
      | Block   | Datum      | StartZeit | Pruefungen   |
      | block 1 | 12.02.2022 | 08:30     | KI           |
      | block 2 |            |           | AuD          |
      | block 3 | 11.02.2022 | 08:30     | Datascience  |
      | block 4 |            |           | Rechnernetze |
    Wenn ich alle ungeplanten Bloecke anfrage
    Dann erhalte ich die Bloecke "block 2, block 4"

  Szenario: Es gibt keine Bloecke
    Angenommen es existiert eine Pruefungsperiode
    Und es gibt keine geplanten Bloecke
    Wenn ich alle ungeplanten Bloecke anfrage
    Dann erhalte ich keine Bloecke
