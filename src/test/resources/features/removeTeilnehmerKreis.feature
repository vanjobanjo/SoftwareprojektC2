# language: de
Funktionalität: Als Planender moechte ich zu einer Klausur einen Teilnehmerkreis und die dazugehoerige
  geschaetze Anzahl an Teilnehmer entfernen

  Szenario: Die ungeplante Klausur hat einen Teilnehmerkreis und ich als Planer möchte den entfernen
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester und "inf" im 2 Semster
    Wenn ich der Pruefung "Analysis" den Teilnehmerkreis "inf" Fachsemester 1 entfernen moechte
    Dann hat die Pruefung "Analysis" nicht den Teilnehmerkreis "inf" im Fachsemster 1
    Und die Pruefung "Analysis" hat den Teilnehmerkreis "inf" im Fachsemster 2


  Szenario: Die ungeplante  Klausur hat  zwei Teilnehmerkreise und ich als Planer möchte den entfernen
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester und "bwl" im 1 Semster
    Wenn ich der Pruefung "Analysis" den Teilnehmerkreis "inf" Fachsemester 1 entfernen moechte
    Dann hat die Pruefung "Analysis" nicht den Teilnehmerkreis "inf" im Fachsemster 1
    Und die Pruefung "Analysis" hat den Teilnehmerkreis "bwl" im Fachsemster 1

  Szenario: Die geplante Klausur hat einen Teilnehmerkreis und ich als Planer möchte den entfernen
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester am 02.02.2022
    Wenn ich der Pruefung "Analysis" den Teilnehmerkreis "inf" Fachsemester 1 entfernen moechte
    Dann hat die Pruefung "Analysis" nicht den Teilnehmerkreis "inf" im Fachsemster 1

    Szenario: Die ungeplante Klausur hat nicht den Teilnehmerkreis den ich entfernen möchte
      Angenommen es existiert eine Pruefungsperiode
      Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 2 Semester am 02.02.2022
      Wenn ich der Pruefung "Analysis" den Teilnehmerkreis "inf" Fachsemester 1 entfernen moechte
      Dann hat die Pruefung "Analysis" nicht den Teilnehmerkreis "inf" im Fachsemster 1
      Und die Pruefung "Analysis" hat den Teilnehmerkreis "inf" im Fachsemster 2

  Szenario: Weiche Restiction testen, beim removen von Teilnehmerkreis
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 2 Semester am 02.02.2022 um 09:00 Uhr
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "inf" im 2 Semester am 02.02.2022 um 16:00 Uhr
    Wenn ich der Pruefung "Analysis" den Teilnehmerkreis "inf" Fachsemester 2 entfernen moechte
    Dann hat die Pruefung "Analysis" nicht den Teilnehmerkreis "inf" im Fachsemster 2
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "MEHRERE_PRUEFUNGEN_AM_TAG"


  Szenario: Fehlermeldung IllegalStateException verursachen
      Angenommen es existiert eine Pruefungsperiode
      Wenn ich einer Pruefung einen Teilnehmerkreis enfernen möchte
      Dann bekomme ich eine Fehlermeldung IllegalStateException

  Szenario: Bugfix Klausur bei remove teilnehmerkreis nicht dabei
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester und "inf" im 2 Semster
    Wenn ich der Pruefung "Analysis" den Teilnehmerkreis "inf" Fachsemester 1 entfernen moechte bugfix
    Dann hat die Pruefung "Analysis" nicht den Teilnehmerkreis "inf" im Fachsemster 1 bufix
    Und die Pruefung "Analysis" hat den Teilnehmerkreis "inf" im Fachsemster 2 bugfix


