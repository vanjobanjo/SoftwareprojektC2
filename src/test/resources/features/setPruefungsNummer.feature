# language: de
Funktionalität: Als Planender möchte ich die Nummer einer Prüfung ändern können.

  Szenario: Die Nummer einer Prüfung wird erfolgreich geändert
    Angenommen die Prüfung "Analysis" hat die Nummer "b001"
    Wenn ich die Nummer von "Analysis" zu "b123" ändere
    Dann ist die Nummer von "Analysis" "b123"

  Szenario: Die nummer einer unbekannten Prüfung kann nicht geändert werden
    Angenommen es existieren keine Prüfungen
    Wenn ich versuche die Nummer einer Prüfung zu ändern
    Dann bekomme ich eine Fehlermeldung

  Szenario: Es wird keine Prüfung genannt, dessen Nummer geändert werden soll
    Wenn ich keine Prüfung nenne, dessen Nummer ich verändern möchte
    Dann bekomme ich eine Fehlermeldung

    Szenario: Es wird keine neue Prüfungsnummer vergeben
      Wenn ich die Nummer einer Prüfung ändere ohne eine neue Nummer anzugeben
      Dann bekomme ich eine Fehlermeldung

  Szenario: Eine Prüfungsnummer ist bereits vergeben
    Angenommen die Prüfung "Analysis" hat die Nummer "b001"
    Und es existiert eine Prüfung mit der Nummer "b202"
    Wenn ich die Nummer von "Analysis" zu "b202" ändere
    Dann bekomme ich eine Fehlermeldung
    Und die Nummer von "Analysis" ist immernoch "b001"
