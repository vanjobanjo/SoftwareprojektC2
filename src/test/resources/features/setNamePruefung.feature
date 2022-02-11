# language: de

  Funktionalität: Als Pruefer*in moechte ich den Namen von Pruefungen anpassen koennen

    Szenario: Ich aendere erfolgreich den Namen einer geplanten Pruefung
      Angenommen es existiert die geplante Pruefung "Pruefung 1"
      Wenn ich den Namen der Pruefung auf "Pruefung 3" aendere
      Dann erhalte ich eine Pruefung mit dem Namen "Pruefung 3"


    Szenario: Ich aendere erfolgreich den Namen einer ungeplanten Pruefung
      Angenommen es existiert die ungeplante Pruefung "Pruefung 3"
      Wenn ich den Namen der Pruefung auf "Pruefung mit neuem Namen" aendere
      Dann erhalte ich eine Pruefung mit dem Namen "Pruefung mit neuem Namen"


    Szenario: Ich versuche den Namen einer Pruefung zu aendern, den es nicht gibt
      Angenommen es existiert keine Pruefung mit dem Namen "Pruefung 1"
      Wenn ich den Namen der Pruefung auf "Pruefung 42" aendere
      Dann erhalte ich eine Fehlermeldung

    Szenario: Ich versuche den Namen einer Pruefung auf einen bereits vergebenen Namen zu ändern
      Angenommen es existieren die Pruefungen "Pruefung 1" und "Pruefung 2"
      Wenn ich den Namen von "Pruefung 1" auf "Pruefung 2" aendere
      Dann erhalte ich eine Fehlermeldung
