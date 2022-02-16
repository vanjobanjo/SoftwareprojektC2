# language: de
Funktionalität: Als Planender moechte ich den Termin von einer Pruefung aendern koennen. Und an einem Tag kann nur eine Klausur geschrieben werden.

  Szenariogrundriss: Der Termin einer Pruefung wird erfolgreich geaendert, da er innerhalb der Pruefungperiode liegt
    Angenommen es existiert eine Pruefungsperiode von 04.08.2021 - 10.08.2021
    Und die Pruefung "Analysis" hat den Termin <aktueller Termin> und die Pruefungsperiode von 04.08.2022 - 10.08.2021 und es gibt noch keine Pruefungen
    Wenn ich den Termin von "Analysis" auf den <neuer Termin> aendere
    Dann ist der Termin von "Analysis" am <dann Termin>
    Beispiele:
      | aktueller Termin | neuer Termin | dann Termin |
      | 05.08.2021       | 06.08.2021   | 06.08.2021  |
      | 05.08.2021       | 04.08.2021   | 04.08.2021  |
      | 05.08.2021       | 10.08.2021   | 10.08.2021  |
      |                  | 06.08.2021   | 06.08.2021  |
      |                  | 04.08.2021   | 04.08.2021  |
      |                  | 10.08.2021   | 10.08.2021  |
      | 05.08.2021       | 04.08.2021   | 04.08.2021  |

  Szenariogrundriss: Der Termin einer Pruefung wird nicht erfolgreich geaendert, da er ausserhalb der Pruefungperiode liegt
    Angenommen es existiert eine Pruefungsperiode von 04.08.2021 - 10.08.2021
    Und  die Pruefung "Analysis" hat den Termin <aktueller Termin> und die Pruefungsperiode von 04.08.2021 - 10.08.2021 und es gibt noch keine Pruefungen
    Wenn ich den Termin von "Analysis" auf den <neuer Termin> aendere
    Dann ist der Termin von "Analysis" <dann Termin> und bekommt eine Fehlermeldung
    Beispiele:
      | aktueller Termin | neuer Termin | dann Termin |
      | 05.08.2021       | 03.08.2021   | 05.08.2021  |
      | 05.08.2021       | 11.08.2021   | 05.08.2021  |
      | 05.08.2021       | 06.08.2022   | 05.08.2021  |
      |                  | 03.08.2021   | 05.08.2021  |
      |                  | 03.08.2021   |             |
      |                  | 11.08.2021   |             |

  Szenariogrundriss: Der Termin einer Pruefung wird erfolgreich geaendert, da er innerhalb der Pruefungperiode liegt
    Angenommen es existiert eine Pruefungsperiode von 04.08.2021 - 10.08.2021
    Und  die Pruefung "Analysis" hat den Termin <aktueller Termin> und die Pruefungsperiode von 04.08.2021 - 10.08.2021 und es gibt schon Pruefungen
      | Pruefung            | Termin     |
      | Analysis            | 05.08.2021 |
      | Informationstechnik | 06.08.2021 |
    Wenn ich den Termin von "Analysis" auf den <neuer Termin> aendere
    Dann ist der Termin von "Analysis" am <dann Termin>
    Beispiele:
      | aktueller Termin | neuer Termin | dann Termin |
      | 05.08.2021       | 07.08.2021   | 07.08.2021  |
      | 05.08.2021       | 04.08.2021   | 04.08.2021  |
      | 05.08.2021       | 10.08.2021   | 10.08.2021  |
      |                  | 07.08.2021   | 07.08.2021  |
      |                  | 04.08.2021   | 04.08.2021  |
      |                  | 10.08.2021   | 10.08.2021  |

  Szenariogrundriss: Der Termin einer Pruefung wird nicht erfolgreich geaendert, da sie probiert wird auf ein Termin zu schieben, der schon belegt ist.
    Angenommen es existiert eine Pruefungsperiode von 04.08.2021 - 10.08.2021
    Und die Pruefung "Analysis" hat den Termin <aktueller Termin> und die Pruefungsperiode von 04.08.2021 - 10.08.2021 und es gibt schon Pruefungen
      | Pruefung            | Termin     |
      | Analysis            | 05.08.2021 |
      | Informationstechnik | 06.08.2021 |
    Wenn ich den Termin von "Analysis" auf den <neuer Termin> aendere
    Dann ist der Termin von "Analysis" <dann Termin> und bekommt eine HarteKriterums Fehlermeldung
    Beispiele:
      | aktueller Termin | neuer Termin | dann Termin |
      | 05.08.2021       | 06.08.2021   | 05.08.2021  |
      | 05.08.2021       | 06.08.2021   | 05.08.2021  |
      | 05.08.2021       | 06.08.2021   | 05.08.2021  |
      |                  | 06.08.2021   |             |
      |                  | 06.08.2021   |             |


  Szenario: Ich will eine Pruefung hinzufügen, aber es existiert noch keine Pruefungsperiode
    Angenommen es existiert keine Pruefungsperiode und die Pruefung "Analysis" soll eingeplant werden
    Wenn ich der Pruefung "Analysis" einen Termin gebe
    Dann bekomme ich eine Fehlermeldung NoPRuefungsPeriodeDefinedException


  Szenario: Ich will einer Pruefung einen neuen starttermin geben, wenn diese sich schon in einen Block befindet
    Angenommen es existiert eine Pruefungsperiode von 01.12.2021 - 21.02.2022
    Und es existiert die Pruefung "Informationstechnik"
    Und die Pruefung "Informationstechnik" ist im Block "tollerBlock"

    Wenn ich der Pruefung "Informationstechnik" einen Termin gebe
    Dann bekomme ich eine Fehlermeldung IlligaleArgumentException, da sie in ein Block liegt



  #IllegalArgumentException – Wenn Prüfung in einem Block ist.
 # IllegalStateException – Wenn die übergebene Prüfung nicht der Prüfungsperiode zugeordnet ist.
