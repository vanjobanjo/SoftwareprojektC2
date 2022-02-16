# language: de
Funktionalität: Als Planende*r moechte ich Bloecke erstellen koennen

  Szenario: Ich erstelle erfolgreich einen Block mit geplanten Klausuren, die sich eigentlich ausschließen
    Angenommen es existiert eine Pruefungsperiode
    Und es existieren die geplanten Pruefungen "Analysis" und "Diskrete Mathematik"
    Wenn ich einen Block mit den geplanten Pruefungen "Analysis" und "Diskrete Mathematik" erstelle
    Dann erhalte ich ein Fehlermeldung Pruefungen sind geplant


  Szenario: Ich erstelle erfolgreich einen Block mit geplanten Klausuren, die sich nicht ausschließen
    Angenommen es existiert eine Pruefungsperiode
    Und es existieren die geplanten Pruefungen "AuD" und "foop"
    Wenn ich einen Block mit den geplanten Pruefungen "AuD" und "foop" erstelle
    Dann erhalte ich ein Fehlermeldung Pruefungen sind geplant


  Szenario: Ich erstelle erfolgreich einen Block mit ungeplanten Klausuren, die sich eigentlich ausschließen
    Angenommen es existiert eine Pruefungsperiode
    Und es existieren die ungeplanten Klausuren "Analysis" und "Diskrete Mathematik"
    Wenn ich einen Block mit den ungeplanten Pruefungen "Analysis" und "Diskrete Mathematik" erstelle
    Dann erhalte ich einen Block mit den Pruefungen "Analysis" und "Diskrete Mathematik"


  Szenario: Ich erstelle erfolgreich einen Block mit ungeplanten Klausuren, die sich nicht ausschließen
    Angenommen es existiert eine Pruefungsperiode
    Und es existieren die ungeplanten Klausuren "AuD" und "foop"
    Wenn ich einen Block mit den ungeplanten Pruefungen "AuD" und "foop" erstelle
    Dann erhalte ich einen Block mit den Pruefungen "AuD" und "foop"


  Szenario: Ich moechte einen Block mit ungeplanten und geplanten Pruefungen erstellen
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert die geplante Pruefung "Digitaltechnik" und die ungeplante Pruefung "Rechnernetze"
    Wenn ich einen gemischten Block mit der geplanten Pruefung "Digitaltechnik" und ungeplanten "Rechnerstrukturen" erstelle
    Dann erhalte ich ein Fehlermeldung Pruefungen sind geplant


  Szenario: Ich erstelle einen Block mit nur einer ungeplanten Pruefung
    Angenommen es existiert eine Pruefungsperiode
    Angenommen es existiert die ungeplante Pruefung "Echtzeitsysteme"
    Wenn ich einen Block mit der Pruefung "Echtzeitsysteme" erstelle
    Dann erhalte ich einen Block mit der Pruefung "Echtzeitsysteme"

  Szenario: Ich erstelle einen Block mit nur einer geplanten Pruefung
    Angenommen es existiert eine Pruefungsperiode
    Angenommen es existiert die geplante Pruefung "Echtzeitsysteme"
    Wenn ich einen Block mit der geplanten Pruefung "Echtzeitsysteme" erstelle
    Dann erhalte ich ein Fehlermeldung Pruefungen sind geplant
