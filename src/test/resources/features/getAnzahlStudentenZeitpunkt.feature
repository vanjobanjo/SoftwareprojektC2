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