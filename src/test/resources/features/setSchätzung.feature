# language: de
Funktionalität: Als Planender möchte ich die Schaetzung von Studierenden, die an einer Prüfung teilnehmen, aendern können.

  Szenario: Die Schaetzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich geaendert
    Angenommen die Prüfung "Analysis" hat die Schaetzung 100
    Wenn ich die Schaetzung von "Analysis" zu 200 aendere
    Dann ist die Schaetzung von "Analysis" den Wert 200

  Szenario: Die Schaetzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich geaendert
    Angenommen die Prüfung "Analysis" hat die Schaetzung 100
    Wenn ich die Schaetzung von "Analysis" zu -1 aendere
    Dann ist die Schaetzung von "Analysis" den Wert 100

  Szenario: Die Schaetzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich geaendert
    Angenommen die Prüfung "Analysis" hat die Schaetzung 100
    Wenn ich die Schaetzung von "Analysis" zu 0 aendere
    Dann ist die Schaetzung von "Analysis" den Wert 0

  Szenario: Die Schaetzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich geaendert
    Angenommen die Prüfung "Analysis" hat die Schaetzung 100
    Wenn ich die Schaetzung von "Analysis" zu 1 aendere
    Dann ist die Schaetzung von "Analysis" den Wert 1

  Szenario: Die Schaetzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich geaendert
    Angenommen die Prüfung "Analysis" hat die Schaetzung 100
    Wenn ich die Schaetzung von "Analysis" zu 1000 aendere
    Dann ist die Schaetzung von "Analysis" den Wert 1000


  Szenario: Die Schaetzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich geaendert
    Angenommen die Prüfung "Analysis" noch keine Schaetzung
    Wenn ich die Schaetzung von "Analysis" zu 200 aendere
    Dann ist die Schaetzung von "Analysis" den Wert 200

