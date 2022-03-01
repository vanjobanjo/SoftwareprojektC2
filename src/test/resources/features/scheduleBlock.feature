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


  Szenario: Ich plane erfolgreich einen Block ein und es werden weiche Kriterien verletzt
    Angenommen es existiert eine Pruefungsperiode von 20.02.2022 - 30.04.2022 mit dem Ankertag 01.04.2022
    Und es existiert ein ungeplanter Block "block" mit der Pruefung "pruefung" und dem Teilnehmerkreis "B_INF 3 14.0"
    Und es existiert die geplante Pruefung "geplante Pruefung" am 20.03.2022 um 08:00 Uhr mit dem Teilnehmerkreis "B_INF 3 14.0"
    Wenn ich den Block am 20.03.2022 um 08:00 Uhr einplane
    Dann wird eine harte Restriktion verletzt

