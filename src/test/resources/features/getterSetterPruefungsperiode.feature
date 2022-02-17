# language: de
Funktionalität: Als Planer moechte ich Informationen der Pruefungsperiode abfragen und aendern koennen.


    ################################################################
    #####################  Start- und Enddatum  ####################
    ################################################################

  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte das Startdatum der Periode wissen
    Angenommen es existiert keine Pruefungsperiode
    Wenn ich das Startdatum der Periode anfrage
    Dann erhalte ich einen Fehler


  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte das Startdatum der Periode wissen
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich das Startdatum der Periode anfrage
    Dann erhalte ich das Startdatum


  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte das Enddatum der Periode wissen
    Angenommen es existiert keine Pruefungsperiode
    Wenn ich das Enddatum der Periode anfrage
    Dann erhalte ich einen Fehler


  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte das Enddatum der Periode wissen
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich das Enddatum der Periode anfrage
    Dann erhalte ich das Enddatum


  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte das Start und Enddatum aendern
    Angenommen es existiert keine Pruefungsperiode
    Wenn ich das Startdatum auf 01.12.2022 und das Enddatum auf 20.01.2023 setze
    Dann erhalte ich einen Fehler

  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte das Start und Enddatum aendern
    Angenommen es existiert eine Pruefungsperiode von 01.10.2021 - 02.02.2022 mit dem Ankertag 02.12.2021
    Wenn ich das Startdatum auf 01.12.2021 und das Enddatum auf 20.01.2022 setze
    Dann werden die Daten auf 01.12.2021 und 20.01.2022 geaendert

    ################################################################
    ##########################  Ankertag  ##########################
    ################################################################

  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte den Ankertag der Periode wissen
    Angenommen es existiert keine Pruefungsperiode
    Wenn ich den Ankertag der Periode anfrage
    Dann erhalte ich einen Fehler


  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte  den Ankertag der Periode wissen
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich den Ankertag der Periode anfrage
    Dann erhalte ich den Ankertag

  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte den Ankertag aendern
    Angenommen es existiert keine Pruefungsperiode
    Wenn ich den Ankertag auf 01.12.2022 setze
    Dann erhalte ich einen Fehler

  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte den Ankertag aendern
    Angenommen es existiert eine Pruefungsperiode von 01.10.2021 - 02.02.2022 mit dem Ankertag 02.10.2021
    Wenn ich den Ankertag auf 03.01.2022 setze
    Dann wird der Ankertag auf 03.01.2022 geaendert

  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte den Ankertag auf nach dem Ende der Pruefungsperiode aendern
    Angenommen es existiert eine Pruefungsperiode von 01.10.2021 - 02.02.2022
    Wenn ich den Ankertag auf 03.01.2023 setze
    Dann erhalte ich einen Fehler

  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte den Ankertag auf vor dem Anfang der Pruefungsperiode aendern
    Angenommen es existiert eine Pruefungsperiode von 01.10.2021 - 02.02.2022
    Wenn ich den Ankertag auf 03.01.2020 setze
    Dann erhalte ich einen Fehler

    ################################################################
    #########################  Kapazitaet  #########################
    ################################################################

  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte die Gesamtkapazitaet wissen
    Angenommen es existiert keine Pruefungsperiode
    Wenn ich die Kapazitaet anfrage
    Dann erhalte ich einen Fehler

  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte die Gesamtkapazitaet wissen
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich die Kapazitaet anfrage
    Dann erhalte ich die Kapazitaet


  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte die Gesamtkapazitaet aendern
    Angenommen es existiert keine Pruefungsperiode
    Wenn ich die Kapazitaet auf den Wert 100 setze
    Dann erhalte ich einen Fehler

  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte die Kapazitaet auf einen negativen
  Wert aendern
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich die Kapazitaet auf den Wert -1 setze
    Dann erhalte ich einen Fehler

  Szenario: Es gibt eine geplante Pruefungsperiode sowie geplante Klausuren und ich moechte die Kapazitaet auf 0 aendern
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich die Kapazitaet auf den Wert 0 setze
    Dann erhalte ich einen Fehler


  Szenario: Es gibt eine geplante Pruefungsperiode sowie Klausuren und ich moechte die Kapazitaet aendern
    Angenommen es existiert eine Pruefungsperiode
    Und es sind Pruefungen geplant
    Wenn ich die Kapazitaet auf den Wert 10 setze
    Dann erhalte ich eine Liste mit Klausuren deren Bewertung veraendert wurde

  Szenario: Es gibt eine geplante Pruefungsperiode aber keine geplanten Klausuren und
  ich moechte die Kapazitaet aendern
    Angenommen es existiert eine Pruefungsperiode
    Und es sind keine Pruefungen geplant
    Wenn ich die Kapazitaet auf den Wert 140 setze
    Dann erhalte ich eine Liste ohne Klausuren

    ################################################################
    ###########################  Semester  #########################
    ################################################################

  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte das Semester wissen
    Angenommen es existiert keine Pruefungsperiode
    Wenn ich das Semester abfrage
    Dann erhalte ich einen Fehler

  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte das Semester wissen
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich das Semester abfrage
    Dann erhalte ich das Semester

  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte das Semester aendern
    Angenommen es existiert keine Pruefungsperiode
    Wenn ich ein neues Semester mit dem Jahr 2021 und dem Typ Sommersemester erstelle
    Dann erhalte ich das Semester Sommersemester 2021


  Szenariogrundriss: Es gibt eine geplante Pruefungsperiode und ich moechte das Semester aendern
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich ein neues Semester mit dem Jahr <Jahr> und dem Typ <Semestertyp> erstelle
    Dann erhalte ich das Semester <Semestertyp> <Jahr>
    Beispiele:
      | Jahr | Semestertyp    |
      | 2020 | Sommersemester |
      | 2020 | Sommersemester |
      | 2021 | Wintersemester |
      | 2021 | Wintersemester |
      | 2021 | Wintersemester |

    ################################################################
    #####################  Teilnehmerkreise  #######################
    ################################################################


  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte alle Teilnehmerkreise abfragen
    Angenommen es existiert keine Pruefungsperiode
    Wenn ich alle Teilnehmerkreise anfrage
    Dann erhalte ich einen Fehler

  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte alle Teilnehmerkreise abfragen
    Angenommen es existiert eine Pruefungsperiode
    Und es existieren die Teilnehmerkreise B_INF 1 20.0 40, b_inf 2 20.0 40, b_inf 3 20.0 50, b_inf 4 20.0 13
    Wenn ich alle Teilnehmerkreise anfrage
    Dann bekomme ich die Teilnehmerkreise B_INF 1 20.0 40, b_inf 2 20.0 40, b_inf 3 20.0 50, b_inf 4 20.0 13


  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte alle Teilnehmerkreise abfragen
    Angenommen es existiert eine Pruefungsperiode
    Und es gibt keine Teilnehmerkreise
    Wenn ich alle Teilnehmerkreise anfrage
    Dann bekomme ich eine leere Liste
