# language: de
Funktionalität: Als Planender moechte ich eine Pruefung ausplanen koennen.

  Szenario: Ich plane erfolgreich eine Pruefung aus
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis"
    Wenn ich die Pruefung "Analysis" ausplanen moechte
    Dann ist die Pruefung "Analysis" nicht eingeplant

  Szenario: Eine Pruefung in einer geplanten Gruppe kann nicht ausgeplant werden
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante Block "B1" mit der Pruefung "Analysis"
    Wenn ich die Pruefung "Analysis" ausplanen moechte
    Dann erhalte ich einen Fehler

  Szenario: Eine Pruefung in einer ungeplanten Gruppe kann nicht ausgeplant werden
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der Block "B1" mit der Pruefung "Analysis"
    Wenn ich die Pruefung "Analysis" ausplanen moechte
    Dann erhalte ich einen Fehler

  Szenario: Eine ausgeplante Pruefung ist im Ergebnis enthalten
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis"
    Wenn ich die Pruefung "Analysis" ausplanen moechte
    Dann enthaelt das Ergebnis genau die Pruefung "Analysis"

  Szenario: Eine ausgeplante Pruefung ist im Ergebnis enthalten
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "bwl" im 1 Semester am 14.02.2022
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "bwl" im 1 Semester am 15.02.2022
    Wenn ich die Pruefung "Analysis" ausplanen moechte
    Dann enthaelt das Ergebnis als einzige Pruefungen "Analysis", "BWL"

  Szenario: Eine beeinflusste Pruefung ist im Ergebnis enthalten
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "bwl" im 1 Semester am 14.02.2022
    Und es existiert der geplante Block "B1" mit der Pruefung "BWL" direkt nach "Analysis"
    Und die Pruefung "BWL" hat einen Teilnehmerkreis "bwl" mit 10 Studenten
    Wenn ich die Pruefung "Analysis" ausplanen moechte
    Dann enthaelt das Ergebnis als einzige Pruefungen "Analysis", "BWL"

  Szenario: Der Block einer beeinflussten Pruefung ist im Ergebnis enthalten
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "bwl" im 1 Semester am 14.02.2022
    Und es existiert der geplante Block "B1" mit der Pruefung "BWL" direkt nach "Analysis"
    Und die Pruefung "BWL" hat einen Teilnehmerkreis "bwl" mit 10 Studenten
    Wenn ich die Pruefung "Analysis" ausplanen moechte
    Dann enthaelt das Ergebnis als einzige Bloecke "B1"

  Szenario: Eine unbekannte Pruefung kann nicht ungeplant werden
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich die unbekannte Pruefung "Zaubertraenke" ausplane
    Dann erhalte ich einen Fehler
