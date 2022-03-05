# language: de

Funktionalität: Als Planende*r moechte ich die Anzahl an Studenten, die zu einem Zeitpunkt eine Pruefung schreiben, wissen

  Szenario: Es ist keine Pruefung geplant und ich frage die Anzahl an Studenten ab
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.03.2022
    Wenn ich die Anzahl an Studenten die am 12.01.2022 um 08:00 Uhr eine Pruefung schreiben anfrage
    Dann erhalte ich die Anzahl 0

  Szenario: Es ist eine Pruefung geplant und ich frage die Anzahl während dieser Pruefung ab
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.03.2022 mit dem Ankertag 10.01.2022
    Und es existiert die geplante Pruefung "pruefung" am 11.02.2022 um 12:00 Uhr mit 10 Teilnehmern
    Wenn ich die Anzahl an Studenten die am 11.02.2022 um 12:30 Uhr eine Pruefung schreiben anfrage
    Dann erhalte ich die Anzahl 10

  Szenario: Es sind mehrere Pruefungen geplant und ich frage die Anzahl während dieser Pruefungen ab
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.03.2022 mit dem Ankertag 10.01.2022
    Und es existiert die geplante Pruefung "pruefung" am 11.02.2022 um 12:00 Uhr mit 10 Teilnehmern
    Und es existiert die geplante Pruefung "pruefung2" am 11.02.2022 um 12:10 Uhr mit 10 Teilnehmern
    Wenn ich die Anzahl an Studenten die am 11.02.2022 um 12:30 Uhr eine Pruefung schreiben anfrage
    Dann erhalte ich die Anzahl 20

  Szenario: Es sind mehrere Pruefungen geplant und ich frage die Anzahl während keiner dieser Pruefungen ab
    Angenommen es existiert eine Pruefungsperiode von 01.01.2022 - 01.03.2022 mit dem Ankertag 10.01.2022
    Und es existiert die geplante Pruefung "pruefung" am 11.02.2022 um 12:00 Uhr mit 10 Teilnehmern
    Und es existiert die geplante Pruefung "pruefung2" am 11.02.2022 um 12:10 Uhr mit 10 Teilnehmern
    Wenn ich die Anzahl an Studenten die am 12.02.2022 um 12:30 Uhr eine Pruefung schreiben anfrage
    Dann erhalte ich die Anzahl 0

  Szenario: Es ist ein paralleler Block geplant und ich frage die Anzahl der Studenten dessen Anfang ab
    Angenommen es existiert eine Pruefungsperiode von 01.08.2022 - 14.09.2022 mit dem Ankertag 29.08.2022
    Und es existiert der am 30.08.2022 um 12:00 Uhr geplante "parallele" Block "block" mit den Pruefungen
      | Pruefung | Teilnehmeranzahl | Dauer |
      | Analysis | 2                | 1:30  |
      | DM       | 10               | 2:00  |
      | SP       | 3                | 1:00  |
      | AuD      | 5                | 3:00  |
    Wenn ich die Anzahl an Studenten die am 30.08.2022 um 12:00 Uhr eine Pruefung schreiben anfrage
    Dann erhalte ich die Anzahl 20

  Szenario: Es ist ein sequentieller Block geplant und ich frage die Anzahl der Studenten dessen Anfang ab
    Angenommen es existiert eine Pruefungsperiode von 01.08.2022 - 14.09.2022 mit dem Ankertag 29.08.2022
    Und es existiert der am 30.08.2022 um 12:00 Uhr geplante "sequentielle" Block "block" mit den Pruefungen
      | Pruefung | Teilnehmeranzahl | Dauer |
      | Analysis | 2                | 1:30  |
      | DM       | 10               | 2:00  |
      | SP       | 3                | 1:00  |
      | AuD      | 5                | 3:00  |
    Wenn ich die Anzahl an Studenten die am 30.08.2022 um 12:00 Uhr eine Pruefung schreiben anfrage
    Dann erhalte ich die Anzahl 20

  Szenario: Es ist ein paralleler Block geplant und ich frage die Anzahl der Studenten
  zu einem Zeitpunkt ab, ab dem manche Pruefungen schon vorbei sind
    Angenommen es existiert eine Pruefungsperiode von 01.08.2022 - 14.09.2022 mit dem Ankertag 29.08.2022
    Und es existiert der am 30.08.2022 um 12:00 Uhr geplante "parallele" Block "block" mit den Pruefungen
      | Pruefung | Teilnehmeranzahl | Dauer |
      | Analysis | 2                | 1:30  |
      | DM       | 10               | 2:00  |
      | SP       | 3                | 1:00  |
      | AuD      | 5                | 3:00  |
    Wenn ich die Anzahl an Studenten die am 30.08.2022 um 14:59 Uhr eine Pruefung schreiben anfrage
    Dann erhalte ich die Anzahl 5

  Szenario: Es sind ein paralleler Block und eine Pruefung geplant und ich frage die Anzahl der Studenten
  zu einem Zeitpunkt ab
    Angenommen es existiert eine Pruefungsperiode von 01.08.2022 - 14.09.2022 mit dem Ankertag 29.08.2022
    Und es existiert der am 30.08.2022 um 12:00 Uhr geplante "parallele" Block "block" mit den Pruefungen
      | Pruefung | Teilnehmeranzahl | Dauer |
      | Analysis | 2                | 1:30  |
      | DM       | 10               | 2:00  |
      | SP       | 3                | 1:00  |
      | AuD      | 5                | 3:00  |
    Und es existiert die geplante Pruefung "pruefung" am 30.08.2022 um 14:30 Uhr mit 10 Teilnehmern
    Wenn ich die Anzahl an Studenten die am 30.08.2022 um 14:59 Uhr eine Pruefung schreiben anfrage
    Dann erhalte ich die Anzahl 15

  Szenario: Es ist ein sequentieller Block geplant und ich frage die Anzahl der Studenten dessen Anfang ab
    Angenommen es existiert eine Pruefungsperiode von 01.08.2022 - 14.09.2022 mit dem Ankertag 29.08.2022
    Und es existiert der am 30.08.2022 um 12:00 Uhr geplante "sequentielle" Block "block" mit den Pruefungen
      | Pruefung | Teilnehmeranzahl | Dauer |
      | Analysis | 2                | 1:30  |
      | DM       | 10               | 2:00  |
      | SP       | 3                | 1:00  |
      | AuD      | 5                | 3:00  |
    Wenn ich die Anzahl an Studenten die am 30.08.2022 um 14:00 Uhr eine Pruefung schreiben anfrage
    Dann erhalte ich die Anzahl 15

  Szenario: Ich frage einen Termin vor der Pruefungsperiode ab
    Angenommen es existiert eine Pruefungsperiode von 01.08.2022 - 14.09.2022 mit dem Ankertag 29.08.2022
    Wenn ich die Anzahl an Studenten die am 31.07.2022 um 08:00 Uhr eine Pruefung schreiben anfrage
    Dann erhalte ich die Anzahl 0

  Szenario: Ich frage einen Termin nach der Pruefungsperiode ab
    Angenommen es existiert eine Pruefungsperiode von 01.08.2022 - 14.09.2022 mit dem Ankertag 29.08.2022
    Wenn ich die Anzahl an Studenten die am 30.08.2022 um 08:00 Uhr eine Pruefung schreiben anfrage
    Dann erhalte ich die Anzahl 0
