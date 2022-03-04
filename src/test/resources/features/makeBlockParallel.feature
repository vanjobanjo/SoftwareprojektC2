# language: de
Funktionalität: Als Planende*r moechte ich sequentielle Bloecke parallel machen koennen

#  ist schon parallel
#  beeinflusste Pruefungen durch besseres scoring
#  beeinflusste Bloecke durch besseres scoring
#  unbekannte Pruefung

  Szenario: Ein umgestellter Block ist hinterher vom Typ parallel
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert der geplante sequentielle Block "Bloeckchen" am 03.02.2022 um 15:00 Uhr mit den Pruefungen
      | Pruefung |
      | Analysis |
      | DM       |
    Wenn ich den Block "Bloeckchen" auf parallel stelle
    Dann ist der Block "Bloeckchen" parallel

