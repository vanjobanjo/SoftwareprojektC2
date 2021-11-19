# language: de
Funktionalität: Als Planender möchte ich die Schätzung von Studierenden, die an einer Prüfung teilnehmen, ändern können.

  Szenario: Die Schätzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich geändert
    Angenommen die Prüfung "Analysis" hat die Schätzung 100
    Wenn ich die Schätzung von "Analysis" zu 200 ändere
    Dann ist die Schätzung von "Analysis" den Wert 200

  Szenario: Die Schätzung der Studierenden, die an einer Prüfung teilnehmen werden, nicht erfolgreich geändert
    Angenommen die Prüfung "Analysis" hat die Schätzung 100
    Wenn ich die Schätzung von "Analysis" zu -1 ändere
    Dann ist die Schätzung von "Analysis" den Wert 100

  Szenario: Die Schätzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich geändert
    Angenommen die Prüfung "Analysis" hat die Schätzung 100
    Wenn ich die Schätzung von "Analysis" zu 0 ändere
    Dann ist die Schätzung von "Analysis" den Wert 0

  Szenario: Die Schätzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich geändert
    Angenommen die Prüfung "Analysis" hat die Schätzung 100
    Wenn ich die Schätzung von "Analysis" zu 1 ändere
    Dann ist die Schätzung von "Analysis" den Wert 1

  Szenario: Die Schätzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich geändert
    Angenommen die Prüfung "Analysis" hat die Schätzung 100
    Wenn ich die Schätzung von "Analysis" zu 1000 ändere
    Dann ist die Schätzung von "Analysis" den Wert 1000


  Szenario: Die Schätzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich geändert
    Angenommen die Prüfung "Analysis" noch keine Schätzung
    Wenn ich die Schätzung von "Analysis" zu 200 ändere
    Dann ist die Schätzung von "Analysis" den Wert 200
