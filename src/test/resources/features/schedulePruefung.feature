# language: de
Funktionalität: Als Planende*r moechte ich eine Pruefung einplanen koennen.


  Szenario: Ich plane erfolgreich eine Pruefung ein
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis"
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 eingeplant

  Szenario: Ich plane erfolgreich eine Pruefung ein
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 eingeplant

  Szenario: Ich plane erfolgreich eine Pruefung ein
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster
    Und es existiert die geplante Pruefung "BWL" mit den Teilnehmerkreis "bwl" im 1 semster am 14.02.2022
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 eingeplant


  Szenario: Ich plane unerfolgreich eine Pruefung ein da schon eine Pruefung mit gleichen Teilnehmerkreis liegt
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster
    Und es existiert die geplante Pruefung "BWL" mit den Teilnehmerkreis "inf" im 1 semster am 14.02.2022
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann bekomme ich eine Fehlermeldung HartesKriteriumException

  Szenario: Ich plane unerfolgreich eine Pruefung ein da schon eine Pruefung mit gleichen Teilnehmerkreis liegt
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster am 15.02.2022
    Und es existiert die geplante Pruefung "BWL" mit den Teilnehmerkreis "inf" im 1 semster am 14.02.2022
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann bekomme ich eine Fehlermeldung HartesKriteriumException
    Und ist die Pruefung "Analysis" am 15.02.2022 eingeplant

  Szenario: Ich plane erfolgreich eine Pruefung ein
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster am 15.02.2022
    Und es existiert die geplante Pruefung "BWL" mit den Teilnehmerkreis "bwl" im 1 semster am 14.02.2022
    Wenn ich "Analysis" am 14.02.2022 einplanen moechte
    Dann ist die Pruefung "BWL" am 14.02.2022 eingeplant
    Und ist die Pruefung "Analysis" am 14.02.2022 eingeplant

  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium MehrePruefung an einem Tag
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster
    Und es existiert die geplante Pruefung "BWL" mit den Teilnehmerkreis "inf" im 1 semster am 14.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 14.02.2022 um 16 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 um 16 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat das WeicheKriterium "MEHRERE_PRUEFUNGEN_AM_TAG"
    Und die Pruefung "BWL" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Analysis" hat das WeicheKriterium "MEHRERE_PRUEFUNGEN_AM_TAG"
    Und die Pruefung "Analysis" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"


  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium FreierTagZwischen
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster
    Und es existiert die geplante Pruefung "BWL" mit den Teilnehmerkreis "inf" im 1 semster am 14.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 15.02.2022 um 16 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 15.02.2022 um 16 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"
    Und die Pruefung "Analysis" hat das WeicheKriterium "FREIER_TAG_ZWISCHEN_PRUEFUNGEN"


  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium SOnntag
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis"
    Wenn ich "Analysis" am 13.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 13.02.2022 eingeplant
    Und die Pruefung "Analysis" hat das WeicheKriterium "SONNTAG"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium SOnntag
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis"
    Wenn ich "Analysis" am 13.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 13.02.2022 eingeplant
    Und die Pruefung "Analysis" hat das WeicheKriterium "SONNTAG"


  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium MehrePruefung an einem Tag
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster
    Und es existiert die geplante Pruefung "BWL" mit den Teilnehmerkreis "inf" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Haskel" mit den Teilnehmerkreis "inf" im 1 semster am 16.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Diskrete Mathematik" mit den Teilnehmerkreis "inf" im 1 semster am 15.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Datenbanken" mit den Teilnehmerkreis "inf" im 1 semster am 17.02.2022 um 8 Uhr
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
    Und  es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster
    Wenn ich "Analysis" am 26.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 26.02.2022 eingeplant
    Und die Pruefung "Analysis" hat das WeicheKriterium "WOCHE_VIER_FUER_MASTER"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium WOCHE_VIER_FUER_MASTER
    Angenommen es existiert eine Pruefungsperiode von 24.01.2022 - 27.02.2022
    Und  es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster
    Wenn ich "Analysis" am 19.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 19.02.2022 eingeplant
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "WOCHE_VIER_FUER_MASTER"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeichesKriterium WOCHE_VIER_FUER_MASTER
    Angenommen es existiert eine Pruefungsperiode von 24.01.2022 - 27.02.2022
    Und  es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster "MASTER" und 100 Teilnehmer
    Wenn ich "Analysis" am 26.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 26.02.2022 eingeplant
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "WOCHE_VIER_FUER_MASTER"

    # WOCHE_VIER_FUER_MASTER

  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH 6 gleichzeitig löst nicht aus
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster
    Und es existiert die geplante Pruefung "BWL" mit den Teilnehmerkreis "bwl" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Haskel" mit den Teilnehmerkreis "tinf" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Diskrete Mathematik" mit den Teilnehmerkreis "minf" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Datenbanken" mit den Teilnehmerkreis "winf" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Klausurzuviel" mit den Teilnehmerkreis "Zinf" im 1 semster am 14.02.2022 um 8 Uhr
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
    Und es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster
    Und es existiert die geplante Pruefung "BWL" mit den Teilnehmerkreis "bwl" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Haskel" mit den Teilnehmerkreis "tinf" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Diskrete Mathematik" mit den Teilnehmerkreis "minf" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Datenbanken" mit den Teilnehmerkreis "winf" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Klausurzuviel" mit den Teilnehmerkreis "Zinf" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Klausurzuviel1" mit den Teilnehmerkreis "ZZinf" im 1 semster am 14.02.2022 um 8 Uhr
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
    Und es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster
    Und es existiert die geplante Pruefung "BWL" mit den Teilnehmerkreis "bwl" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Haskel" mit den Teilnehmerkreis "tinf" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Diskrete Mathematik" mit den Teilnehmerkreis "minf" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Datenbanken" mit den Teilnehmerkreis "winf" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Klausurzuviel" mit den Teilnehmerkreis "Zinf" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Klausurzuviel1" mit den Teilnehmerkreis "ZZinf" im 1 semster am 14.02.2022 um 8 Uhr
    Und es existiert die geplante Pruefung "Klausurzuviel2" mit den Teilnehmerkreis "ZZZinf" im 1 semster am 14.02.2022 um 8 Uhr
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
    Und es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster "BACHELOR" und 100 Teilnehmer
    Und es existiert die geplante Pruefung "BWL" mit den Teilnehmerkreis "bwl" im 1 semster und 100 Teilnehmer am 14.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 14.02.2022 um 8 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat nicht das WeicheKriterium "ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Analysis" hat nicht das WeicheKriterium "ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH"

  Szenario: Ich plane erfolgreich eine Pruefung ein WeicheKriterium ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH 201 gleichzeitig löst nicht aus
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster "BACHELOR" und 100 Teilnehmer
    Und es existiert die geplante Pruefung "BWL" mit den Teilnehmerkreis "bwl" im 1 semster und 101 Teilnehmer am 14.02.2022 um 8 Uhr
    Wenn ich "Analysis" am 14.02.2022 um 8 Uhr einplanen moechte
    Dann ist die Pruefung "Analysis" am 14.02.2022 um 8 Uhr eingeplant
    Und ist die Pruefung "BWL" am 14.02.2022 um 8 Uhr eingeplant
    Und die Pruefung "BWL" hat das WeicheKriterium "ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH"
    Und die Pruefung "Analysis" hat das WeicheKriterium "ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH"




    # UNIFORME_ZEITSLOTS
    # PRUEFUNGEN_MIT_VIELEN_AN_ANFANG
