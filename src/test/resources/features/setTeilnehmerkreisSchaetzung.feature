# language: de
Funktionalität: Als Planender möchte ich die Schätzung von Studierenden, die an einer Prüfung teilnehmen, ändern können.

  Szenario: Die Schätzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich geändert
    Angenommen es existiert eine Pruefungsperiode
    Und die Prüfung "Analysis" hat den Teilnehmerkreis "inf" und die Schätzung 100
    Wenn ich die Schätzung von "Analysis" mit den Teilnehmerkreis "inf" zu 200 ändere
    Dann ist die Schätzung von "Analysis" mit den Teilnehmerkreis "inf" den Wert 200

  Szenario: Die Schätzung der Studierenden, die an einer Prüfung teilnehmen werden, nicht erfolgreich geändert
    Angenommen es existiert eine Pruefungsperiode
    Und die Prüfung "Analysis" hat den Teilnehmerkreis "inf" und die Schätzung 100
    Wenn ich die Schätzung von "Analysis" mit den Teilnehmerkreis "inf" zu -1 ändere
    Dann ist die Schätzung von "Analysis" mit den Teilnehmerkreis "inf" den Wert 100
    Und bekomme ich eine Fehlermeldung fuer eine IllegalArgumentException

  Szenario: Die Schätzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich zu 0 geändert
    Angenommen es existiert eine Pruefungsperiode
    Und die Prüfung "Analysistest" hat den Teilnehmerkreis "inf" und die Schätzung 100
    Wenn ich die Schätzung von "Analysistest" mit den Teilnehmerkreis "inf" zu 0 ändere
    Dann ist die Schätzung von "Analysistest" mit den Teilnehmerkreis "inf" den Wert 0

  Szenario: Die Schätzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich zu 1 geändert
    Angenommen es existiert eine Pruefungsperiode
    Und die Prüfung "Analysis" hat den Teilnehmerkreis "inf" und die Schätzung 100
    Wenn ich die Schätzung von "Analysis" mit den Teilnehmerkreis "inf" zu 1 ändere
    Dann ist die Schätzung von "Analysis" mit den Teilnehmerkreis "inf" den Wert 1

  Szenario: Die Schätzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich zu 1000 geändert
    Angenommen es existiert eine Pruefungsperiode
    Und die Prüfung "Analysis" hat den Teilnehmerkreis "inf" und die Schätzung 100
    Wenn ich die Schätzung von "Analysis" mit den Teilnehmerkreis "inf" zu 1000 ändere
    Dann ist die Schätzung von "Analysis" mit den Teilnehmerkreis "inf" den Wert 1000


  Szenario: Die Schätzung der Studierenden, die an einer Prüfung teilnehmen werden, erfolgreich zu 200 geändert aber vorher kein Teilnehmerkreis existiert
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die ungeplante Pruefung "Analysis" mit dem Teilnehmerkreis "inf" im 1 Semester
    Wenn ich die Schätzung von "Analysis" mit den Teilnehmerkreis "inf" zu 200 ändere
    Dann ist die Schätzung von "Analysis" mit den Teilnehmerkreis "inf" den Wert 200



       #IllegalArgumentException – Wenn die übergebene Schätzung < 0.
  Szenario: Die Schätzung der Studierenden, die an einer Prüfung teilnehmen werden, nicht erfolgreich geändert da -1
    Angenommen es existiert eine Pruefungsperiode
    Und die Prüfung "Analysis" hat den Teilnehmerkreis "inf" und die Schätzung 100
    Wenn ich die Schätzung von "Analysis" mit den Teilnehmerkreis "inf" zu -1 ändere
    Dann ist die Schätzung von "Analysis" mit den Teilnehmerkreis "inf" den Wert 100
    Und bekomme ich eine Fehlermeldung fuer eine IllegalArgumentException

            #IllegalArgumentException – Wenn der Teilnehmerkreis der Prüfung noch nicht zugeordnet wurde.
  Szenario: Die Schätzung der Studierenden, die an einer Prüfung teilnehmen werden, nicht erfolgreich geändert da
    Angenommen es existiert eine Pruefungsperiode
    Und die Prüfung "Analysis" noch keinen Teilnehmerkreis mit Schätzung
    Wenn ich die Schätzung von "Analysis" mit den Teilnehmerkreis "inf" zu 6 ändere
    Und bekomme ich eine Fehlermeldung fuer eine IllegalArgumentException

    #IllegalStateException – Wenn die übergebene Prüfung nicht der Prüfungsperiode zugeordnet ist.
  Szenario: Die Pruefung nicht in der Pruefungsperiode existiert
    Angenommen es existiert eine Pruefungsperiode
    Wenn ich die Schätzung einer Unbekannten Pruefung ändere
    Dann bekomme ich eine Fehlermeldung IllegalStateException






