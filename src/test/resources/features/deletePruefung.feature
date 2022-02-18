# language: de
Funktionalität: Als Planender moechte ich eine neue Pruefung löschen koennen.

  Szenario: Eine ungeplante Pruefung kann geloescht werden.
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert eine ungeplante Pruefung "ComputerGrafik"
    Wenn ich die Pruefung "ComputerGrafik" loesche
    Dann existiert die Pruefung "ComputerGrafik" nicht mehr

  Szenario: Eine geplante Pruefung kann nicht geloescht werden.
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "ComputerGrafik"
    Wenn ich die Pruefung "ComputerGrafik" loesche
    Dann erhalte ich einen Fehler

  Szenario: Das Loeschen einer Pruefung liefert keine Rueckgabe wenn die Pruefung in keinem Block ist.
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert eine ungeplante Pruefung "ComputerGrafik"
    Wenn ich die Pruefung "ComputerGrafik" loesche
    Dann ist das Ergebnis leer

  Szenario: Das Loeschen einer Pruefung liefert den Block in der sie sich befand.
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der Block "block 1" mit der Pruefung "ComputerGrafik"
    Wenn ich die Pruefung "ComputerGrafik" loesche
    Dann enthaelt das Ergebnis den Block "block 1"

  Szenario: Eine unbekannte Pruefung kann nicht geloescht werden.
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich die unbekannte Pruefung "ComputerGrafik" loesche
    Dann erhalte ich einen Fehler
