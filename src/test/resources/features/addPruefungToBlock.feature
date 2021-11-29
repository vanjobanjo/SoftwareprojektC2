# language: de

Funktionalität: Als Benutzer moechte ich Pruefungen zu Bloecken hinzufuegen koennen

  Szenario: Ich fuege erfolgreich eine Pruefung zu einem leeren Block hinzu
    Angenommen es existiert der leere Block "block 1"
    Wenn ich die Pruefung "Analysis" hinzufuege
    Dann erhalte ich einen Block der die Pruefung "Analysis" enthaelt


  Szenario: Ich fuege erfolgreich eine Pruefung zu einem nicht leeren Block hinzu
    Angenommen es existiert der Block "block 1" mit der Pruefung "Analysis"
    Wenn ich die Pruefung "Diskrete Mathematik" hinzufuege
    Dann erhalte ich einen Block mit den Pruefungen "Diskrete Mathematik" und "Analysis"

  Szenario: Ich versuche eine Pruefung zu einem Block hinzuzufuegen, aber ein hartes Kriterium wird verletzt
    Angenommen es gibt am selben Tag einen geplanten Block "block 1" und die geplante Pruefung "Informationstechnik"
    Wenn ich die Pruefung "Digitaltechnik" zum Block "block 1" hinzufuege
    Dann erhalte ich eine Fehlermeldung


  Szenario: Ich versuche eine Pruefung zu einem Block hinzuzufuegen aber es gibt keine Pruefungsperiode
    Angenommen es existiert keine Pruefungsperiode
    Und es existiert der leere Block "block 1"
    Wenn ich die Pruefung "Rechnerstrukturen" hinzufuege
    Dann erhalte ich eine Fehlermeldung

  Szenario: Ich versuche eine Pruefung zu einem Block hinzuzufuegen aber es gibt keine Pruefungsperiode
    Angenommen es existiert keine Pruefungsperiode
    Und es existiert der Block "block 1" mit der Pruefung "GdfP"
    Wenn ich die Pruefung "Rechnerstrukturen" hinzufuege
    Dann erhalte ich eine Fehlermeldung


