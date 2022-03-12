# language: de
Funktionalität: Als Planender moechte ich gerne Pruefungen von einem Block entfernen.


  Szenario: Aus einem ungeplante Block wird eine Klausur entfernt
    Angenommen es existiert eine Pruefungsperiode
    Und der ungeplante Block "blockA" "PARALLEL" enthaelt "BWL", "Mathe1"
    Wenn "BWL" aus dem Block "blockA" entfernt wird
    Dann ist die Pruefung "BWL" ungeplant
    Und der Block "blockA" enthaelt die Pruefung "BWL" nicht
    Und der Block "blockA" enthaelt die Pruefung "Mathe1"

  Szenario: Aus einem ungeplante Block wird eine Klausur entfernt
    Angenommen es existiert eine Pruefungsperiode
    Und der ungeplante Block "blockA" "SEQUENTIAL" enthaelt "BWL", "Mathe1"
    Wenn "BWL" aus dem Block "blockA" entfernt wird
    Dann ist die Pruefung "BWL" ungeplant
    Und der Block "blockA" enthaelt die Pruefung "BWL" nicht
    Und der Block "blockA" enthaelt die Pruefung "Mathe1"


  Szenario: Aus einem ungeplante Block wird die einzige Klausur entfernt
    Angenommen es existiert eine Pruefungsperiode
    Und der ungeplante Block "blockA" "PARALLEL" enthaelt "BWL"
    Wenn "BWL" aus dem Block "blockA" entfernt wird
    Dann ist die Pruefung "BWL" ungeplant
    Und der Block "blockA" enthaelt die Pruefung "BWL" nicht

  Szenario: Aus einem ungeplante Block wird die einzige Klausur entfernt
    Angenommen es existiert eine Pruefungsperiode
    Und der ungeplante Block "blockA" "SEQUENTIAL" enthaelt "BWL"
    Wenn "BWL" aus dem Block "blockA" entfernt wird
    Dann ist die Pruefung "BWL" ungeplant
    Und der Block "blockA" enthaelt die Pruefung "BWL" nicht


  Szenario: Aus einem ungeplante Block wird die einzige Klausur entfernt
    Angenommen es existiert eine Pruefungsperiode
    Und der geplante Block "blockA" "PARALLEL" 02.02.2022 enthaelt "BWL", "Mathe1"
    Wenn "BWL" aus dem Block "blockA" entfernt wird
    Dann ist die Pruefung "BWL" ungeplant
    Und der Block "blockA" enthaelt die Pruefung "BWL" nicht
    Und der Block "blockA" enthaelt die Pruefung "Mathe1"
    Und ist die Pruefung "Mathe1" geplant

  Szenario: Aus einem ungeplante Block wird die einzige Klausur entfernt
    Angenommen es existiert eine Pruefungsperiode
    Und der geplante Block "blockA" "SEQUENTIAL" 02.02.2022 enthaelt "BWL", "Mathe1"
    Wenn "BWL" aus dem Block "blockA" entfernt wird
    Dann ist die Pruefung "BWL" ungeplant
    Und der Block "blockA" enthaelt die Pruefung "BWL" nicht
    Und der Block "blockA" enthaelt die Pruefung "Mathe1"
    Und ist die Pruefung "Mathe1" geplant

  Szenario: Aus einem ungeplante Block wird eine Klausur entfernt mit mehrerenBlocken
    Angenommen es existiert eine Pruefungsperiode
    Und der ungeplante Block "blockA" "PARALLEL" enthaelt "BWL", "Mathe1"
    Und der ungeplante Block "blockB" "PARALLEL" enthaelt "BWL1", "Mathe11"
    Wenn "BWL" aus dem Block "blockA" entfernt wird
    Dann ist die Pruefung "BWL" ungeplant
    Und der Block "blockA" enthaelt die Pruefung "BWL" nicht
    Und der Block "blockA" enthaelt die Pruefung "Mathe1"

  Szenario: Aus einem ungeplante Block wird eine Klausur entfernt mehrerenBlocken
    Angenommen es existiert eine Pruefungsperiode
    Und der ungeplante Block "blockA" "SEQUENTIAL" enthaelt "BWL", "Mathe1"
    Und der ungeplante Block "blockB" "SEQUENTIAL" enthaelt "BWL1", "Mathe11"
    Wenn "BWL" aus dem Block "blockA" entfernt wird
    Dann ist die Pruefung "BWL" ungeplant
    Und der Block "blockA" enthaelt die Pruefung "BWL" nicht
    Und der Block "blockA" enthaelt die Pruefung "Mathe1"


  Szenario: remove Pruefung from Block with fehlermeldung
    Angenommen es existiert eine Pruefungsperiode
    Und der ungeplante Block "blockA" "SEQUENTIAL" enthaelt "BWL", "Mathe1"
    Wenn wenn ich eine Unbekannte Pruefung aus einen Block "blockA" entfernen moechte
    Dann bekomme ich eine Fehlermeldung IllegalStateException



