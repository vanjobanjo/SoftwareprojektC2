#language: de
Funktionalität: Als Planer moechte ich Zugriff auf alle geplanten Bloecke haben

  Szenario: Es ist eine Pruefungsperiode vorhanden und ich moechte alle geplanten Bloecke anfragen
    Angenommen es ist eine Pruefungsperiode geplant
    Und es gibt die folgenden geplanten Bloecke:
      | Block     |
      | "block 1" |
      | "block 2" |
      | "block 3" |
      | "block 4" |
      | "block 5" |
    Wenn ich alle geplanten Bloecke anfrage
    Dann erhalte ich die Bloecke "block 1, block 2, block 3, block 4, block 5"


  Szenario: Es es ist eine Pruefungsperiode geplant und ich moechte alle geplanten Bloecke anfragen
    Angenommen es ist eine Pruefungsperiode geplant
    Und es gibt die folgenden geplanten und ungeplanten Bloecke:
      | BlockGeplant | BlockUngeplant |
      | "block 3"    | "block 1"      |
      | "block 4"    | "block 49"     |
    Wenn ich alle geplanten Bloecke anfrage
    Dann erhalte ich die Bloecke "block 3, block 4"


  Szenario: Es ist eine Pruefungsperiode geplant und ich moechte alle geplanten Bloecke anfragen
    Angenommen es ist eine Pruefungsperiode geplant
    Und es gibt keine geplanten Bloecke
    Wenn ich alle geplanten Bloecke anfrage
    Dann erhalte ich keine Bloecke


  Szenario: Es ist keine Pruefungsperiode geplant und ich moechte alle geplanten Bloecke anfragen
    Angenommen es ist keine Pruefungsperiode geplant
    Wenn ich alle geplanten Bloecke anfrage
    Dann erhalte ich einen Fehler
