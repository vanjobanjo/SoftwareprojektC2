# language: de
Funktionalität: Als Planender moechte ich zu einer Pruefung abfragen können, wie dessen Scoring zustande kommt.

  Szenario: Ich frage die Analyse einer Klausur ab, die gegen keine Restriktion verlaetzt.
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert eine ungeplante Pruefung "Lineare Algebra"
    Wenn ich die Analyse zu "Lineare Algebra" anfrage
    Dann sind keine Kriterien verletzt

  Szenario: Ich frage die Analyse einer Klausur ab, mit zu vielen Anderen Klausuren gleichzeitig stattfindet.
    Angenommen es existiert eine Pruefungsperiode
    Und es sind mehr Klausuren gleichzeitig geplant als erlaubt
    Wenn ich die Analyse zu einer der geplanten Klausuren abfrage
    Dann ist das Kriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH" verlaetzt

  Szenario: Ich frage die Analyse einer Klausur ab, die mit zwei verschiedenen Kriterien gleichzeitig in Konflikt steht.
    Angenommen es existiert eine Pruefungsperiode
    Und es sind mehr Klausuren gleichzeitig geplant als erlaubt
    Und eine weitere Klausur mit anderer Laenge ist zum selben Zeitpunkt geplant
    Wenn ich die Analyse zu einer der geplanten Klausuren abfrage
    Dann ist das Kriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH" verlaetzt
    Dann ist das Kriterium "UNIFORME_ZEITSLOTS" verlaetzt

  Szenario: Ich frage die Analyse einer Klausur ab, die nicht existiert.
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich die Analyse zu einer unbekannten Klausur abfrage
    Dann erhalte ich einen Fehler
