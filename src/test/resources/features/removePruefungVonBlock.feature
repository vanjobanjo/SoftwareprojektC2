# language: de
Funktionalität: Als Planender moechte ich gerne Pruefungen von einem Block entfernen.

  Szenariogrundriss: Eine Klausur wird aus einem geplanten Block entfernt
    Angenommen die <Klausuren> sind Teil des geplanten Block "ABC"
    Wenn die erste der <Klausuren> aus dem Block entfernt wird
    Dann ist die erste der <Klausuren> ungeplant
    Und die erste der <Klausuren> ist nicht mehr im Block "ABC"
    Und der Block "ABC" ist immernoch geplant
    Und es sind 2 Klausuren im Block "ABC"
    Und das Entfernen hat Auswirkungen auf das Scoring
    #TODO bitte hier eine vernuenftige dann Bedingung
    Beispiele:
      | Klausuren                           |
      | Analysis, Diskrete Mathematik       |
      | Medienrecht, Wirtschaftsprivatrecht |
      | Deutsch 1, Englisch 2               |

  Szenario: Aus einem geplanten Block wird die einzige Klausur entfernt
    Angenommen der geplante Block "ABC" hat die Pruefung "Analysis"
    Wenn "Analysis" aus dem Block entfernt wird
    Dann ist "Analysis" ungeplant
    Und der Block "ABC" ist noch geplant
    Und der Block "ABC" hat keine Pruefungen mehr
    Und das Entfernen hat Auswirkungen auf das Scoring
    #TODO bitte hier eine vernuenftige dann Bedingung

  Szenariogrundriss: Es wird eine Klausur aus einem ungeplanten Block entfernt
    Angenommen die <Klausuren> sind Teil des ungeplanten Block "ABC"
    Wenn die erste der <Klausuren> aus dem Block entfernt wird
    Dann ist die erste der <Klausuren> ungeplant
    Und die erste der <Klausuren> ist nicht mehr im Block "ABC"
    Und der Block "ABC" ist immer noch ungeplant
    Und es sind 2 Klausuren im Block "ABC"
    Und das Entfernen hat Auswirkungen auf das Scoring
    Beispiele:
      | Klausuren                           |
      | Analysis, Diskrete Mathematik       |
      | Medienrecht, Wirtschaftsprivatrecht |
      | Deutsch 1, Englisch 2               |

  Szenario: Es wird eine Klausur aus einem ungeplanten Block entfernt obwohl sie nicht im Block ist
    Angenommen "Analysis", "Diskrete Mathematik" sind Teil des geplanten Block "ABC"
    Wenn die Pruefung "Deutsch 1" aus dem Block "ABC" entfernt wird
    Dann werfe IllegalArgumentException

  Szenario: Es wird eine Klausur aus einem geplanten Block entfernt obwohl sie nicht im Block ist
    Angenommen "Analysis", "Diskrete Mathematik" sind Teil des ungeplanten Block "ABC"
    Wenn die Pruefung "Deutsch 1" aus dem Block "ABC" entfernt wird
    Dann werfe IllegalArgumentException
