#language: de
Funktionalität: Als Planende*r moechte einen HartenKonflikt richtig angezeigt bekommen

  Szenario: Ich als Planer moechte bei einen HartenKonfikt eine richtige Exception bekommen
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Analysis" mit den Teilnehmerkreisen am 02.02.2022 um 08:00 Uhr
      | Name | Semster | Anzahl |
      | inf  | 1       | 33     |
      | bwl  | 1       | 13     |
    Und es existiert die ungeplante Pruefung "Andere" mit den Teilnehmerkreisen
      | Name | Semster | Anzahl |
      | inf  | 1       | 20     |
      | bwl  | 1       | 15     |
    Wenn ich die Pruefung "Andere" am 02.02.2022 um 08:00 Uhr einplanen moechte
    Dann habe ich eine HartesKriteriumException mit den Teilnehmerkreisen
      | Name | Semster | Anzahl |
      | inf  | 1       | 33     |
      | bwl  | 1       | 15     |

