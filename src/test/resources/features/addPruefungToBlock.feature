# language: de

Funktionalität: Als Benutzer moechte ich Pruefungen zu Bloecken hinzufuegen koennen

  Szenario: Ich fuege erfolgreich eine Pruefung zu einem leeren Block hinzu
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der leere Block "block 1"
    Wenn ich die Pruefung "Analysis" zum Block "block 1" hinzufuege
    Dann enthalten die Planungseinheiten, die ich erhalte den Block "block 1"
    Und der Block "block 1" enthaelt Analysis

  Szenario: Ich fuege erfolgreich eine Pruefung zu einem nicht leeren Block hinzu
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der Block "block 1" mit der Pruefung "Analysis"
    Wenn ich die Pruefung "Diskrete Mathematik" zum Block "block 1" hinzufuege
    Dann enthalten die Planungseinheiten, die ich erhalte den Block "block 1"
    Und der Block "block 1" enthaelt Diskrete Mathematik, Analysis

#  Szenario: Ich versuche eine Pruefung zu einem Block hinzuzufuegen aber es gibt keine Pruefungsperiode
#    Angenommen es existiert keine Pruefungsperiode
#    Und es existiert der leere Block "block 1"
#    Wenn ich die Pruefung "Rechnerstrukturen" hinzufuege
#    Dann erhalte ich eine Fehlermeldung
#
#  Szenario: Ich versuche eine unbekannte Pruefung zu einem Block hinzuzufuegen
#    Angenommen es existiert keine Pruefungsperiode
#    Und es existiert der Block "block 1" mit der Pruefung "GdfP"
#    Wenn ich die Pruefung "Rechnerstrukturen" hinzufuege
#    Dann erhalte ich eine Fehlermeldung
#
#  Szenario: Ich versuche eine Pruefung zu einem unbekannten Block hinzuzufuegen
#    Angenommen es existiert keine Pruefungsperiode
#    Und es existiert der Block "block 1" mit der Pruefung "GdfP"
#    Wenn ich die Pruefung "Rechnerstrukturen" hinzufuege
#    Dann erhalte ich eine Fehlermeldung
#
#  Szenario: Ich versuche eine geplante Pruefung zu einem Block hinzuzufuegen
#
#  Szenario: Ich versuche eine Pruefung aus einem Block in einen anderen Block einzuplanen
#
#  Szenario: Ich versuche eine Pruefung aus einem Block in den selben Block einzuplanen
#
#  Szenario: Ich fuege eine Pruefung zu einem geplanten Block hinzu und beeinflusse andere Pruefungen
#
#  Szenario: Ich fuege eine Pruefung zu einem geplanten Block hinzu und beeinflusse andere Pruefungen in Bloecken
#
#  Szenario: Ich fuege eine Pruefung zu einem geplanten Block hinzu und verletze ein hartes Kriterium
