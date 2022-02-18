# language: de
Funktionalität: Als Planende*r moechte ich Pruefungen innerhalb eines Zeitraums abfragen.
  Datatable:

  Szenario: Ich frage den gesamten Zeitraum der Pruefungsperiode ab
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.03.2022
    Angenommen es existieren die folgenden Klausuren:
      | Name        | Datum      | StartZeit | Dauer |
      | AuD         | 10.01.2022 | 08:00     | 03:00 |
      | DM          | 11.02.2022 | 08:00     | 03:00 |
      | Datascience |            |           | 03:00 |

    Angenommen es existieren die folgenden Bloecke:
      | Block   | Datum      | StartZeit | Pruefungen  |
      | block 1 | 11.02.2022 | 08:30     | Datascience |
    Wenn ich alle Planungseinheiten im Zeitraum 01.01.2022 - 01.03.2022 anfrage
    Dann erhalte ich die Pruefungen "AuD, DM" und die Bloecke "block 1"

Szenario: Ich frage einen Zeitraum ab, der die Pruefungsperiode einschliesst
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.03.2022
    Angenommen es existieren die folgenden Klausuren:
      | Name        | Datum      | StartZeit | Dauer |
      | AuD         | 10.01.2022 | 08:00     | 03:00 |
      | DM          | 11.02.2022 | 08:00     | 03:00 |
      | Datascience |            |           | 03:00 |

    Angenommen es existieren die folgenden Bloecke:
      | Block   | Datum      | StartZeit | Pruefungen  |
      | block 1 | 11.02.2022 | 08:30     | Datascience |
    Wenn ich alle Planungseinheiten im Zeitraum 01.01.2021 - 01.03.2023 anfrage
    Dann erhalte ich die Pruefungen "AuD, DM" und die Bloecke "block 1"


  Szenario: Ich frage einen Zeitraum innerhalb der Pruefungsperiode ab
    Angenommen es existiert eine Pruefungsperiode von 01.02.2022 - 01.04.2022
    Angenommen es existieren die folgenden Klausuren:
      | Name        | Datum      | StartZeit | Dauer |
      | AuD         | 08.02.2022 | 08:00     | 03:00 |
      | DM          | 16.02.2022 | 08:00     | 03:00 |
      | Datascience |            |           | 03:00 |

    Angenommen es existieren die folgenden Bloecke:
      | Block   | Datum      | StartZeit | Pruefungen  |
      | block 1 | 09.03.2022 | 08:30     | Datascience |
    Wenn ich alle Planungseinheiten im Zeitraum 15.01.2022 - 01.03.2022 anfrage
    Dann erhalte ich die Pruefungen "DM, AuD"


  Szenario: Ich frage einen Zeitraum vor der Pruefungsperiode ab
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.03.2022
    Angenommen es existieren die folgenden Klausuren:
      | Name        | Datum      | StartZeit | Dauer |
      | AuD         | 10.01.2022 | 08:00     | 03:00 |
      | DM          | 11.02.2022 | 08:00     | 03:00 |
      | Datascience |            |           | 03:00 |

    Angenommen es existieren die folgenden Bloecke:
      | Block   | Datum      | StartZeit | Pruefungen  |
      | block 1 | 11.02.2022 | 08:30     | Datascience |
    Wenn ich alle Planungseinheiten im Zeitraum 01.12.2021 - 31.12.2021 anfrage
    Dann dann erhalte ich keine Planungseinheiten

Szenario: Ich frage einen Zeitraum nach der Pruefungsperiode ab
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.03.2022
    Angenommen es existieren die folgenden Klausuren:
      | Name        | Datum      | StartZeit | Dauer |
      | AuD         | 10.01.2022 | 08:00     | 03:00 |
      | DM          | 11.02.2022 | 08:00     | 03:00 |
      | Datascience |            |           | 03:00 |

    Angenommen es existieren die folgenden Bloecke:
      | Block   | Datum      | StartZeit | Pruefungen  |
      | block 1 | 11.02.2022 | 08:30     | Datascience |
    Wenn ich alle Planungseinheiten im Zeitraum 02.03.2022 - 01.04.2022 anfrage
    Dann dann erhalte ich keine Planungseinheiten

Szenario: Ich frage einen Zeitraum der teilweise vor der Pruefungsperiode liegt ab
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.03.2022
    Angenommen es existieren die folgenden Klausuren:
      | Name        | Datum      | StartZeit | Dauer |
      | AuD         | 08.01.2022 | 08:00     | 03:00 |
      | DM          | 11.02.2022 | 08:00     | 03:00 |
      | Datascience |            |           | 03:00 |

    Angenommen es existieren die folgenden Bloecke:
      | Block   | Datum      | StartZeit | Pruefungen  |
      | block 1 | 09.01.2022 | 08:30     | Datascience |
    Wenn ich alle Planungseinheiten im Zeitraum 12.12.2021 - 10.01.2022 anfrage
    Dann erhalte ich die Pruefungen "AuD" und die Bloecke "block 1"


Szenario: Ich frage einen Zeitraum der teilweise nach der Pruefungsperiode liegt ab
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.03.2022
    Angenommen es existieren die folgenden Klausuren:
      | Name        | Datum      | StartZeit | Dauer |
      | AuD         | 08.01.2022 | 08:00     | 03:00 |
      | DM          | 16.02.2022 | 08:00     | 03:00 |
      | Datascience |            |           | 03:00 |

    Angenommen es existieren die folgenden Bloecke:
      | Block   | Datum      | StartZeit | Pruefungen  |
      | block 1 | 09.02.2022 | 08:30     | Datascience |
    Wenn ich alle Planungseinheiten im Zeitraum 15.01.2022 - 01.04.2022 anfrage
    Dann erhalte ich die Pruefungen "DM" und die Bloecke "block 1"
