# language: de
Funktionalität: Als Planer möchte ich Informationen der Prüfungsperiode abfragen und ändern können.


  Szenario: Es gibt kein geplantes Semester
    Angenommen Es ist kein Semester geplant
    Wenn ich das Startdatum der Periode anfrage
    Dann Erhalte ich einen Fehler


  Szenario: Es gibt ein geplantes Semester
    Angenommen Es ist ein Semester geplant
    Wenn ich das Startdatum der Periode anfrage
    Dann erhalte ich das Startdatum


  Szenariogrundriss: Es gibt ein geplantes Semester
    Angenommen Es ist ein Semester geplant
    Wenn ich das <Startdatum> und das <Enddatum> der Periode aendere
    Dann werden die Daten entsprechend geaendert
    Beispiele:
      | Startdatum | Enddatum |
      | "1.1."     | "2.2."   |
      | "12.3."    | "12.4."  |
      | "1.12."    | "20.1."  |


# debug nicht überdacht, nur für merge show Zwecke
  Szenario: Es gibt ein geplantes Semester
    Angenommen es existieren folgende Teilnehmerkreise:
      | Name        |
      | b_inf20.0_1 |
      | b_inf20.0_2 |
      | b_inf20.0_3 |
      | b_inf20.0_4 |

    Wenn ich alle Teilnehmerkreise anfrage
    Dann bekomme ich die Teilnehmerkreise "b_inf20.0_1, b_inf20.0_2 ,b_inf20.0_3, b_inf20.0_4"