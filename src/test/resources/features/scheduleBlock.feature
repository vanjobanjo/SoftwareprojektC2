#language: de
Funktionalität: Als Planende*r moechte ich Bloecke einplanen koennen

  Szenario: Ich plane erfolgreich einen ungeplanten Block ein
    Angenommen es existiert eine Pruefungsperiode von 10.01.2022 - 10.02.2022 mit dem Ankertag 20.01.2022
    Und es existiert ein ungeplanter Block "block" mit der Pruefung "pruefung"
    Wenn ich den Block am 22.01.2022 einplane
    Dann ist der Block "block" am 22.01.2022 geplant
    Und es werden keine Restriktionen verletzt

  Szenario: Ich plane erfolgreich einen Block ein und es werden weiche Kriterien verletzt
    Angenommen es existiert eine Pruefungsperiode von 20.03.2022 - 30.04.2022 mit dem Ankertag 01.04.2022
    Und es existiert ein ungeplanter Block "block" mit der Pruefung "pruefung" und dem Teilnehmerkreis "B_INF 3 14.0"
    Und es existiert die geplante Pruefung "geplante Pruefung" am 21.03.2022 um 08:00 Uhr mit dem Teilnehmerkreis "B_INF 3 14.0"
    Wenn ich den Block am 20.03.2022 um 08:00 Uhr einplane
    Dann ist der Block "block" am 20.03.2022 um 08:00 Uhr geplant
    Und es werden Restriktionen verletzt


  Szenario: Ich plane erfolgreich einen Block ein und es werden harte Kriterien verletzt
    Angenommen es existiert eine Pruefungsperiode von 20.02.2022 - 30.04.2022 mit dem Ankertag 01.04.2022
    Und es existiert ein ungeplanter Block "block" mit der Pruefung "pruefung" und dem Teilnehmerkreis "B_INF 3 14.0"
    Und es existiert die geplante Pruefung "geplante Pruefung" am 20.03.2022 um 08:00 Uhr mit dem Teilnehmerkreis "B_INF 3 14.0"
    Wenn ich den Block am 20.03.2022 um 08:00 Uhr einplane
    Dann wird eine harte Restriktion verletzt

  Szenario: Ich versuche einen geplanten Block an so umzuplanen, dass eine harte Restriktion verletzt wird
    Angenommen es existiert eine Pruefungsperiode von 20.02.2022 - 30.04.2022 mit dem Ankertag 01.04.2022
    Und es existiert ein geplanter Block "block" mit der Pruefung "pruefung" und dem Teilnehmerkreis "B_INF 3 14.0" am 22.02.2022 um 10:00 Uhr
    Und es existiert die geplante Pruefung "geplante Pruefung" am 21.03.2022 um 08:00 Uhr mit dem Teilnehmerkreis "B_INF 3 14.0"
    Wenn ich den Block am 21.03.2022 um 08:00 Uhr einplane
    Dann wird eine harte Restriktion verletzt
    Dann ist der Block wieder "block" am 22.02.2022 um 10:00 Uhr geplant

  Szenario: Ich versuche einen Block einzuplanen, den es nicht gibt
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.03.2022 mit dem Ankertag 01.02.2022
    Wenn ich versuche den Block "block" am 20.02.2022 um 09:00 Uhr einzuplanen
    Dann erhalte ich einen Fehler

  Szenario: Ich versuche einen Block mit einer Pruefung, die es nicht gibt einzuplanen
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.03.2022 mit dem Ankertag 01.02.2022
    Angenommen es existiert der ungeplante Block "block" mit der Pruefung "Analysis"
    Wenn ich den Block mit der unbekannten Pruefung "pruefung" am 20.02.2022 um 12:00 Uhr einplane
    Dann erhalte ich einen Fehler

  Szenario: Ich versuche einen Block nach der Pruefungsperiode einzuplanen
    Angenommen es existiert eine Pruefungsperiode von 10.01.2022 - 10.02.2022 mit dem Ankertag 20.01.2022
    Und es existiert ein ungeplanter Block "block" mit der Pruefung "pruefung"
    Wenn ich den Block am 22.10.2022 einplane
    Dann erhalte ich einen Fehler

  Szenario: Ich versuche einen Block vor der Pruefungsperiode einzuplanen
    Angenommen es existiert eine Pruefungsperiode von 10.01.2022 - 10.02.2022 mit dem Ankertag 20.01.2022
    Und es existiert ein ungeplanter Block "block" mit der Pruefung "pruefung"
    Wenn ich den Block am 22.10.2021 einplane
    Dann erhalte ich einen Fehler


  Szenario: Ich versuche einen geplanten Block an so umzuplanen, dass eine harte Restriktion verletzt wird bugfix test
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert ein geplanter Block "block" mit der Pruefung "pruefung" und dem Teilnehmerkreis "B_INF 3 14.0" am 02.02.2022 um 10:00 Uhr
    Und es existiert die geplante Pruefung "geplantePruefung" mit dem Teilnehmerkreis "B_INF 3 14.0" im 1 Semester am 03.02.2022 um 8 Uhr
    Wenn ich den Block "block" am 15.02.2022 um 08:00 Uhr einplane bugfix
    Dann ist der Block "block" am 15.02.2022 um 08:00 Uhr geplant
    Und die Pruefung "geplantePruefung" wird auch das Scoring verändert;

  Szenario: Ich versuche einen geplanten Block an so umzuplanen, dass eine harte Restriktion verletzt wird bugfix mit mehrere_Pruefung_am_Tag
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert ein geplanter Block "block" mit der Pruefung "pruefung" und dem Teilnehmerkreis "B_INF 3 14.0" am 02.02.2022 um 10:00 Uhr
    Und es existiert ein geplanter Block "block1" mit der Pruefung "pruefung1" und dem Teilnehmerkreis "B_INF 3 14.0" am 01.02.2022 um 16:00 Uhr
    Und es existiert die geplante Pruefung "geplantePruefung" mit dem Teilnehmerkreis "B_INF 3 14.0" im 1 Semester am 02.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "geplantePruefung1" mit dem Teilnehmerkreis "B_INF 3 14.0" im 1 Semester am 03.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "geplantePruefung2" mit dem Teilnehmerkreis "B_INF 3 14.0" im 1 Semester am 01.02.2022 um 8 Uhr
    Wenn ich den Block "block" am 15.02.2022 um 08:00 Uhr einplane bugfix
    Und die Pruefung "geplantePruefung" wird auch das Scoring verändert;
    Und die Pruefung "geplantePruefung2" wird auch das Scoring verändert;
    Und die Pruefung "geplantePruefung1" wird auch das Scoring verändert;
    Und die Pruefung "pruefung1" wird auch das Scoring verändert;
    Und der Block "block1" wird auch das Scoring verändert;


