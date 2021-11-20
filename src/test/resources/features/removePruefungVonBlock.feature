# language: de
Funktionalität: Als Planender moechte ich gerne Pruefungen von einem Block entfernen.

  Szenariogrundriss: Es existiert ein geplanter Block und Planer entfernt eine Klausur vom Block
    Angenommen der geplante Block "ABC" hat die Pruefungen <ListeVonPruefungenInnerhalbEinesBlockes>
    Wenn die 0 Pruefung aus dem Block entfernt wird
    Dann ist 0 Pruefung ungeplant
    Dann ist 0 Prufung nicht mehr im Block "ABC"
    Dann ist Block "ABC" immernoch geplant
    Dann hat der Block "ABC" noch 2 Pruefungen
    Dann hat das Entfernen Auswirkungen auf das Scoring
    #TODO bitte hier eine vernuenftige dann Bedingung
    Beispiele:
      | ListeVonPruefungenInnerhalbEinesBlockes |
      | Analysis, Diskrete Mathematik           |
      | Medienrecht, Wirtschaftsprivatrecht     |
      | Deutsch 1, Englisch 2                   |
      #TODO: es ist doch moeglich sein, dass es als String - Liste uebergeben wird -.-

  Szenario: Es existiert ein geplanter Block und Planer entfernt die letzte Klausur
    Angenommen der geplante Block "ABC" hat die Pruefungen "Analysis"
    Wenn die 0 Pruefung aus dem Block entfernt wird
    Dann ist 0 Pruefung ungeplant
    Dann ist Block "ABC" immernoch geplant
    Dann hat der Block "ABC" noch 0 Pruefungen
    Dann hat das Entfernen Auswirkungen auf das Scoring
    #TODO bitte hier eine vernuenftige dann Bedingung

  Szenariogrundriss: Es existiert ein ungeplanter Block und Planer entfernt beliebige Klausur
      Angenommen der ungeplante Block "ABC" hat die Pruefungen <ListeVonPruefungenInnerhalbEinesBlockes>
      Wenn die 0 Pruefung aus dem Block entfernt wird
      Dann ist 0 Pruefung ungeplant
      Dann ist 0 Prufung nicht mehr im Block "ABC"
      Dann ist Block "ABC" immernoch ungeplant
      Dann hat der Block "ABC" noch 2 Pruefungen
    Beispiele:
        | ListeVonPruefungenInnerhalbEinesBlockes |
        | Analysis, Diskrete Mathematik           |
        | Medienrecht, Wirtschaftsprivatrecht     |
        | Deutsch 1, Englisch 2                   |

    Szenario: Es existiert ein ungeplanter Block und Planer entfernt eine Klausur die nicht existiert
      Angenommen der ungeplante Block "ABC" hat die Pruefungen "Analysis, Diskrete Mathematik"
      Wenn die Pruefung "Deutsch 1" aus dem Block "ABC" entfernt wird
      Dann werfe IllegalArgumentException

  Szenario: Es existiert ein geplanter Block und Planer entfernt eine Klausur die nicht existiert
    Angenommen der geplante Block "ABC" hat die Pruefungen "Analysis, Diskrete Mathematik"
    Wenn die Pruefung "Deutsch 1" aus dem Block "ABC" entfernt wird
    Dann werfe IllegalArgumentException
