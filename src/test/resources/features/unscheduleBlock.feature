# language:de
Funktionalität: Als Planende*r moechte ich Bloecke ausplanen koennen

  Szenario: Ich plane einen Block erfolgreich aus
    Angenommen es existiert eine Pruefungsperiode von 10.05.2021 - 10.06.2021 mit dem Ankertag 15.05.2021
    Und es existiert ein geplanter Block "block" mit der Pruefung "pruefung" am 20.05.2021 um 12:00 Uhr
    Wenn ich den Block ausplane
    Dann ist der Block ungeplant
    Dann ist die Pruefung "pruefung" ungeplant

  Szenario: Ich plane einen Block erfolgreich aus, das Scoring einer Pruefung veraendert sich dadurch
    Angenommen es existiert eine Pruefungsperiode von 10.05.2021 - 10.06.2021 mit dem Ankertag 15.05.2021
    Und es existiert ein geplanter Block "block" mit der Pruefung "pruefung" und dem Teilnehmerkreis "B_INF 14.0 1" am 20.05.2021 um 12:00 Uhr
    Und es existiert die geplante Pruefung "pruefung geplant" am 21.05.2021 mit dem Teilnehmerkreis "B_INF 14.0 1"
    Wenn ich den Block ausplane
    Dann ist der Block ungeplant
    Dann ist die Pruefung "pruefung" ungeplant
    Dann hat sich das Scoring der Pruefung "pruefung geplant" veraendert

  Szenario: Ich versuche einen Block auszuplanen, den es nicht gibt
    Angenommen es existiert eine Pruefungsperiode von 10.05.2021 - 10.06.2021 mit dem Ankertag 15.05.2021
    Wenn ich versuche den Block "block" auszuplanen
    Dann erhalte ich einen Fehler

  Szenario: Ich versuche einen Block mit einer unbekannten Pruefung auszuplanen
    Angenommen es existiert eine Pruefungsperiode von 10.05.2021 - 10.06.2021 mit dem Ankertag 15.05.2021
    Und es existiert ein geplanter Block "block" mit der Pruefung "pruefung" am 09.06.2021 um 08:30 Uhr
    Wenn ich versuche den Block "block" mit der Pruefung "Analysis" auszuplanen
    Dann erhalte ich einen Fehler