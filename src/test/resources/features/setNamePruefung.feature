# language: de

Funktionalität: Als Pruefer*in moechte ich den Namen von Pruefungen anpassen koennen

  Szenario: Ich aendere erfolgreich den Namen einer geplanten Pruefung
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "pruefung"
    Wenn ich den Namen der Pruefung auf "pruefung 3" aendere
    Dann erhalte ich eine Pruefung mit dem Namen "pruefung 3"


  Szenario: Ich aendere erfolgreich den Namen einer Pruefung, die sich in einem Block befindet
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante Block "block" mit der Pruefung "pruefung"
    Wenn ich den Namen der Pruefung auf "Pruefung neu" aendere
    Dann erhalte ich den Block "block" mit der geaenderten Pruefung "Pruefung neu"

  Szenario: Ich aendere erfolgreich den Namen einer ungeplanten Pruefung
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "pruefung"
    Wenn ich den Namen der Pruefung auf "Pruefung mit neuem Namen" aendere
    Dann erhalte ich eine Pruefung mit dem Namen "Pruefung mit neuem Namen"


  Szenario: Ich versuche den Namen einer Pruefung zu aendern, den es nicht gibt
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert keine Pruefung mit dem Namen "pruefung"
    Wenn ich versuche den Namen der Pruefung auf "Pruefung 42" zu aendern
    Dann erhalte ich einen Fehler

  Szenario: Ich versuche den Namen einer Pruefung zu aendern, den es nicht gibt
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert keine Pruefung mit dem Namen "pruefung"
    Wenn ich versuche den Namen der Pruefung auf "" zu aendern
    Dann erhalte ich einen Fehler
