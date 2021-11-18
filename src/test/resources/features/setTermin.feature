# language: de
Funktionalität: Als Planender möchte ich den Termin von einer Prüfung ändern können.

  Szenario: Der Termin einer Prüfung wird erfolgreich geändert, da er innerhalb der Prüfungperiode liegt
    Angenommen die Prüfung "Analysis" hat den Termin 05.08.2021 und die Prüfungsperiode von 04.08.2021 - 10.08.2021
    Wenn ich den Termin von "Analysis" auf den 06.08.2021 ändere
    Dann ist der Termin von "Analysis" 06.08.2021

  Szenario: Der Termin einer Prüfung wird erfolgreich geändert, da er innerhalb der Prüfungperiode liegt
    Angenommen die Prüfung "Analysis" hat den Termin 05.08.2021 und die Prüfungsperiode von 04.08.2021 - 10.08.2021
    Wenn ich den Termin von "Analysis" auf den 04.08.2021 ändere
    Dann ist der Termin von "Analysis" 04.08.2021


  Szenario: Der Termin einer Prüfung wird erfolgreich geändert, da er innerhalb der Prüfungperiode liegt
    Angenommen die Prüfung "Analysis" hat den Termin 05.08.2021 und die Prüfungsperiode von 04.08.2021 - 10.08.2021
    Wenn ich den Termin von "Analysis" auf den 10.08.2021 ändere
    Dann ist die Schätzung von "Analysis" den Wert 10.08.2021

  Szenario: Der Termin einer Prüfung wird erfolgreich geändert, da er innerhalb der Prüfungperiode liegt
    Angenommen die Prüfung "Analysis" hat den Termin keinWert und die Prüfungsperiode von 04.08.2021 - 10.08.2021
    Wenn ich den Termin von "Analysis" auf den 06.08.2021 ändere
    Dann ist die Schätzung von "Analysis" den Wert 06.08.2021

  Szenario: Der Termin einer Prüfung wird erfolgreich geändert, da er innerhalb der Prüfungperiode liegt
    Angenommen die Prüfung "Analysis" hat den Termin keinWert und die Prüfungsperiode von 04.08.2021 - 10.08.2021
    Wenn ich den Termin von "Analysis" auf den 04.08.2021 ändere
    Dann ist die Schätzung von "Analysis" den Wert 04.08.2021

  Szenario: Der Termin einer Prüfung wird erfolgreich geändert, da er innerhalb der Prüfungperiode liegt
    Angenommen die Prüfung "Analysis" hat den Termin keinWert und die Prüfungsperiode von 04.08.2021 - 10.08.2021
    Wenn ich den Termin von "Analysis" auf den 10.08.2021 ändere
    Dann ist die Schätzung von "Analysis" den Wert 10.08.2021

  Szenario: Der Termin einer Prüfung wird nicht erfolgreich geändert, da er ausserhalb der Prüfungperiode liegt
    Angenommen die Prüfung "Analysis" hat den Termin 05.08.2021 und die Prüfungsperiode von 04.08.2021 - 10.08.2021
    Wenn ich den Termin von "Analysis" auf den 03.08.2021 ändere
    Dann ist die Schätzung von "Analysis" den Wert 05.08.2021

  Szenario: Der Termin einer Prüfung wird nicht erfolgreich geändert, da er ausserhalb der Prüfungperiode liegt
    Angenommen die Prüfung "Analysis" hat den Termin 05.08.2021 und die Prüfungsperiode von 04.08.2021 - 10.08.2021
    Wenn ich den Termin von "Analysis" auf den 11.08.2021 ändere
    Dann ist die Schätzung von "Analysis" den Wert 05.08.2021

  Szenario: Der Termin einer Prüfung wird nicht erfolgreich geändert, da er ausserhalb der Prüfungperiode liegt
    Angenommen die Prüfung "Analysis" hat den Termin 05.08.2021 und die Prüfungsperiode von 04.08.2021 - 10.08.2021
    Wenn ich den Termin von "Analysis" auf den 06.08.2022 ändere
    Dann ist die Schätzung von "Analysis" den Wert 05.08.2021

  Szenario: Der Termin einer Prüfung wird nicht erfolgreich gesetzt, da er ausserhalb der Prüfungperiode liegt
    Angenommen die Prüfung "Analysis" hat den Termin keinWert und die Prüfungsperiode von 04.08.2021 - 10.08.2021
    Wenn ich den Termin von "Analysis" auf den 03.08.2021 ändere
    Dann ist die Schätzung von "Analysis" den Wert keinWert

  Szenario: Der Termin einer Prüfung wird nicht erfolgreich gesetzt, da er ausserhalb der Prüfungperiode liegt
    Angenommen die Prüfung "Analysis" hat den Termin keinWert und die Prüfungsperiode von 04.08.2021 - 10.08.2021
    Wenn ich den Termin von "Analysis" auf den 11.08.2021 ändere
    Dann ist die Schätzung von "Analysis" den Wert keinWert


