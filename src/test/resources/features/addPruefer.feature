# language: de
Funktionalität: Als Planender muechte ich bei einer Pruefung einen Pruefer hinzufuegen.

  Szenario: Eine Pruefung hat noch keinen Pruefer und ich fuege erfolgreich einen Pruefer hinzu.
    Angenommen die Pruefung "Informationstechnik" hat keinen Pruefer als Pruefer
    Wenn ich der Pruefung "Informationstechnik" den Pruefer "Prof. Dr. Dennis Saering" hinzufuege
    Dann hat die Pruefung "Informationstechnik" den Pruefer "Prof. Dr. Dennis Saering" eingetragen


  Szenario: Eine Pruefung hat schon einen Pruefer und ich fuege erfolgreich einen Pruefer hinzu.
    Angenommen die Pruefung "Informationstechnik" hat "Prof. Dr. Dennis Saering" als Pruefer
    Wenn ich der Pruefung "Informationstechnik" den Pruefer "Birger Wolter" hinzufuege
    Dann hat die Pruefung "Informationstechnik" den Pruefer "Prof. Dr. Dennis Saering" und "Birger Wolter" eingetragen

  Szenario: Eine Pruefung hat schon einen Pruefer und ich probiere den gleichen Pruefer hinzu zufuegen.
    Angenommen die Pruefung "Informationstechnik" hat "Prof. Dr. Dennis Saering" als Pruefer
    Wenn ich der Pruefung "Informationstechnik" den Pruefer "Prof. Dr. Dennis Saering" hinzufuege
    Dann hat die Pruefung "Informationstechnik" den Pruefer "Prof. Dr. Dennis Saering" eingetragen


  Szenario: Eine Pruefung hat schon zwei Pruefer und ich fuege erfolgreich einen Pruefer hinzu.
    Angenommen die Pruefung "Informationstechnik" hat "Prof. Dr. Dennis Saering" und "Birger Wolter" als Pruefer
    Wenn ich der Pruefung "Informationstechnik" den Pruefer "Ilja Kaleck" hinzufuege
    Dann hat die Pruefung "Informationstechnik" den Pruefer "Prof. Dr. Dennis Saering" und "Birger Wolter" und "Ilja Kaleck" eingetragen

