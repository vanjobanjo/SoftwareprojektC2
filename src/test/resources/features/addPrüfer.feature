# language: de
Funktionalität: Als Planender möchte ich bei einer Prüfung einen Prüfer hinzufügen.

  Szenario: Eine Prüfung hat noch keinen Prüfer und ich füge erfolgreich einen Prüfer hinzu.
    Angenommen die Prüfung "Informationstechnik" hat keinen Prüfer als Prüfer
    Wenn ich der Prüfung "Informationstechnik" den Prüfer "Prof. Dr. Dennis Saering" hinzufüge
    Dann hat die Prüfung "Informationstechnik" den Prüfer "Prof. Dr. Dennis Saering" eingetragen


  Szenario: Eine Prüfung hat schon einen Prüfer und ich füge erfolgreich einen Prüfer hinzu.
    Angenommen die Prüfung "Informationstechnik" hat "Prof. Dr. Dennis Saering" als Prüfer
    Wenn ich der Prüfung "Informationstechnik" den Prüfer "Birger Wolter" hinzufüge
    Dann hat die Prüfung "Informationstechnik" den Prüfer "Prof. Dr. Dennis Saering" und "Birger Wolter" eingetragen

  Szenario: Eine Prüfung hat schon einen Prüfer und ich füge erfolgreich einen Prüfer hinzu.
    Angenommen die Prüfung "Informationstechnik" hat "Prof. Dr. Dennis Saering" als Prüfer
    Wenn ich der Prüfung "Informationstechnik" den Prüfer "Prof. Dr. Dennis Saering" hinzufüge
    Dann hat die Prüfung "Informationstechnik" den Prüfer "Prof. Dr. Dennis Saering" eingetragen


  Szenario: Eine Prüfung hat schon zwei Prüfer und ich füge erfolgreich einen Prüfer hinzu.
    Angenommen die Prüfung "Informationstechnik" hat "Prof. Dr. Dennis Saering" und "Birger Wolter" als Prüfer
    Wenn ich der Prüfung "Informationstechnik" den Prüfer "Ilja Kaleck" hinzufüge
    Dann hat die Prüfung "Informationstechnik" den Prüfer "Prof. Dr. Dennis Saering" und "Birger Wolter" und "Ilja Kaleck" eingetragen

