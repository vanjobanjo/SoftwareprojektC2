# language: de
Funktionalität: Als Planender möchte ich den Termin von einer Prüfung ändern können.

  Szenariogrundriss: Der Termin einer Prüfung wird erfolgreich geändert, da er innerhalb der Prüfungperiode liegt
    Angenommen die Prüfung "Analysis" hat den Termin "05.08.2021" und die Prüfungsperiode von "04.08.2021" - "10.08.2021 und es gibt noch keine Prüfungen
    Wenn ich den Termin von "Analysis" auf den "06.08.2021" ändere
    Dann ist der Termin von "Analysis" am "06.08.2021"
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
    Angenommen die Prüfung "Analysis" hat den Termin 05.08.2021 und die Prüfungsperiode von 04.08.2021 - 10.08.2021 und es gibt noch keine Prüfungen
    Wenn ich den Termin von "Analysis" auf den 06.08.2021 ändere
    Dann ist der Termin von "Analysis" 06.08.2021 und bekommt eine Fehlermeldung
    Beispiele:
      | Pruefung   | aktueller Termin | neuer Termin | dann Termin | Pruefungsperiodenstart | Pruefungsperiodenende |
      | "Analysis" | 05.08.2021       | 03.08.2021   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" | 05.08.2021       | 11.08.2021   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" | 05.08.2021       | 06.08.2022   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 03.08.2021   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 03.08.2021   |             | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 11.08.2021   |             | 04.08.2021             | 10.08.2021            |


  Szenariogrundriss: Der Termin einer Prüfung wird erfolgreich geändert, da er innerhalb der Prüfungperiode liegt
    Angenommen die Prüfung "Analysis" hat den Termin "05.08.2021" und die Prüfungsperiode von "04.08.2021" - "10.08.2021 und es gibt schon Prüfungen
    Wenn ich den Termin von "Analysis" auf den "06.08.2021" ändere
    Dann ist der Termin von "Analysis" am "06.08.2021"
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
    Angenommen die Prüfung "Analysis" hat den Termin 05.08.2021 und die Prüfungsperiode von 04.08.2021 - 10.08.2021 und es gibt schon Prüfungen
    Wenn ich den Termin von "Analysis" auf den 06.08.2021 ändere
    Dann ist der Termin von "Analysis" 06.08.2021 und bekommt eine Fehlermeldung
    Beispiele:
      | Pruefung   | aktueller Termin | neuer Termin | dann Termin | Pruefungsperiodenstart | Pruefungsperiodenende |
      | "Analysis" | 05.08.2021       | 03.08.2021   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" | 05.08.2021       | 11.08.2021   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" | 05.08.2021       | 06.08.2022   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 03.08.2021   | 05.08.2021  | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 03.08.2021   |             | 04.08.2021             | 10.08.2021            |
      | "Analysis" |                  | 11.08.2021   |             | 04.08.2021             | 10.08.2021            |




