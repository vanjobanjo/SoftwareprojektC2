# language: de
Funktionalität: Als Planender moechte ich aus einer Menge von Zeitpunkten jene heraus gefiltert bekommen,
  an denen ich eine bestimmte Pruefung nicht einplanen kann.

  Szenario: Es werden alle verbotenen Zeitpunkte geliefert
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "Inf" im 4 Semester am 03.02.2022 um 08:00 Uhr
    Und es existiert die ungeplante Pruefung "CG" mit dem Teilnehmerkreis "Inf" im 4 Semester
    Wenn ich abfrage, welche der folgenden Zeitpunkte fuer die Pruefung "CG" verboten ist
      | Zeitpunkte              |
      | 02.02.2022 um 08:00 Uhr |
      | 03.02.2022 um 06:30 Uhr |
      | 03.02.2022 um 06:31 Uhr |
      | 03.02.2022 um 09:29 Uhr |
      | 03.02.2022 um 09:30 Uhr |
      | 04.02.2022 um 08:00 Uhr |
    Dann enthaelt das Ergebnis genau die Zeitpunkte
      | Zeitpunkte              |
      | 03.02.2022 um 06:31 Uhr |
      | 03.02.2022 um 09:29 Uhr |

  Szenario: Mit der Abgefragten Planungseinheit selbst kann es keinen Konflikt geben
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "Inf" im 4 Semester am 03.02.2022 um 08:00 Uhr
    Wenn ich abfrage, welche der folgenden Zeitpunkte fuer die Pruefung "Analysis" verboten ist
      | Zeitpunkte              |
      | 02.02.2022 um 08:00 Uhr |
      | 03.02.2022 um 06:30 Uhr |
      | 03.02.2022 um 06:31 Uhr |
      | 03.02.2022 um 09:29 Uhr |
      | 03.02.2022 um 09:30 Uhr |
      | 04.02.2022 um 08:00 Uhr |
    Dann enthaelt das Ergebnis keine Zeitpunkte

  Szenario: Zeitpunkte vor der Pruefungsperiode sind nicht erlaubt
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "Inf" im 4 Semester am 03.02.2022 um 08:00 Uhr
    Und es existiert die ungeplante Pruefung "CG" mit dem Teilnehmerkreis "Inf" im 4 Semester
    Wenn ich abfrage, welche der folgenden Zeitpunkte fuer die Pruefung "CG" verboten ist
      | Zeitpunkte              |
      | 02.01.2022 um 08:00 Uhr |
      | 03.02.2022 um 06:30 Uhr |
    Dann erhalte ich einen Fehler

  Szenario: Zeitpunkte nach der Pruefungsperiode sind nicht erlaubt
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "Inf" im 4 Semester am 03.02.2022 um 08:00 Uhr
    Und es existiert die ungeplante Pruefung "CG" mit dem Teilnehmerkreis "Inf" im 4 Semester
    Wenn ich abfrage, welche der folgenden Zeitpunkte fuer die Pruefung "CG" verboten ist
      | Zeitpunkte              |
      | 03.02.2022 um 06:30 Uhr |
      | 28.02.2022 um 08:00 Uhr |
    Dann erhalte ich einen Fehler

  Szenario: Abfragen fuer eine unbekannte Pruefung gehen nicht
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich abfrage, welche der folgenden Zeitpunkte fuer die unbekannte Pruefung "CG" verboten ist
      | Zeitpunkte              |
      | 03.02.2022 um 06:30 Uhr |
      | 28.02.2022 um 08:00 Uhr |
    Dann erhalte ich einen Fehler

  Szenario: Bei sequentiellen Bloecken wir die gesammtdauer beachtet
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante sequentielle Block "Blocky" am 02.02.2022 um 10:00 Uhr mit den Pruefungen
      | Pruefung | Studiengang | Semester |
      | Analysis | Inf         | 1        |
      | DM       | BWL         | 1        |
      | CG       | CGT         | 1        |
    Und es existiert die ungeplante Pruefung "Rechnernetze" mit dem Teilnehmerkreis "BWL" im 1 Semester
    Wenn ich abfrage, welche der folgenden Zeitpunkte fuer die Pruefung "Rechnernetze" verboten ist
      | Zeitpunkte              |
      | 02.02.2022 um 08:30 Uhr |
      | 02.02.2022 um 08:31 Uhr |
      | 02.02.2022 um 13:29 Uhr |
      | 02.02.2022 um 13:30 Uhr |
    Dann enthaelt das Ergebnis genau die Zeitpunkte
      | Zeitpunkte              |
      | 02.02.2022 um 08:31 Uhr |
      | 02.02.2022 um 13:29 Uhr |
