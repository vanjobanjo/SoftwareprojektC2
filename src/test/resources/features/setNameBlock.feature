# language: de
Funktionalität: Als Planer*in moechte ich den Namen von Bloecken anpassen koennen

  Szenario: Ich aendere erfolgreich den Namen eines geplanten Blocks
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante Block "block"
    Wenn ich den Namen des Blocks auf "Block 3" aendere
    Dann erhalte ich einen Block mit dem Namen "Block 3"


  Szenario: Ich aendere erfolgreich den Namen eines ungeplanten Blocks
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der ungeplante Block "block"
    Wenn ich den Namen des Blocks auf "Block mit neuem Namen" aendere
    Dann erhalte ich einen Block mit dem Namen "Block mit neuem Namen"


  Szenario: Ich versuche den Namen eines Blocks zu aendern, den es nicht gibt
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert kein Block mit dem Namen "block"
    Wenn ich versuche den Namen des Blocks auf "Block 42" zu aendern
    Dann erhalte ich einen Fehler

  Szenario: Ich versuche den Namen eines Blocks auf einen leeren String zu aendern
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der ungeplante Block "block"
    Wenn ich versuche den Namen des Blocks auf "" zu aendern
    Dann erhalte ich einen Fehler