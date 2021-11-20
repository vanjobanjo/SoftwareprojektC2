# language: de
Funktionalität: Als Planender moechte ich gerne Pruefungen von einem Block entfernen.

  Szenariogrundriss: Es existiert ein geplanter Block und Planer entfernt eine Klausur vom Block
    Angenommen der Block geplante "ABC" hat die Pruefungen <ListeVonPruefungenInnerhalbEinesBlockes>
    Wenn die 1 Pruefung aus dem Block entfernt wird
    Dann ist 1 Pruefung ungeplant
    Beispiele:
      | ListeVonPruefungenInnerhalbEinesBlockes |
      | Analysis, Diskrete Mathematik           |
      | Medienrecht, Wirtschaftsprivatrecht     |
      | Deutsch 1, Englisch 2                   |
      #TODO: es ist doch moeglich sein, dass es als String - Liste uebergeben wird -.-
      #TODO: auch noch wichtig zu klaeren: ob ueberhaupt Klausuren aus geplanten Bloecken geloescht werden duerfen.
      #TODO: weil die Loeschung von nur ungeplanten Klausuren erlaubt ist.Szenario.

  Szenario: Es existiert ein geplanter Block und Planer entfernt