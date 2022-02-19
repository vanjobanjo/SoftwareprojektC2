# language: de
Funktionalität: Als Planender moechte ich einen Block loeschen koennen.

  Szenario: Ein ungeplanter Block kann geloescht werden
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der leere Block "Block"
    Wenn ich den Block "Block" loesche
    Dann erhalte ich keine Pruefungen

  Szenario: Ein geplanter Block kann nicht geloescht werden
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante Block "Block"
    Wenn ich den Block "Block" loesche
    Dann erhalte ich einen Fehler

  Szenario: Das Loeschen eines geplanten Blocks liefert dessen Pruefungen
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der Block "Block" mit der Pruefung "Pruefung"
    Wenn ich den Block "Block" loesche
    Dann enthaelt das Ergebnis genau die Pruefung "Pruefung"

  Szenario: Der Versuch einen unbekannten Block zu loeschen verursacht einen Fehler
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich den unbekannten Block "Block" loesche
    Dann erhalte ich einen Fehler

  Szenario: Der Versuch einen Block mit einr unbekannten Pruefung zu loeschen verursacht einen Fehler
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der leere Block "Block"
    Wenn ich den Block "Block" mit einer unbekannten Pruefung loesche
    Dann erhalte ich einen Fehler
