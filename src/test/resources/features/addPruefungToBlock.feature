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

  Szenario: Ich versuche eine unbekannte Pruefung zu einem Block hinzuzufuegen
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der Block "block 1" mit der Pruefung "GdfP"
    Wenn ich die unbekannte Pruefung "Rechnerstrukturen" zum Block "block 1" hinzufuege
    Dann erhalte ich einen Fehler

  Szenario: Ich versuche eine Pruefung zu einem unbekannten Block hinzuzufuegen
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich die Pruefung "Rechnerstrukturen" zu einem unbekannten Block "block 42" hinzufuege
    Dann erhalte ich einen Fehler

  Szenario: Ich versuche eine geplante Pruefung zu einem Block hinzuzufügen
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der leere Block "block 1"
    Wenn ich die geplante Pruefung "Analysis" zum Block "block 1" hinzufuege
    Dann erhalte ich einen Fehler

  Szenario: Ich versuche eine Pruefung aus einem Block in einen anderen Block einzuplanen
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der Block "block 1" mit der Pruefung "Analysis"
    Und es existiert der leere Block "block 2"
    Wenn ich die Pruefung "Analysis" zum Block "block 2" hinzufuege
    Dann erhalte ich einen Fehler

  Szenario: Ich versuche eine Pruefung aus einem Block in den selben Block einzuplanen
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der Block "block 1" mit der Pruefung "Analysis"
    Wenn ich die Pruefung "Analysis" zum Block "block 1" hinzufuege
    Dann aendert sich nichts

  Szenario: Ich fuege eine Pruefung zu einem geplanten Block hinzu und beeinflusse andere Pruefungen
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante Block "block 1" mit der Pruefung "Analysis"
    Und die Pruefung "Rechnernetze" ist direkt nach "block 1" geplant
    Und es existiert eine ungeplante Pruefung "Diskrete Mathematik"
    Und "Rechnernetze" und "Diskrete Mathematik" haben einen gemeinsamen Teilnehmerkreis
    Wenn ich die Pruefung "Diskrete Mathematik" zum Block "block 1" hinzufuege
    Dann ist "Rechnernetze" Teil der beeinflussten Planungseinheiten

  Szenario: Ich fuege eine Pruefung zu einem geplanten Block hinzu und beeinflusse andere Pruefungen in Bloecken
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante Block "block 1" mit der Pruefung "Analysis"
    Und es existiert der geplante Block "block 2" mit der Pruefung "Lineare Algebra" direkt nach "block 1"
    Und es existiert eine ungeplante Pruefung "Diskrete Mathematik"
    Und "Lineare Algebra" und "Diskrete Mathematik" haben einen gemeinsamen Teilnehmerkreis
    Wenn ich die Pruefung "Diskrete Mathematik" zum Block "block 1" hinzufuege
    Dann ist "block 2" Teil der beeinflussten Planungseinheiten

  Szenario: Ich fuege eine Pruefung zu einem geplanten Block hinzu und verletze ein hartes Kriterium
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante Block "block 1" mit der Pruefung "Analysis"
    Und es existiert eine ungeplante Pruefung "WebTech"
    Und die Pruefung "Rechnernetze" ist zeitgleich mit "block 1" geplant
    Und "Rechnernetze" und "WebTech" haben einen gemeinsamen Teilnehmerkreis
    Wenn ich die Pruefung "WebTech" zum Block "block 1" hinzufuege
    Dann ist ein hartes Kriterium verletzt
