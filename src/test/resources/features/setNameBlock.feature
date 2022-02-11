# language: de
Funktionalität: Als Planer*in moechte ich den Namen von Bloecken anpassen koennen

  Szenario: Ich aendere erfolgreich den Namen eines geplanten Blocks
    Angenommen es existiert der geplante Block "Block 1"
    Wenn ich den Namen des Blocks auf "Block 3" aendere
    Dann erhalte ich einen Block mit dem Namen "Block 3"


  Szenario: Ich aendere erfolgreich den Namen eines ungeplanten Blocks
    Angenommen es existiert der ungeplante Block "Block 3"
    Wenn ich den Namen des Blocks auf "Block mit neuem Namen" aendere
    Dann erhalte ich einen Block mit dem Namen "Block mit neuem Namen"


  Szenario: Ich versuche den Namen eines Blocks zu aendern, den es nicht gibt
    Angenommen es existiert kein Block mit dem Namen "Block 1"
    Wenn ich den Namen des Blocks auf "Block 42" aendere
    Dann erhalte ich eine Fehlermeldung

    Szenario: Ich versuche den Namen eines Blocks auf einen bereits vergebenen Namen zu ändern
      Angenommen es existieren die Bloecke "Block 1" und "Block 2"
      Wenn ich den Namen von "Block 1" auf "Block 2" aendere
      Dann erhalte ich eine Fehlermeldung

