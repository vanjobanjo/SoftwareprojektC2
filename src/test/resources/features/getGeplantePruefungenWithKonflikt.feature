# language: de
Funktionalität: Als Planender moechte ich erfahren koennen, mit welchen Pruefungen eine Pruefung immer in Konflikt stehen wird.

  Szenario: Frage erfolgreich Konflikte ab aber es gibt keine
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis"
    Wenn ich Konflikte mit geplanten Pruefungen abfrage fuer "Analysis"
    Dann erhalte ich keine Pruefungen

  Szenario: Frage erfolgreich Konflikte ab und alle Konflikte werden gefunden
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "DM" mit dem Teilnehmerkreis "Inf" im 1 Semester am 01.02.2022
    Und es existiert die ungeplante Pruefung "Rechnernetze" mit dem Teilnehmerkreis "Inf" im 1 Semester
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "Inf" im 1 Semester
    Wenn ich Konflikte mit geplanten Pruefungen abfrage fuer "Analysis"
    Dann enthaelt das Ergebnis als einzige Pruefungen "DM"

  Szenario: Frage Konflikte einer unbekannten Pruefung ab
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich Konflikte mit geplanten Pruefungen fuer eine ungeplante Pruefung abfrage
    Dann erhalte ich einen Fehler
