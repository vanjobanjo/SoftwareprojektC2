# language: de
Funktionalität: Als Planender moechte ich zu einer Klausur einen Teilnehmerkreis und die dazugehoerige
  geschaetze Anzahl an Teilnehmer entfernen

  Szenario: Die Klausur hat keinen TeilnehmerKreis + Schaetzung und entferne einen Teilnehmerkreis
    Angenommen die Pruefung "Analysis" hat als Teilnehmerkreisschaetzung: ""
    Wenn ich den Studiengang "B_BWL" Fachsemester 1 mit Ordnung "10.0" und 60 schaetze und entferne
    Dann werfe IllegalArgumentException

  Szenario: Die Klausur hat bereits einen Teilnehmerkreis + Schaetzung und entferne einen
    Angenommen die Pruefung "Analysis" hat als Teilnehmerkreisschaetzung: "B_BWL 1 10.0 50"
    Wenn ich den Studiengang "B_BWL" Fachsemester 1 mit Ordnung "10.0" und 50 schaetze und entferne
    Dann hat die Pruefung "Analysis" die Teilnehmerkreischaetzungen: ""

  Szenario: Die Klausur hat bereits einen Teilnehmerkreis + Schaetzung, entferne TK mit negative Schaetzzahl. Invalider TK
    Angenommen die Pruefung "Analysis" hat als Teilnehmerkreisschaetzung: "B_BWL 1 10.0 50, B_WING 1 11.0 100"
    Wenn ich den Studiengang "B_WING" Fachsemester 2 mit Ordnung "11.0" und -1 schaetze und entferne
    Dann werfe IllegalArgumentException

  Szenario: Die Klausur hat zugewiesene Teilnehmerkreise + Schaetzung, entferne eine Schaetzung
  das Scoring wird veraendert und eine Liste von Klausuren werden zurueckgegeben
    #TODO: das Scoring muss veraendert werden, wie genau wird noch später definiert
    #TODO: wenn das Scoring definiert ist, werden neue Testfaelle definiert.

  Szenario: Die Klausur hat zugewiesene Teilnehmerkreise + Schaetzung, entferne einen Teilnehmerkreis
    #TODO: das Scoring muss veraendert werden, wie genau wird noch später definiert
    #TODO: wenn das Scoring definiert ist, werden neue Testfaelle definiert.

  Szenario: Der Klausur werden Teilnehmerkreise entfernt, die eigentlich von TK geschrieben werden muss
    #TODO: Was soll hier geschehen? Ist es moeglich, dass der Kunde beliebige TK entfernen kann?

