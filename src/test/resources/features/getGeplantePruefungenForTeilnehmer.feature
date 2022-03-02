# language: de
Funktionalität: Als Planender moechte ich abfragen koennen welche Pruefungen fur einen Teilnehmerkreis eigeplant sind.

#  alle geplanten / keine ungeplanten
#  Pruefungen in Bloecken auch
#  keine Periode

  Szenario: Es werden nur Pruefungen fuer den gesuchten Teilnehmerkreis geliefert
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "Inf" im 2 Semester am 01.02.2022
    Und es existiert die geplante Pruefung "DM" mit dem Teilnehmerkreis "BWL" im 2 Semester am 04.02.2022
    Und es existiert die geplante Pruefung "CG" mit dem Teilnehmerkreis "Inf" im 2 Semester am 07.02.2022
    Wenn ich die geplanten Pruefungen zum Teilnehmerkreis "Inf" Semester 2 abfrage
    Dann erhalte ich die Pruefungen "Analysis", "CG"

  Szenario: Es werden nur geplante Pruefungen beruecksichtigt
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "Inf" im 2 Semester am 01.02.2022
    Und es existiert die ungeplante Pruefung "CG" mit dem Teilnehmerkreis "Inf" im 2 Semester
    Und es existiert die geplante Pruefung "DM" mit dem Teilnehmerkreis "Inf" im 2 Semester am 07.02.2022
    Wenn ich die geplanten Pruefungen zum Teilnehmerkreis "Inf" Semester 2 abfrage
    Dann erhalte ich die Pruefungen "Analysis", "DM"

  Szenario: Pruefungen in Bloecken werden erkannt
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "DM" mit dem Teilnehmerkreis "BWL" im 2 Semester am 07.02.2022
    Und es existiert der geplante Block "block 1" mit der Pruefung "Analysis"
    Wenn ich den Teilnehmerkreis "Inf" mit 5 Studenten zu "Analysis" hinzufuege
    Und ich die geplanten Pruefungen zum Teilnehmerkreis "Inf" Semester 1 abfrage
    Dann erhalte ich die Pruefungen "Analysis"
