# language: de
Funktionalität: Als Planer*in moechte ich die Dauer von einer Pruefung aender koennen

  Szenario: Ich aender erfolgreich die Dauer einer Pruefung
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester und Dauer von 60 Minuten
    Wenn ich die Dauer der Pruefung "Analysis" auf 120 Minuten ändere
    Dann hat die Pruefung "Analysis" die Dauer von 120 Minuten

  Szenario: Ich aender erfolgreich die Dauer einer Pruefung
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester und Dauer von 60 Minuten
    Wenn ich die Dauer der Pruefung "Analysis" auf 1 Minuten ändere
    Dann hat die Pruefung "Analysis" die Dauer von 1 Minuten


  Szenario: Ich aender nicht erfolgreich die Dauer einer Pruefung
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester und Dauer von 60 Minuten
    Wenn ich die Dauer der Pruefung "Analysis" auf -60 Minuten ändere
    Dann hat die Pruefung "Analysis" die Dauer von 60 Minuten
    Und bekomme ich eine Fehlermeldung IllegalArgumentException

  Szenario: Ich aender nicht erfolgreich die Dauer einer Pruefung
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester und Dauer von 60 Minuten
    Wenn ich die Dauer der Pruefung "Analysis" auf 0 Minuten ändere
    Dann hat die Pruefung "Analysis" die Dauer von 60 Minuten
    Und bekomme ich eine Fehlermeldung IllegalArgumentException


  Szenario: Ich moechte die Dauer von einer Pruefung aender und es Kommt zu einer HartesKriteriumException
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester und Dauer von 60 Minuten am 02.02.2022 um 08:00 Uhr
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "inf" im 1 Semester und Dauer von 60 Minuten am 02.02.2022 um 09:30 Uhr
    Wenn ich die Dauer der Pruefung "Analysis" auf 61 Minuten ändere
    Dann bekomme ich eine Fehlermeldung HartesKriteriumException
    Und hat die Pruefung "Analysis" die Dauer von 60 Minuten

    Szenario: Ich möchte die IllegalStateException hervorrufen
      Angenommen es existiert eine Pruefungsperiode
      Wenn ich eine Pruefung eine dauer aendermoechte die nicht existiert
      Dann bekomme ich eine Fehlermeldung IllegalStateException


