# language: de
Funktionalität: Als Planende*r moechte ich eine Pruefung einplanen koennen.


  Szenario: Ich plane erfolgreich eine Pruefung ein
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis"
    Wenn ich "Analysis" am 13.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 13.02.2022 eingeplant

  Szenario: Ich plane erfolgreich eine Pruefung ein
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster
    Wenn ich "Analysis" am 13.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 13.02.2022 eingeplant

  Szenario: Ich plane erfolgreich eine Pruefung ein
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster
    Und es existiert die geplante Pruefung "BWL" mit den Teilnehmerkreis "bwl" im 1 semster am 13.02.2022
    Wenn ich "Analysis" am 13.02.2022 einplanen moechte
    Dann ist die Pruefung "Analysis" am 13.02.2022 eingeplant


  Szenario: Ich plane unerfolgreich eine Pruefung ein
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit den Teilnehmerkreis "inf" im 1 semster
    Und es existiert die geplante Pruefung "BWL" mit den Teilnehmerkreis "inf" im 1 semster am 13.02.2022
    Wenn ich "Analysis" am 13.02.2022 einplanen moechte
    Dann bekomme ich eine Fehlermeldung HartesKriteriumException