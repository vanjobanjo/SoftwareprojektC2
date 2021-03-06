# language: de
Funktionalität: Als Planender maechte ich einen Pruefer von einer Pruefung entfernen.

  Szenariogrundriss: Die Pruefung hat einen Pruefer und es wird erfolgreich versucht ein Pruefer zu entfernen
    Angenommen die Pruefung <Pruefung> hat den Pruefer <PrueferEins>
    Wenn ich den Pruefer <Prueferentfernen> entferne
    Dann hat die Pruefung <Pruefung> <result>
    Beispiele:
      | Pruefung        | PrueferEins         | Prueferentfernen    | result              |
      | "Analysis"      | "Prof. Dr. Harms"   | "Prof. Dr. Harms"   | ""                  |
      | "Analysis"      | "PrueferEins"       | "PrueferEins"       | ""                  |
      | "IT-Sicherheit" | "Prof. Dr. Beuster" | "Prof. Dr. Beuster" | ""                  |
      | "IT-Sicherheit" | "PrueferEins"       | "PrueferEins"       | ""                  |

  Szenariogrundriss: Die Pruefung hat einen Pruefer und es wird nicht erfolgreich versucht ein Pruefer zu entfernen
    Angenommen die Pruefung <Pruefung> hat den Pruefer <PrueferEins>
    Wenn ich den Pruefer <Prueferentfernen> entferne
    Dann hat die Pruefung <Pruefung> <result>
    Beispiele:
      | Pruefung        | PrueferEins         | Prueferentfernen    | result              |
      | "Analysis"      | "Prof. Dr. Harms"   | "Kein Namen"        | "Prof. Dr. Harms"   |
      | "Analysis"      | "PrueferEins"       | "Kein Name"         | "PrueferEins"       |
      | "Analysis"      | "PrueferEins"       | "PrueferZwei"       | "PrueferEins"       |
      | "IT-Sicherheit" | "Prof. Dr. Beuster" | "Kein Namen"        | "Prof. Dr. Beuster" |
      | "IT-Sicherheit" | "PrueferEins"       | "Kein Name"         | "PrueferEins"       |
      | "IT-Sicherheit" | "PrueferEins"       | "PrueferZwei"       | "PrueferEins"       |


  Szenariogrundriss: Die Pruefung hat zwei Pruefer und es wird erfolgreich ein Pruefer entfern
    Angenommen die Pruefung <Pruefung> hat den Pruefer <PrueferEins> und <PrueferZwei>
    Wenn ich den Pruefer <Prueferentfernen> entferne
    Dann hat die Pruefung <Pruefung> <result> und <resultTwo>
    Beispiele:
      | Pruefung        | PrueferEins   | PrueferZwei   | Prueferentfernen | result        | resultTwo     |
      | "Analysis"      | "PrueferEins" | "PrueferZwei" | "PrueferEins"    | "PrueferZwei" | ""            |
      | "Analysis"      | "PrueferEins" | "PrueferZwei" | "PrueferZwei"    | "PrueferEins" | ""            |
      | "IT-Sicherheit" | "PrueferEins" | "PrueferZwei" | "PrueferEins"    | "PrueferZwei" | ""            |
      | "IT-Sicherheit" | "PrueferEins" | "PrueferZwei" | "PrueferZwei"    | "PrueferEins" | ""            |

  Szenariogrundriss: Die Pruefung hat zwei Pruefer und es wird nicht  erfolgreich ein Pruefer entfern
    Angenommen die Pruefung <Pruefung> hat den Pruefer <PrueferEins> und <PrueferZwei>
    Wenn ich den Pruefer <Prueferentfernen> entferne
    Dann hat die Pruefung <Pruefung> <result> und <resultTwo>
    Beispiele:
      | Pruefung        | PrueferEins   | PrueferZwei   | Prueferentfernen | result        | resultTwo     |
      | "Analysis"      | "PrueferEins" | "PrueferZwei" | "Kein Name"      | "PrueferEins" | "PrueferZwei" |
      | "IT-Sicherheit" | "PrueferEins" | "PrueferZwei" | "Kein Name"      | "PrueferEins" | "PrueferZwei" |

  Szenariogrundriss: Die Pruefung hat keinen Pruefer und es wird versucht ein Pruefer zu entfernen
    Angenommen die Pruefung <Pruefung> hat keinen Pruefer eingetragen
    Wenn ich den Pruefer <Prueferentfernen> entferne
    Dann hat die Pruefung <Pruefung> <result>
    Beispiele:
      | Pruefung        | Prueferentfernen | result |
      | "Analysis"      | "Kein Name"      | ""     |
      | "Analysis"      | "PrueferEins"    | ""     |
      | "Analysis"      | "PrueferZwei"    | ""     |
      | "IT-Sicherheit" | "Kein Name"      | ""     |
      | "IT-Sicherheit" | "PrueferEins"    | ""     |
      | "IT-Sicherheit" | "PrueferZwei"    | ""     |