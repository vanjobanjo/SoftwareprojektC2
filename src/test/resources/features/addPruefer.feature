# language: de
Funktionalität: Als Planender moechte ich bei einer Pruefung einen Pruefer hinzufuegen.

  Szenario: Eine Pruefung hat noch keinen Pruefer und ich fuege erfolgreich einen Pruefer hinzu.
    Angenommen es existiert eine Pruefungsperiode
    Und die Pruefung "Informationstechnik" hat keinen Pruefer
    Wenn ich der Pruefung "Informationstechnik" den Pruefer "Prof. Dr. Dennis Saering" hinzufuege
    Dann hat die Pruefung "Informationstechnik" den Pruefer "Prof. Dr. Dennis Saering" eingetragen

  Szenario: Eine Pruefung hat schon einen Pruefer und ich fuege erfolgreich einen Pruefer hinzu.
    Angenommen es existiert eine Pruefungsperiode
    Angenommen die Pruefung "Informationstechnik" hat "Prof. Dr. Dennis Saering" als Pruefer
    Wenn ich der Pruefung "Informationstechnik" den Pruefer "Birger Wolter" hinzufuege
    Dann hat die Pruefung "Informationstechnik" die Pruefer Prof. Dr. Dennis Saering, Birger Wolter

  Szenario: Eine Pruefung hat schon einen Pruefer und ich probiere den gleichen Pruefer hinzu zufuegen.
    Angenommen es existiert eine Pruefungsperiode
    Und die Pruefung "Informationstechnik" hat "Prof. Dr. Dennis Saering" als Pruefer
    Wenn ich der Pruefung "Informationstechnik" den Pruefer "Prof. Dr. Dennis Saering" hinzufuege
    Dann hat die Pruefung "Informationstechnik" nur den Pruefer "Prof. Dr. Dennis Saering" eingetragen

  Szenario: Ein Pruefer wird einer Pruefung in einem Block hinzugefuegt.
    Angenommen es existiert eine Pruefungsperiode
    Und die Pruefung "Informationstechnik" hat keinen Pruefer
    Und die Pruefung "Informationstechnik" ist im Block "tollerBlock"
    Wenn ich der Pruefung "Informationstechnik" den Pruefer "Prof. Dr. Dennis Saering" hinzufuege
    Dann erhalte ich den Block "tollerBlock" zurueck
    Dann hat die Pruefung "Informationstechnik" den Pruefer "Prof. Dr. Dennis Saering" eingetragen

  Szenario: Ein Pruefer soll einer unbekannten Pruefung hinzugefuegt werden.
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert keine Pruefung "Informationstechnik"
    Wenn ich der Pruefung "Informationstechnik" den Pruefer "Prof. Dr. Dennis Saering" hinzufuege
    Dann erhalte ich einen Fehler
