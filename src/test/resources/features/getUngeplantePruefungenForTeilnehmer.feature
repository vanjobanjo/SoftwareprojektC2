# language: de
Funktionalität: Als Planender moechte ich abfragen koennen welche Pruefungen fur einen Teilnehmerkreis nicht eigeplant sind.

  Szenario: Es werden nur Pruefungen fuer den gesuchten Teilnehmerkreis geliefert
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "DM" mit dem Teilnehmerkreis "Inf" im 2 Semester
    Und es existiert die ungeplante Pruefung "CG" mit dem Teilnehmerkreis "BWL" im 2 Semester
    Und es existiert die ungeplante Pruefung "GG" mit dem Teilnehmerkreis "Inf" im 1 Semester
    Wenn ich die ungeplanten Pruefungen zum Teilnehmerkreis "Inf" Semester 2 abfrage
    Dann erhalte ich die Pruefungen "DM"

  Szenario: Es werden nur ungeplante Pruefungen beruecksichtigt
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "Inf" im 2 Semester am 01.02.2022
    Und es existiert die ungeplante Pruefung "CG" mit dem Teilnehmerkreis "Inf" im 2 Semester
    Und es existiert die geplante Pruefung "DM" mit dem Teilnehmerkreis "Inf" im 2 Semester am 07.02.2022
    Wenn ich die ungeplanten Pruefungen zum Teilnehmerkreis "Inf" Semester 2 abfrage
    Dann erhalte ich die Pruefungen "CG"

  Szenario: Pruefungen in Bloecken werden erkannt
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "DM" mit dem Teilnehmerkreis "BWL" im 2 Semester am 07.02.2022
    Und es existiert ein ungeplanter Block "Blocki" mit der Pruefung "CG" und dem Teilnehmerkreis "Inf"
    Wenn ich den Teilnehmerkreis "Inf" mit 5 Studenten zu "CG" hinzufuege
    Und ich die ungeplanten Pruefungen zum Teilnehmerkreis "Inf" Semester 1 abfrage
    Dann erhalte ich die Pruefungen "CG"
