# language: de
Funktionalität: Als Planender moechter ich zu einer Klausur einen Teilnehmerkreis und die dazugehoerige
  geschaetze Anzahl an Teilnehmer definieren bzw. hinzufügen oder sogar aendern.

  Szenario: Die Klausur hat keinen TeilnehmerKreis + Schaetzung und fuegen neuen hinzu
    Angenommen die Pruefung "Analysis" hat als Teilnehmerkreisschaetzung: ""
    Wenn ich den Studiengang "B_BWL" Fachsemester 1 mit Ordnung "10.0" und 60 schaetze und hinzufuege
    Dann hat die Pruefung "Analysis" die Teilnehmerkreischaetzungen: "B_BWL 1 10.0 50"

  Szenario: Die Klausur hat bereits einen Teilnehmerkreis + Schaetzung fuege neuen hinzu
    Angenommen die Pruefung "Analysis" hat als Teilnehmerkreisschaetzung: "B_BWL 1 10.0 50"
    Wenn ich den Studiengang "B_WING" Fachsemester 1 mit Ordnung "11.0" und 100 schaetze und hinzufuege
    Dann hat die Pruefung "Analysis" die Teilnehmerkreischaetzungen: "B_BWL 1 10.0 50, B_WING 1 11.0 100"

  Szenario: Die Klausur hat bereits einen Teilnehmerkreis + Schaetzung und aendere Teilnehmerkreis Schaetzung
    Angenommen die Pruefung "Analysis" hat als Teilnehmerkreisschaetzung: "B_BWL 1 10.0 50, B_WING 1 11.0 100"
    Wenn ich den Studiengang "B_WING" Fachsemester 1 mit Ordnung "11.0" und 120 schaetze und hinzufuege
    Dann hat die Pruefung "Analysis" die Teilnehmerkreischaetzungen: "B_BWL 1 10.0 50, B_WING 1 11.0 120"

  Szenario: Die Klausur hat bereits einen Teilnehmerkreis + Schaetzung, fuege selben Studiengang aber anderes Semester hinzu
    Angenommen die Pruefung "Analysis" hat als Teilnehmerkreisschaetzung: "B_BWL 1 10.0 50, B_WING 1 11.0 100"
    Wenn ich den Studiengang "B_WING" Fachsemester 2 mit Ordnung "11.0" und 10 schaetze und hinzufuege
    Dann hat die Pruefung "Analysis" die Teilnehmerkreischaetzungen: "B_BWL 1 10.0 50, B_WING 1 11.0 120, B_WING 2 11.0 10"