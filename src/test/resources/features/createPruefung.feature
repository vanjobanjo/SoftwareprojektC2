# language: de
Funktionalität: Als Planender moechte ich eine neue Pruefung erstellen koennen.

  Szenario: Eine Pruefung existiert nach ihrer Erstellung.
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich die Pruefung "Systemanalyse" erstelle
    Dann existiert die Pruefung "Systemanalyse" ungeplant

  Szenario: Eine Pruefung existiert bereits.
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert eine ungeplante Pruefung "Systemanalyse"
    Wenn ich die Pruefung "Systemanalyse" erstelle
    Dann erhalte ich einen Fehler

  Szenariogrundriss: Eine Pruefung kann nicht ohne Pruefungsnummer erstellt werden.
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich eine Pruefung ohne <Eigenschaft> erstelle
    Dann erhalte ich einen Fehler

    Beispiele:
      | Eigenschaft       |
      | "Pruefungsnummer" |
      | "Name"            |
      | "Referenz"        |
      | "Pruefer"         |
      | "PrueferName"     |
      | "Teilnehmerkreis" |

  Szenariogrundriss: Eine Pruefung kann nicht mit einer leeren Pruefungsnummer erstellt werden.
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich eine Pruefung mit leerer <Eigenschaft> erstelle
    Dann erhalte ich einen Fehler

    Beispiele:
      | Eigenschaft       |
      | "Pruefungsnummer" |
      | "Name"            |
      | "Referenz"        |
      | "PrueferName"     |

  Szenario: Eine Pruefung kann nicht mit ohne Gueltige Dauer erstellt werden.
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich die Pruefung "Systemanalyse" mit ungueltiger Dauer erstelle
    Dann erhalte ich einen Fehler

  Szenario: Eine Pruefung kann nicht mit negativer Anzahl an Teilnehmern zu einem Teilnehmerkreis erstellt werden.
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich eine Pruefung erstelle dessen Teilnehmerkreis eine negative Teilnehmerzahl hat
    Dann erhalte ich einen Fehler

  Szenario: Eine Pruefung kann nicht mit negativer Anzahl an Teilnehmern zu einem Teilnehmerkreis erstellt werden,
  auch nicht wenn die gesammtzahl an Teilnehmern positiv ist.
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich eine Pruefung erstelle, mit einem von mehreren Teilen Teilnehmerkreisen mit negativer Anzahl
    Dann erhalte ich einen Fehler
