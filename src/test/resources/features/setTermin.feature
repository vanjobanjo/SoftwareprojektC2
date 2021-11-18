# language: de
Funktionalität: Als Planender möchte ich den Termin von einer Prüfung ändern können. Und an einem Tag kann nur eine Klausur geschrieben werden.

  Szenariogrundriss: Der Termin einer Prüfung wird erfolgreich geändert, da er innerhalb der Prüfungperiode liegt
    Angenommen die Prüfung <Pruefung> hat den Termin <aktueller Termin> und die Prüfungsperiode von <Pruefungsperiodenstart> - <Pruefungsperiodenende> und es gibt noch keine Prüfungen
    Wenn ich den Termin von <Pruefung> auf den <neuer Termin> ändere
    Dann ist der Termin von <Pruefung> am <dann Termin>
    Beispiele:
      | Pruefung   | aktueller Termin | neuer Termin | dann Termin | Pruefungsperiodenstart | Pruefungsperiodenende |
      | "Analysis" | 05.08.2021       | 06.08.2021   | 06.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" | 05.08.2021       | 04.08.2021   | 04.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" | 05.08.2021       | 10.08.2021   | 10.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 06.08.2021   | 06.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 04.08.2021   | 04.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 10.08.2021   | 10.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" | 05.08.2021       | 04.08.2021   | 04.08.2021  | 04.08.2021             | 10.08.2021            |


  Szenariogrundriss: Der Termin einer Prüfung wird nicht erfolgreich geändert, da er ausserhalb der Prüfungperiode liegt
    Angenommen die Prüfung <Pruefung> hat den Termin <aktueller Termin> und die Prüfungsperiode von <Pruefungsperiodenstart> - <Pruefungsperiodenende> und es gibt noch keine Prüfungen
    Wenn ich den Termin von <Pruefung> auf den <neuer Termin> ändere
    Dann ist der Termin von <Pruefung> <dann Termin> und bekommt eine Fehlermeldung
    Beispiele:
      | Pruefung   | aktueller Termin | neuer Termin | dann Termin | Pruefungsperiodenstart | Pruefungsperiodenende |
      | "Analysis" | 05.08.2021       | 03.08.2021   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" | 05.08.2021       | 11.08.2021   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" | 05.08.2021       | 06.08.2022   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 03.08.2021   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 03.08.2021   |             | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 11.08.2021   |             | 04.08.2021             | 10.08.2021            |


  Szenariogrundriss: Der Termin einer Prüfung wird erfolgreich geändert, da er innerhalb der Prüfungperiode liegt
    Angenommen die Prüfung <Pruefung> hat den Termin <aktueller Termin> und die Prüfungsperiode von <Pruefungsperiodenstart> - <Pruefungsperiodenende> und es gibt schon Prüfungen
      | Pruefung              | Termin     |
      | "Analysis"            | 05.08.2021 |
      | "Informationstechnik" | 06.08.2021 |
    Wenn ich den Termin von <Pruefung> auf den <neuer Termin> ändere
    Dann ist der Termin von <Pruefung> am <dann Termin>
    Beispiele:
      | Pruefung   | aktueller Termin | neuer Termin | dann Termin | Pruefungsperiodenstart | Pruefungsperiodenende |
      | "Analysis" | 05.08.2021       | 07.08.2021   | 07.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" | 05.08.2021       | 04.08.2021   | 04.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" | 05.08.2021       | 10.08.2021   | 10.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 07.08.2021   | 07.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 04.08.2021   | 04.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 10.08.2021   | 10.08.2021  | 04.08.2021             | 10.08.2021            |


  Szenariogrundriss: Der Termin einer Prüfung wird nicht erfolgreich geändert, da sie probiert wird auf ein Termin zu schieben, der schon belegt ist.
    Angenommen die Prüfung <Pruefung> hat den Termin <aktueller Termin> und die Prüfungsperiode von <Pruefungsperiodenstart> - <Pruefungsperiodenende> und es gibt schon Prüfungen
      | Pruefung              | Termin     |
      | "Analysis"            | 05.08.2021 |
      | "Informationstechnik" | 06.08.2021 |
    Wenn ich den Termin von <Pruefung> auf den <neuer Termin> ändere
    Dann ist der Termin von <Pruefung> <dann Termin> und bekommt eine Fehlermeldung
    Beispiele:
      | Pruefung   | aktueller Termin | neuer Termin | dann Termin | Pruefungsperiodenstart | Pruefungsperiodenende |
      | "Analysis" | 05.08.2021       | 06.08.2021   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" | 05.08.2021       | 06.08.2021   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" | 05.08.2021       | 06.08.2022   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 06.08.2021   |             | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 06.08.2021   |             | 04.08.2021             | 10.08.2021            |




