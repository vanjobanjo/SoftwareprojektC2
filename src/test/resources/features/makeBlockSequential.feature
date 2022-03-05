# language: de
Funktionalität: Als Planende*r moechte ich Bloecke sequentiell machen koennen


#  ist schon parallel
#  beeinflusste Pruefungen durch besseres scoring
#  beeinflusste Bloecke durch besseres scoring
#  unbekannte Pruefung

  Szenario: Ein paralleler Block ist nach dem Aufruf sequentiell
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante parallele Block "Bloeckchen" am 03.02.2022 um 15:00 Uhr mit den Pruefungen
      | Pruefung |
      | Analysis |
      | DM       |
    Wenn ich den Block "Bloeckchen" auf sequentiell stelle
    Dann ist der Block "Bloeckchen" sequentiell


    Szenario: Ein paralleler Block kann nicht auf sequentiell geaendert werden, weil harte Kriterien verletzt werden
      Angenommen es existiert eine Pruefungsperiode
      Und es existiert der geplante parallele Block "Bloeckchen" am 03.02.2022 um 15:00 Uhr mit den Pruefungen
        | Pruefung | Studiengang | Semester |
        | Analysis | Inf         | 1        |
        | DM       | Inf         | 1        |
        | CG       | Inf         | 1        |
      Und es existiert die geplante Pruefung "pruefung" am 03.02.2022 um 16:30 Uhr mit dem Teilnehmerkreis "Inf" und dem Semester 1
      Wenn ich den Block "Bloeckchen" auf sequentiell stelle
      Dann bekomme ich eine Fehlermeldung HartesKriteriumException