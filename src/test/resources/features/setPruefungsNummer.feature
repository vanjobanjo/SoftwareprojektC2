# language: de
Funktionalität: Als Planender möchte ich die Nummer einer Pruefung ändern können.

  Szenario: Die Nummer einer Pruefung wird erfolgreich geändert
    Angenommen es existiert eine Pruefungsperiode
    Angenommen die Pruefung "Analysis" hat die Nummer "b001"
    Wenn ich die Nummer von "Analysis" zu "b123" ändere
    Dann ist die Nummer von "Analysis" "b123"

  Szenario: Die nummer einer unbekannten Pruefung kann nicht geändert werden
    Angenommen es existiert eine Pruefungsperiode
    Angenommen es existieren keine Pruefungen
    Wenn ich versuche die Nummer einer Pruefung zu ändern
    Dann erhalte ich einen Fehler

  Szenario: Es wird keine Pruefung genannt, dessen Nummer geändert werden soll
    Angenommen es existiert eine Pruefungsperiode
    Angenommen die Pruefung "Analysis" hat die Nummer "b001"
    Wenn ich keine Pruefung nenne, dessen Nummer ich verändern möchte
    Dann erhalte ich einen Fehler

  Szenario: Es wird keine neue Pruefungsnummer vergeben
    Angenommen es existiert eine Pruefungsperiode
    Angenommen die Pruefung "Analysis" hat die Nummer "b001"
    Wenn ich die Nummer der Pruefung "Analysis" aendere ohne eine neue Nummer anzugeben
    Dann erhalte ich einen Fehler

  Szenario: Eine Pruefungsnummer ist bereits vergeben
    Angenommen es existiert eine Pruefungsperiode
    Angenommen die Pruefung "Analysis" hat die Nummer "b001"
    Und es existiert eine Pruefung mit der Nummer "b202"
    Wenn ich die Nummer von "Analysis" zu "b202" ändere
    Dann erhalte ich einen Fehler
    Und die Nummer von "Analysis" ist immer noch "b001"


  Szenario: Die Nummer einer Pruefung innerhalb eines Blocks wird erfolgreich geändert
    Angenommen es existiert eine Pruefungsperiode
    Angenommen die Pruefung "Analysis" hat die Nummer "b001" und ist im Block "block 1"
    Wenn ich die Nummer von "Analysis" zu "b123" ändere
    Dann ist die Nummer von "Analysis" "b123"


  Szenario: Die Nummer einer sich nicht in einem Block befindlichen Pruefung wird veraendert
    Angenommen es existiert eine Pruefungsperiode
    Angenommen die Pruefung "Analysis" hat die Nummer "b001"
    Wenn ich die Nummer von "Analysis" zu "b123" ändere
    Dann erhalte ich eine Pruefung


  Szenario: Die Nummer einer sich in einem Block befindlichen Pruefung wird veraendert
    Angenommen es existiert eine Pruefungsperiode
    Angenommen die Pruefung "Analysis" hat die Nummer "b001" und ist im Block "block 1"
    Wenn ich die Nummer von "Analysis" zu "b123" ändere
    Dann erhalte ich einen Block "block 1" der die Pruefung "Analysis" mit Nummer "b123" enthaelt
