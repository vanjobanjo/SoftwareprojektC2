# language: de
Funktionalität: Als Planer moechte ich Informationen der Pruefungsperiode abfragen und aendern koennen.


    ################################################################
    #####################  Start- und Enddatum  ####################
    ################################################################

  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte das Startdatum der Periode wissen
    Angenommen es ist keine Pruefungsperiode geplant
    Wenn ich das Startdatum der Periode anfrage
    Dann erhalte ich einen Fehler


  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte das Startdatum der Periode wissen
    Angenommen es ist eine Pruefungsperiode geplant
    Wenn ich das Startdatum der Periode anfrage
    Dann erhalte ich das Startdatum


  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte das Enddatum der Periode wissen
    Angenommen es ist keine Pruefungsperiode geplant
    Wenn ich das Enddatum der Periode anfrage
    Dann erhalte ich einen Fehler


  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte das Enddatum der Periode wissen
    Angenommen es ist eine Pruefungsperiode geplant
    Wenn ich das Enddatum der Periode anfrage
    Dann erhalte ich das Enddatum


  Szenariogrundriss: Es gibt keine geplante Pruefungsperiode und ich moechte das Start und Enddatum aendern
    Angenommen es ist keine Pruefungsperiode geplant
    Wenn ich das <Startdatum> und das <Enddatum> der Periode aendere
    Dann erhalte ich einen Fehler
    Beispiele:
      | Startdatum | Enddatum   |
      | 01.01.2021 | 02.02.2021 |
      | 12.03.2021 | 12.04.2021 |
      | 01.12.2021 | 20.01.2021 |

  Szenariogrundriss: Es gibt eine geplante Pruefungsperiode und ich moechte das Start und Enddatum aendern
    Angenommen es ist eine Pruefungsperiode geplant
    Wenn ich das <Startdatum> und das <Enddatum> der Periode aendere
    Dann werden die Daten entsprechend geaendert
    Beispiele:
      | Startdatum  | Enddatum   |
      | 01.01.2021  | 02.02.2021 |
      | 12.03.2021  | 12.04.2021 |
      | 01.12.2021" | 20.01.2021 |

    ################################################################
    #########################  Kapazitaet  #########################
    ################################################################

  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte die Gesamtkapazitaet wissen
    Angenommen es ist keine Pruefungsperiode geplant
    Wenn ich die Kapazitaet anfrage
    Dann erhalte ich einen Fehler

  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte die Gesamtkapazitaet wissen
    Angenommen es ist eine Pruefungsperiode geplant
    Wenn ich die Kapazitaet anfrage
    Dann erhalte ich die Kapazitaet


  Szenariogrundriss: Es gibt keine geplante Pruefungsperiode und ich moechte die Gesamtkapazitaet aendern
    Angenommen es ist keine Pruefungsperiode geplant
    Wenn ich die <Kapazitaet> aendere
    Dann erhalte ich einen Fehler
    Beispiele:
      | Kapazitaet |
      | 0          |
      | 100        |
      | 3453       |
      | 3          |
      | 40         |

  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte die Kapazitaet auf einen negativen
  Wert aendern
    Angenommen es ist eine Pruefungsperiode geplant
    Wenn ich die Kapazitaet auf den Wert -1 setze
    Dann erhalte ich einen Fehler

  Szenario: Es gibt eine geplante Pruefungsperiode sowie geplante Klausuren und ich moechte die Kapazitaet auf 0 aendern
    Angenommen es ist eine Pruefungsperiode geplant und es sind Pruefungen geplant
    Wenn ich die Kapazitaet auf den Wert 0 setze
    Dann erhalte ich eine Liste mit Klausuren deren Bewertung veraendert wurde


  Szenariogrundriss: Es gibt eine geplante Pruefungsperiode sowie Klausuren und ich moechte die Kapazitaet aendern
    Angenommen es ist eine Pruefungsperiode geplant und es sind Pruefungen geplant
    Wenn ich die Kapazitaet auf den Wert <Kapazitaet> setze
    Dann erhalte ich eine Liste mit Klausuren deren Bewertung veraendert wurde
    Beispiele:
      | Kapazitaet |
      | 0          |
      | 100        |
      | 3453       |
      | 3          |
      | 40         |

  Szenariogrundriss: Es gibt eine geplante Pruefungsperiode aber keine geplanten Klausuren und
  ich moechte die Kapazitaet aendern
    Angenommen es ist eine Pruefungsperiode geplant und es sind keine Pruefungen geplant
    Wenn ich die Kapazitaet auf den Wert <Kapazitaet> setze
    Dann erhalte ich eine Liste ohne Klausuren
    Beispiele:
      | Kapazitaet |
      | 0          |
      | 1200       |
      | 2222       |
      | 34         |
      | 423        |

    ################################################################
    ###########################  Semester  #########################
    ################################################################

  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte das Semester wissen
    Angenommen es ist keine Pruefungsperiode geplant
    Wenn ich das Semester abfrage
    Dann erhalte ich einen Fehler

  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte das Semester wissen
    Angenommen es ist eine Pruefungsperiode geplant
    Wenn ich das Semester abfrage
    Dann erhalte ich das Semester

  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte das Semester aendern
    Angenommen es ist keine Pruefungsperiode geplant
    Wenn ich das Semester auf 2021 "Sommersemester" aendere
    Dann erhalte ich einen Fehler


  Szenariogrundriss: Es gibt eine geplante Pruefungsperiode und ich moechte das Semester aendern
    Angenommen es ist eine Pruefungsperiode geplant
    Wenn ich das Semester von <AltesJahr> <AlterSemestertyp> auf <NeuesJahr> <NeuerSemestertyp> aendere
    Dann ist am Ende das Semester <NeuesJahr> <NeuerSemestertyp> eingetragen
    Beispiele:
      | AltesJahr | AlterSemestertyp | NeuesJahr | NeuerSemestertyp |
      | 2020      | "Wintersemester" | 2020      | "Sommersemester" |
      | 2019      | "Wintersemester" | 2020      | "Sommersemester" |
      | 2022      | "Sommersemester" | 2021      | "Wintersemester" |
      | 2021      | "Sommersemester" | 2021      | "Wintersemester" |
      | 2020      | "Wintersemester" | 2021      | "Wintersemester" |



    ################################################################
    #####################  Teilnehmerkreise  #######################
    ################################################################


  Szenario: Es gibt keine geplante Pruefungsperiode und ich moechte alle Teilnehmerkreise abfragen
    Angenommen es ist keine Pruefungsperiode geplant
    Wenn ich alle Teilnehmerkreise anfrage
    Dann erhalte ich einen Fehler

  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte alle Teilnehmerkreise abfragen
    Angenommen es existieren die Teilnehmerkreise B_INF 1 20.0 40, b_inf 2 20.0 40, b_inf 3 20.0 50, b_inf 4 20.0 13
    Wenn ich alle Teilnehmerkreise anfrage
    Dann bekomme ich die Teilnehmerkreise "b_inf 1 20.0 40, b_inf 2 20.0 40 ,b_inf 3 20.0 50, b_inf 4 20.0 13"


  Szenario: Es gibt eine geplante Pruefungsperiode und ich moechte alle Teilnehmerkreise abfragen
    Angenommen es ist eine Pruefungsperiode geplant und es gibt keine Teilnehmerkreise
    Wenn ich alle Teilnehmerkreise anfrage
    Dann bekomme ich eine leere Liste