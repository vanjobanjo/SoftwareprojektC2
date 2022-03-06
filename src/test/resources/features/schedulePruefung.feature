# language: de
Funktionalität: Als Planende*r moechte ich eine Pruefung einplanen koennen.


  Szenario: Ich plane erfolgreich eine Pruefung ein
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis"
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 eingeplant

  Szenario: Ich plane erfolgreich eine Pruefung ein
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 eingeplant

  Szenario: Ich plane erfolgreich eine Pruefung ein
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "bwl" im 1 Semester am 14.02.2022
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 eingeplant


  Szenario: Ich plane unerfolgreich eine Pruefung ein da schon eine Pruefung mit gleichen Teilnehmerkreis liegt
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "inf" im 1 Semester am 14.02.2022
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann bekomme ich eine Fehlermeldung HartesKriteriumException

  Szenario: Ich plane unerfolgreich eine Pruefung ein da schon eine Pruefung mit gleichen Teilnehmerkreis liegt
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester am 15.02.2022
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "inf" im 1 Semester am 14.02.2022
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann bekomme ich eine Fehlermeldung HartesKriteriumException
    Und ist die Pruefung "Analysis" am 15.02.2022 eingeplant

  Szenario: Ich plane erfolgreich eine Pruefung ein
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester am 15.02.2022
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "bwl" im 1 Semester am 14.02.2022
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "BWL" am 14.02.2022 eingeplant
    Und ist die Pruefung "Analysis" am 14.02.2022 eingeplant

  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium MehrePruefung an einem Tag
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "inf" im 1 Semester am 14.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 14.02.2022 um 16 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 um 16 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat das WeicheKriterium "MEHRERE_PRUEFUNGEN_AM_TAG"
    Und die Pruefung "BWL" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Analysis" hat das WeicheKriterium "MEHRERE_PRUEFUNGEN_AM_TAG"
    Und die Pruefung "Analysis" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"


  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium FreierTagZwischen
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "inf" im 1 Semester am 14.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 15.02.2022 um 16 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 15.02.2022 um 16 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Analysis" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"


  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium Sonntag
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis"
    Wenn ich "Analysis" am 13.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 13.02.2022 eingeplant
    Und die Pruefung "Analysis" hat das WeicheKriterium "SONNTAG"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium Sonntag
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis"
    Wenn ich "Analysis" am 13.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 13.02.2022 eingeplant
    Und die Pruefung "Analysis" hat das WeicheKriterium "SONNTAG"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium Sonntag löst nicht aus
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis"
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 eingeplant
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "SONNTAG"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium Sonntag löst nicht aus
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis"
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 eingeplant
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "SONNTAG"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium Sonntag löst nicht aus
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis"
    Wenn ich "Analysis" am 12.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 12.02.2022 eingeplant
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "SONNTAG"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium Sonntag löst nicht aus
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis"
    Wenn ich "Analysis" am 12.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 12.02.2022 eingeplant
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "SONNTAG"


  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium MehrePruefung an einem Tag
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "inf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Haskel" mit dem Teilnehmerkreis "inf" im 1 Semester am 16.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Diskrete Mathematik" mit dem Teilnehmerkreis "inf" im 1 Semester am 15.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Datenbanken" mit dem Teilnehmerkreis "inf" im 1 Semester am 17.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 18.02.2022 um 8 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 18.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Haskel" am 16.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_PRO_WOCHE"
    Und die Pruefung "BWL" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Analysis" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_PRO_WOCHE"
    Und die Pruefung "Analysis" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Haskel" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_PRO_WOCHE"
    Und die Pruefung "Haskel" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Diskrete Mathematik" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_PRO_WOCHE"
    Und die Pruefung "Diskrete Mathematik" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Datenbanken" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_PRO_WOCHE"
    Und die Pruefung "Datenbanken" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium WOCHE_VIER_FUER_MASTER
    Angenommen es existiert eine Pruefungsperiode von 24.01.2022 - 27.02.2022
    Und  es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester
    Wenn ich "Analysis" am 26.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 26.02.2022 eingeplant
    Und die Pruefung "Analysis" hat das WeicheKriterium "WOCHE_VIER_FUER_MASTER"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium WOCHE_VIER_FUER_MASTER nicht
    Angenommen es existiert eine Pruefungsperiode von 24.01.2022 - 27.02.2022
    Und  es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester
    Wenn ich "Analysis" am 19.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 19.02.2022 eingeplant
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "WOCHE_VIER_FUER_MASTER"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium WOCHE_VIER_FUER_MASTER nicht
    Angenommen es existiert eine Pruefungsperiode von 24.01.2022 - 27.02.2022
    Und  es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester "MASTER" und 100 Teilnehmer
    Wenn ich "Analysis" am 26.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 26.02.2022 eingeplant
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "WOCHE_VIER_FUER_MASTER"

    # WOCHE_VIER_FUER_MASTER

  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH 6 gleichzeitig löst nicht aus
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "bwl" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Haskel" mit dem Teilnehmerkreis "tinf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Diskrete Mathematik" mit dem Teilnehmerkreis "minf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Datenbanken" mit dem Teilnehmerkreis "winf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Klausurzuviel" mit dem Teilnehmerkreis "Zinf" im 1 Semester am 14.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 14.02.2022 um 8 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Haskel" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Diskrete Mathematik" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Datenbanken" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Klausurzuviel" am 14.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat nicht das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Haskel" hat nicht das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Diskrete Mathematik" hat nicht das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Datenbanken" hat nicht das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Klausurzuviel" hat nicht das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH 7 gleichzeitig
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "bwl" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Haskel" mit dem Teilnehmerkreis "tinf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Diskrete Mathematik" mit dem Teilnehmerkreis "minf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Datenbanken" mit dem Teilnehmerkreis "winf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Klausurzuviel" mit dem Teilnehmerkreis "Zinf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Klausurzuviel1" mit dem Teilnehmerkreis "ZZinf" im 1 Semester am 14.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 14.02.2022 um 8 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Haskel" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Diskrete Mathematik" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Datenbanken" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Klausurzuviel" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Klausurzuviel1" am 14.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Analysis" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Haskel" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Diskrete Mathematik" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Datenbanken" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Klausurzuviel" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Klausurzuviel1" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH 8 gleichzeitig
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "bwl" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Haskel" mit dem Teilnehmerkreis "tinf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Diskrete Mathematik" mit dem Teilnehmerkreis "minf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Datenbanken" mit dem Teilnehmerkreis "winf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Klausurzuviel" mit dem Teilnehmerkreis "Zinf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Klausurzuviel1" mit dem Teilnehmerkreis "ZZinf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Klausurzuviel2" mit dem Teilnehmerkreis "ZZZinf" im 1 Semester am 14.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 14.02.2022 um 8 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Haskel" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Diskrete Mathematik" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Datenbanken" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Klausurzuviel" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Klausurzuviel1" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Klausurzuviel2" am 14.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Analysis" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Haskel" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Diskrete Mathematik" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Datenbanken" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Klausurzuviel" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Klausurzuviel1" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Klausurzuviel2" hat das WeicheKriterium "ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH"


    # ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH

  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH 200 gleichzeitig löst nicht aus
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester "BACHELOR" und 100 Teilnehmer
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "bwl" im 1 Semester und 100 Teilnehmer am 14.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 14.02.2022 um 8 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat nicht das WeicheKriterium "ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH 201 gleichzeitig löst aus
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester "BACHELOR" und 100 Teilnehmer
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "bwl" im 1 Semester und 101 Teilnehmer am 14.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 14.02.2022 um 8 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat das WeicheKriterium "ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Analysis" hat das WeicheKriterium "ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH"





    # PRUEFUNGEN_MIT_VIELEN_AN_ANFANG



  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium PRUEFUNGEN_MIT_VIELEN_AN_ANFANG 100 gleichzeitig löst aus
    Angenommen es existiert eine Pruefungsperiode von 31.01.2022 - 28.02.2022
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester "BACHELOR" und 100 Teilnehmer
    Wenn ich "Analysis" am 07.02.2022 um 8 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 07.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "Analysis" hat das WeicheKriterium "PRUEFUNGEN_MIT_VIELEN_AN_ANFANG"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium PRUEFUNGEN_MIT_VIELEN_AN_ANFANG 100 gleichzeitig löst nicht aus
    Angenommen es existiert eine Pruefungsperiode von 31.01.2022 - 28.02.2022
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester "BACHELOR" und 99 Teilnehmer
    Wenn ich "Analysis" am 07.02.2022 um 8 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 07.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "PRUEFUNGEN_MIT_VIELEN_AN_ANFANG"


  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium PRUEFUNGEN_MIT_VIELEN_AN_ANFANG 100 gleichzeitig löst aus
    Angenommen es existiert eine Pruefungsperiode von 31.01.2022 - 28.02.2022
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester "BACHELOR" und 100 Teilnehmer
    Wenn ich "Analysis" am 06.02.2022 um 8 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 06.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "PRUEFUNGEN_MIT_VIELEN_AN_ANFANG"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium PRUEFUNGEN_MIT_VIELEN_AN_ANFANG 100 gleichzeitig löst nicht  aus
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester "BACHELOR" und 99 Teilnehmer
    Wenn ich "Analysis" am 06.02.2022 um 8 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 06.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "PRUEFUNGEN_MIT_VIELEN_AN_ANFANG"



  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium UNIFORME_ZEITSLOTS nicht
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester und Dauer von 60 Minuten
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "bwl" im 1 Semester und Dauer von 60 Minuten am 14.02.2022
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 eingeplant
    Und ist die Dauer der Pruefung "Analysis" 60 Minuten
    Und ist die Dauer der Pruefung "BWL" 60 Minuten
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "UNIFORME_ZEITSLOTS"
    Und die Pruefung "BWL" hat nicht das WeicheKriterium "UNIFORME_ZEITSLOTS"


  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium UNIFORME_ZEITSLOTS
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester und Dauer von 60 Minuten
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "bwl" im 1 Semester und Dauer von 120 Minuten am 14.02.2022
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 eingeplant
    Und ist die Dauer der Pruefung "Analysis" 60 Minuten
    Und ist die Dauer der Pruefung "BWL" 120 Minuten
    Und die Pruefung "Analysis" hat das WeicheKriterium "UNIFORME_ZEITSLOTS"
    Und die Pruefung "BWL" hat das WeicheKriterium "UNIFORME_ZEITSLOTS"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium UNIFORME_ZEITSLOTS
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester und Dauer von 60 Minuten
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "bwl" im 1 Semester und Dauer von 61 Minuten am 14.02.2022
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 eingeplant
    Und ist die Dauer der Pruefung "Analysis" 60 Minuten
    Und ist die Dauer der Pruefung "BWL" 61 Minuten
    Und die Pruefung "Analysis" hat das WeicheKriterium "UNIFORME_ZEITSLOTS"
    Und die Pruefung "BWL" hat das WeicheKriterium "UNIFORME_ZEITSLOTS"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium UNIFORME_ZEITSLOTS
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester und Dauer von 60 Minuten
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "bwl" im 1 Semester und Dauer von 59 Minuten am 14.02.2022
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 eingeplant
    Und ist die Dauer der Pruefung "Analysis" 60 Minuten
    Und ist die Dauer der Pruefung "BWL" 59 Minuten
    Und die Pruefung "Analysis" hat das WeicheKriterium "UNIFORME_ZEITSLOTS"
    Und die Pruefung "BWL" hat das WeicheKriterium "UNIFORME_ZEITSLOTS"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium UNIFORME_ZEITSLOTS
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester und Dauer von 61 Minuten
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "bwl" im 1 Semester und Dauer von 59 Minuten am 14.02.2022
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 eingeplant
    Und ist die Dauer der Pruefung "Analysis" 61 Minuten
    Und ist die Dauer der Pruefung "BWL" 59 Minuten
    Und die Pruefung "Analysis" hat das WeicheKriterium "UNIFORME_ZEITSLOTS"
    Und die Pruefung "BWL" hat das WeicheKriterium "UNIFORME_ZEITSLOTS"




     # UNIFORME_ZEITSLOTS










  #Massentest

  Szenario: Ich plane erfoglreich eine Pruefung ein, MassenTest
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" und "bwl" im 1 Semester
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "inf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Haskel" mit dem Teilnehmerkreis "inf" im 1 Semester am 15.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Diskrete Mathematik" mit dem Teilnehmerkreis "inf" im 1 Semester am 16.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Datenbanken" mit dem Teilnehmerkreis "inf" im 1 Semester am 17.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "infKlausur1" mit dem Teilnehmerkreis "inf" im 1 Semester am 18.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "infKlausur2" mit dem Teilnehmerkreis "inf" im 1 Semester am 18.02.2022 um 16 Uhr
    Und es existiert die geplante Pruefung "infKlausur3" mit dem Teilnehmerkreis "inf" im 1 Semester am 20.02.2022 um 8 Uhr

    Und es existiert die geplante Pruefung "BWLbwl" mit dem Teilnehmerkreis "bwl" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Haskelbwl" mit dem Teilnehmerkreis "bwl" im 1 Semester am 15.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Diskrete Mathematikbwl" mit dem Teilnehmerkreis "bwl" im 1 Semester am 16.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Datenbankenbwl" mit dem Teilnehmerkreis "bwl" im 1 Semester am 17.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "infKlausur1bwl" mit dem Teilnehmerkreis "bwl" im 1 Semester am 18.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "infKlausur2bwl" mit dem Teilnehmerkreis "bwl" im 1 Semester am 18.02.2022 um 16 Uhr
    Und es existiert die geplante Pruefung "infKlausur3bwl" mit dem Teilnehmerkreis "bwl" im 1 Semester am 20.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 14.02.2022 um 16 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 um 16 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Haskel" am 15.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Diskrete Mathematik" am 16.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Datenbanken" am 17.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "infKlausur1" am 18.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "infKlausur2" am 18.02.2022 um 16 Uhr eingeplant
    Und ist die Pruefung "infKlausur3" am 20.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "BWLbwl" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Haskelbwl" am 15.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Diskrete Mathematikbwl" am 16.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Datenbankenbwl" am 17.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "infKlausur1bwl" am 18.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "infKlausur2bwl" am 18.02.2022 um 16 Uhr eingeplant
    Und ist die Pruefung "infKlausur3bwl" am 20.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat das WeicheKriterium "MEHRERE_PRUEFUNGEN_AM_TAG"
    Und die Pruefung "Analysis" hat das WeicheKriterium "MEHRERE_PRUEFUNGEN_AM_TAG"
    Und die Pruefung "infKlausur3" hat das WeicheKriterium "SONNTAG"
    Und die Pruefung "infKlausur2" hat das WeicheKriterium "MEHRERE_PRUEFUNGEN_AM_TAG"
    Und die Pruefung "infKlausur1" hat das WeicheKriterium "MEHRERE_PRUEFUNGEN_AM_TAG"
    Und die Pruefung "Analysis" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "BWL" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Haskel" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Diskrete Mathematik" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Datenbanken" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "infKlausur1" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "infKlausur2" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
  #bwl
    Und die Pruefung "BWLbwl" hat das WeicheKriterium "MEHRERE_PRUEFUNGEN_AM_TAG"
    Und die Pruefung "Analysis" hat das WeicheKriterium "MEHRERE_PRUEFUNGEN_AM_TAG"
    Und die Pruefung "infKlausur3bwl" hat das WeicheKriterium "SONNTAG"
    Und die Pruefung "infKlausur2bwl" hat das WeicheKriterium "MEHRERE_PRUEFUNGEN_AM_TAG"
    Und die Pruefung "infKlausur1bwl" hat das WeicheKriterium "MEHRERE_PRUEFUNGEN_AM_TAG"
    Und die Pruefung "Analysis" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "BWLbwl" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Haskelbwl" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Diskrete Mathematikbwl" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Datenbankenbwl" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "infKlausur1bwl" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "infKlausur2bwl" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"




  Szenario: Bugg fix beim umplanen von klausuren klappt nicht mit diesen Test, muss weiter getestet werden und weitere test geschrieben werden
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester am 15.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "inf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Haskel" mit dem Teilnehmerkreis "inf" im 1 Semester am 16.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 18.02.2022 um 8 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 18.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Haskel" am 16.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat nicht das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Haskel" hat nicht das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"




  Szenario: Bugg fix beim umplanen von klausuren klappt nicht mit diesen Test,
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester am 15.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "BWL" mit dem Teilnehmerkreis "inf" im 1 Semester am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Haskel" mit dem Teilnehmerkreis "inf" im 1 Semester am 16.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 18.02.2022 um 08:00 Uhr einplanen moechte bugtest
    Dann ist die Pruefung "Analysis" am 18.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "Haskel" am 16.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat nicht das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN" bugix
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN" bugix
    Und die Pruefung "Haskel" hat nicht das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN" bugix
