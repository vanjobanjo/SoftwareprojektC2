# language: de
Funktionalität: Als Planender moechte ich zu einer Klausur einen Teilnehmerkreis hinzufuegen koennen.

  Szenario: Ich fuege einen ersten Teilnehmerkreis zu einer Pruefung hinzu.
    Angenommen es existiert eine Pruefungsperiode
    Und die Pruefung "Analysis" hat keinen Teilnehmerkreis
    Wenn ich den Teilnehmerkreis "BWL 1 10.0" zu "Analysis" hinzufuege
    Dann ist "Analysis" Teil der beeinflussten Planungseinheiten
    Und "Analysis" hat den Teilnehmerkreis "BWL 1 10.0"

  Szenario: Ich fuege einen Teilnehmerkreis zu einer Pruefung hinzu, die bereits einen Teilnehmerkreis hat.
    Angenommen es existiert eine Pruefungsperiode
    Und die Pruefung "Analysis" hat einen Teilnehmerkreis "INF 3 14.0"
    Wenn ich den Teilnehmerkreis "BWL 1 10.0" zu "Analysis" hinzufuege
    Dann ist "Analysis" Teil der beeinflussten Planungseinheiten
    Und "Analysis" hat den Teilnehmerkreis "BWL 1 10.0"
    Und "Analysis" hat den Teilnehmerkreis "INF 3 14.0"

  Szenario: Ich fuege einen Teilnehmerkreis zu einer Pruefung hinzu, obwohl der Teilnehmerkreis schon zu ihr zugeordnet ist.
    Angenommen es existiert eine Pruefungsperiode
    Und die Pruefung "Analysis" hat einen Teilnehmerkreis "INF 3 14.0" mit 10 Studenten
    Wenn ich den Teilnehmerkreis "INF 3 14.0" mit 5 Studenten zu "Analysis" hinzufuege
    Dann ist "Analysis" Teil der beeinflussten Planungseinheiten
    Und "Analysis" hat den Teilnehmerkreis "INF 3 14.0" mit 5 Studenten

  Szenario: Ich fuege einen Teilnehmerkreis zu einer Pruefung hinzu und beeinflusse damit andere Pruefungen und Bloecke.
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante Block "block 1" mit der Pruefung "Analysis"
    Und die Pruefung "Analysis" hat einen Teilnehmerkreis "INF 3 14.0" mit 10 Studenten
    Und die Pruefung "Rechnernetze" ist direkt nach "block 1" geplant
    Wenn ich den Teilnehmerkreis "INF 3 14.0" mit 5 Studenten zu "Rechnernetze" hinzufuege
    Dann ist "Analysis" Teil der beeinflussten Planungseinheiten
    Dann ist "block 1" Teil der beeinflussten Planungseinheiten

  Szenario: Ich fuege einen Teilnehmerkreis zu einer Pruefung in einem Block hinzu.
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante Block "block 1" mit der Pruefung "Analysis"
    Wenn ich den Teilnehmerkreis "INF 3 14.0" mit 5 Studenten zu "Analysis" hinzufuege
    Dann ist "Analysis" Teil der beeinflussten Planungseinheiten
    Dann ist "block 1" Teil der beeinflussten Planungseinheiten

  Szenario: Ich fuege einen Teilnehmerkreis ohne Teilnehmer zu einer Pruefung hinzu.
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert eine ungeplante Pruefung "Analysis"
    Wenn ich den Teilnehmerkreis "INF 3 14.0" mit 0 Studenten zu "Analysis" hinzufuege
    Dann ist "Analysis" Teil der beeinflussten Planungseinheiten
    Und "Analysis" hat den Teilnehmerkreis "INF 3 14.0" mit 0 Studenten

  Szenario: Ich fuege einen Teilnehmerkreis mit negativer Teilnehmerzahl zu einer Pruefung hinzu.
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert eine ungeplante Pruefung "Analysis"
    Wenn ich den Teilnehmerkreis "INF 3 14.0" mit -1 Studenten zu "Analysis" hinzufuege
    Dann erhalte ich einen Fehler

  Szenario: Ich fuege einen Teilnehmerkreis zu einer unbekannten Pruefung hinzu.
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich den Teilnehmerkreis "BWL 1 10.0" zu einer unbekannten Pruefung "Analysis" hinzufuege
    Dann erhalte ich einen Fehler

  Szenario: Ich fuege einen Teilnehmerkreis zu einer Pruefung hinzu und verletze eine harte Restriktion
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante Block "block 1" mit der Pruefung "Analysis"
    Und die Pruefung "Analysis" hat einen Teilnehmerkreis "INF 3 14.0" mit 10 Studenten
    Und die Pruefung "Rechnernetze" ist zeitgleich mit "block 1" geplant
    Wenn ich den Teilnehmerkreis "INF 3 14.0" mit 5 Studenten zu "Rechnernetze" hinzufuege
    Dann ist ein hartes Kriterium verletzt
