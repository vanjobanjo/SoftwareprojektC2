# language: de
Funktionalität: Als Planender moechte ich zu einer Pruefung abfragen koennen in welchem Block sie liegt.

  Szenario: Die Abfrage einer Klausur aus einem Block liefert diesen Block.
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der Block "Blocky" mit der Pruefung "Physik"
    Wenn ich den Block zu der Pruefung "Physik" abfrage
    Dann enthaelt das Ergebnis den Block "Blocky"

  Szenario: Die Abfrage einer Klausur ohne Block liefert kein Ergebnis.
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die Pruefung "Physik"
    Wenn ich den Block zu der Pruefung "Physik" abfrage
    Dann ist das Ergebnis leer

  Szenario: Die Abfrage einer unbekannten Klausur fuehrt zu einem Fehler.
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich den Block zu der unbekannten Pruefung "Physik" abfrage
    Dann erhalte ich einen Fehler
