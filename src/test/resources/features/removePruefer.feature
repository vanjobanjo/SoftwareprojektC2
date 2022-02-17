# language: de
Funktionalität: Als Planender maechte ich einen Pruefer von einer Pruefung entfernen.

  Szenariogrundriss: Die Pruefung hat einen Pruefer und es wird erfolgreich versucht ein Pruefer zu entfernen
    Angenommen es existiert eine Pruefungsperiode
    Und die Pruefung <Pruefung> hat den Pruefer <PrueferEins>
    Wenn ich den Pruefer <Prueferentfernen> von der <Pruefung> entferne
    Dann hat die Pruefung <Pruefung> <result> als Pruefer
    Beispiele:
      | Pruefung        | PrueferEins         | Prueferentfernen    | result |
      | "Analysis"      | "Prof. Dr. Harms"   | "Prof. Dr. Harms"   | ""     |
      | "Analysis"      | "PrueferEins"       | "PrueferEins"       | ""     |
      | "IT-Sicherheit" | "Prof. Dr. Beuster" | "Prof. Dr. Beuster" | ""     |
      | "IT-Sicherheit" | "PrueferEins"       | "PrueferEins"       | ""     |

  Szenariogrundriss: Die Pruefung hat einen Pruefer und es wird nicht erfolgreich versucht ein Pruefer zu entfernen
    Angenommen es existiert eine Pruefungsperiode
    Und die Pruefung <Pruefung> hat den Pruefer <PrueferEins>
    Wenn ich den Pruefer <Prueferentfernen> von der <Pruefung> entferne
    Dann hat die Pruefung <Pruefung> <result> als Pruefer
    Beispiele:
      | Pruefung        | PrueferEins         | Prueferentfernen | result              |
      | "Analysis"      | "Prof. Dr. Harms"   | "Kein Namen"     | "Prof. Dr. Harms"   |
      | "Analysis"      | "PrueferEins"       | "Kein Name"      | "PrueferEins"       |
      | "Analysis"      | "PrueferEins"       | "PrueferZwei"    | "PrueferEins"       |
      | "IT-Sicherheit" | "Prof. Dr. Beuster" | "Kein Namen"     | "Prof. Dr. Beuster" |
      | "IT-Sicherheit" | "PrueferEins"       | "Kein Name"      | "PrueferEins"       |
      | "IT-Sicherheit" | "PrueferEins"       | "PrueferZwei"    | "PrueferEins"       |


  Szenariogrundriss: Die Pruefung hat zwei Pruefer und es wird erfolgreich ein Pruefer entfern
    Angenommen es existiert eine Pruefungsperiode
    Und die Pruefung <Pruefung> hat den Pruefer <PrueferEins> und <PrueferZwei> als Pruefer
    Wenn ich den Pruefer <Prueferentfernen> von der <Pruefung> entferne
    Dann hat die Pruefung <Pruefung> <result> und <resultTwo> als Pruefer
    Beispiele:
      | Pruefung        | PrueferEins   | PrueferZwei   | Prueferentfernen | result        | resultTwo |
      | "Analysis"      | "PrueferEins" | "PrueferZwei" | "PrueferEins"    | "PrueferZwei" | ""        |
      | "Analysis"      | "PrueferEins" | "PrueferZwei" | "PrueferZwei"    | "PrueferEins" | ""        |
      | "IT-Sicherheit" | "PrueferEins" | "PrueferZwei" | "PrueferEins"    | "PrueferZwei" | ""        |
      | "IT-Sicherheit" | "PrueferEins" | "PrueferZwei" | "PrueferZwei"    | "PrueferEins" | ""        |

  Szenariogrundriss: Die Pruefung hat zwei Pruefer und es wird nicht  erfolgreich ein Pruefer entfern
    Angenommen es existiert eine Pruefungsperiode
    Und die Pruefung <Pruefung> hat den Pruefer <PrueferEins> und <PrueferZwei> als Pruefer
    Wenn ich den Pruefer <Prueferentfernen> von der <Pruefung> entferne
    Dann hat die Pruefung <Pruefung> <result> und <resultTwo> als Pruefer
    Beispiele:
      | Pruefung        | PrueferEins   | PrueferZwei   | Prueferentfernen | result        | resultTwo     |
      | "Analysis"      | "PrueferEins" | "PrueferZwei" | "Kein Name"      | "PrueferEins" | "PrueferZwei" |
      | "IT-Sicherheit" | "PrueferEins" | "PrueferZwei" | "Kein Name"      | "PrueferEins" | "PrueferZwei" |

  Szenariogrundriss: Die Pruefung hat keinen Pruefer und es wird versucht ein Pruefer zu entfernen
    Angenommen es existiert eine Pruefungsperiode
    Und die Pruefung <Pruefung> hat keinen Pruefer eingetragen
    Wenn ich den Pruefer <Prueferentfernen> von der <Pruefung> entferne
    Dann hat die Pruefung <Pruefung> <result> als Pruefer
    Beispiele:
      | Pruefung        | Prueferentfernen | result |
      | "Analysis"      | "Kein Name"      | ""     |
      | "Analysis"      | "PrueferEins"    | ""     |
      | "Analysis"      | "PrueferZwei"    | ""     |
      | "IT-Sicherheit" | "Kein Name"      | ""     |
      | "IT-Sicherheit" | "PrueferEins"    | ""     |
      | "IT-Sicherheit" | "PrueferZwei"    | ""     |


  Szenario: Es existiert noch keine Pruefungsperiode und es soll ein Pruefer von einer Pruefung entfernt werden
    Angenommen es existiert keine Pruefungsperiode
    Und es soll probiert werden eine Pruefer von einer Pruefung entfernt
    Wenn ich ein Pruefer "PrueferEins" von einer Pruefung "Analysis" entfernen möchte
    Dann bekomme ich eine Fehlermeldung NoPRuefungsPeriodeDefinedException

  Szenario:  eine unbekannte Pruefung soll ein Pruefer entfernt werden
    Angenommen es existiert eine Pruefungsperiode
    Und es existiert keine Pruefung "Informationstechnik"
    Wenn ich ein Pruefer "PrueferEins" von einer Pruefung "Informationstechnik" entfernen möchte
    Dann bekomme ich eine Fehlermeldung IllegalStateException

  Szenario: einer bekannten Pruefung soll ein blanke Pruefer entfernt werden
  Angenommen es existiert eine Pruefungsperiode
    Und es soll eine Pruefer von einer Pruefung "Informationstechnik" entfernt werden
  Wenn ich einen leeren Pruefer "" von einer Pruefung "Informationstechnik" entfernen möchte
  Dann bekomme ich eine Fehlermeldung IlligaleArgumentException

 # IllegalArgumentException – Wenn Prüfer blank.