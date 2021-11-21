# language: de
Funktionalität: Als Planender moechte ich gerne alle Pruefungen von einem Block entfernen.

  Szenariogrundriss: Alle Klausuren werden aus einem geplanten Block entfernt
    Angenommen die <Klausuren> sind Teil des geplanten Block "ABC"
    Wenn alle der <Klausuren> aus dem Block "ABC" entfernt wird
    Dann sind alle <Klausuren> ungeplant
    Und sind alle <Klausuren> ist nicht mehr im Block "ABC"
    Und der Block "ABC" ist noch geplant
    Und es sind 0 Klausuren im Block "ABC"
    Und das Entfernen hat Auswirkungen auf das Scoring
    #TODO bitte hier eine vernuenftige dann Bedingung
    Beispiele:
      | Klausuren                           |
      | Analysis, Diskrete Mathematik       |
      | Medienrecht, Wirtschaftsprivatrecht |
      | Deutsch 1, Englisch 2               |

  Szenariogrundriss: Alle Klausuren werden aus einem ungeplanten Block entfernt
    Angenommen die <Klausuren> sind Teil des ungeplanten Block "ABC"
    Wenn alle der <Klausuren> aus dem Block "ABC" entfernt wird
    Dann sind alle <Klausuren> ungeplant
    Und sind alle <Klausuren> ist nicht mehr im Block "ABC"
    Und der Block "ABC" ist immer noch ungeplant
    Und es sind 0 Klausuren im Block "ABC"
    Und das Entfernen hat keine Auswirkungen auf das Scoring
    Beispiele:
      | Klausuren                           |
      | Analysis, Diskrete Mathematik       |
      | Medienrecht, Wirtschaftsprivatrecht |
      | Deutsch 1, Englisch 2               |