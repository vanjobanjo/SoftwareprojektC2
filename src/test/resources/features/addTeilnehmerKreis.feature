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

  Szenario: Die Klausur hat bereits einen Teilnehmerkreis + Schaetzung fuege den selben TK hinzu
    Angenommen die Pruefung "Analysis" hat als Teilnehmerkreisschaetzung: "B_BWL 1 10.0 50"
    Wenn ich den Studiengang "B_BWL" Fachsemester 1 mit Ordnung "10.0" und 50 schaetze und hinzufuege
    Dann hat die Pruefung "Analysis" die Teilnehmerkreischaetzungen: "B_BWL 1 10.0 50"


  Szenario: Die Klausur hat bereits einen Teilnehmerkreis + Schaetzung, fuege negative Schaetzzahl hinzu. Invalider TK
    Angenommen die Pruefung "Analysis" hat als Teilnehmerkreisschaetzung: "B_BWL 1 10.0 50, B_WING 1 11.0 100"
    Wenn ich den Studiengang "B_WING" Fachsemester 2 mit Ordnung "11.0" und -1 schaetze und hinzufuege
    Dann erhalte ich eine Fehlermeldung

  Szenario: Die Klausur hat zugewiesene Teilnehmerkreise + Schaetzung, aendere Schaetzung nach oben hin
            das Scoring wird veraendert und eine Liste von Klausuren werden zurueckgegeben
    #TODO: das Scoring muss veraendert werden, wie genau wird noch später definiert
    #TODO: wenn das Scoring definiert ist, werden neue Testfaelle definiert.

  Szenario: Die Klausur hat zugewiesene Teilnehmerkreise + Schaetzung, entferne einen Teilnehmerkreis
    #TODO: das Scoring muss veraendert werden, wie genau wird noch später definiert
    #TODO: wenn das Scoring definiert ist, werden neue Testfaelle definiert.

  Szenario: Der Klausur werden Teilnehmerkreise hinzugefuegt, die eigentlich nicht vom TK geschrieben wird
    #TODO: Was soll hier geschehen? Ist es moeglich, dass der Kunde beliebige TK hinzufuegen kann?

