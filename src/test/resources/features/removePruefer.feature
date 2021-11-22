# language: de
Funktionalität: Als Planender möchte ich einen Prüfer von einer Prüfung entfernen.

  Szenariogrundriss: Die Prüfung hat einen Prüfer
    Angenommen die Prüfung <Prüfung> hat den Prüfer <PrüferEins>
    Wenn ich den Prüfer <Prüferentfernen> entferne
    Dann hat die Prüfung <Prüfung> <result>
    Beispiele:
      | Prüfung         | PrüferEins          | Prüferentfernen     | result              |
      | "Analysis"      | "Prof. Dr. Harms"   | "Prof. Dr. Harms"   |                     |
      | "Analysis"      | "Prof. Dr. Harms"   | "Kein Namen"        | "Prof. Dr. Harms"   |
      | "Analysis"      | "PrüferEins"        | "Kein Name"         | "PrüferEins"        |
      | "Analysis"      | "PrüferEins"        | "PrüferEins"        | "PrüferZwei"        |
      | "Analysis"      | "PrüferEins"        | "PrüferZwei"        | "PrüferEins"        |
      | "Analysis"      |                     | "PrüferZwei"        |                     |
      | "IT-Sicherheit" | "Prof. Dr. Beuster" | "Prof. Dr. Beuster" |                     |
      | "IT-Sicherheit" | "Prof. Dr. Beuster" | "Kein Namen"        | "Prof. Dr. Beuster" |
      | "IT-Sicherheit" | "PrüferEins"        | "Kein Name"         | "PrüferEins"        |
      | "IT-Sicherheit" | "PrüferEins"        | "PrüferEins"        | "PrüferZwei"        |
      | "IT-Sicherheit" | "PrüferEins"        | "PrüferZwei"        | "PrüferEins"        |
      | "IT-Sicherheit" |                     | "PrüferZwei"        |                     |


  Szenariogrundriss: Die Prüfung hat einen Prüfer
    Angenommen die Prüfung <Prüfung> hat den Prüfer <PrüferEins> und <PrüferZwei>
    Wenn ich den Prüfer <Prüferentfernen> entferne
    Dann hat die Prüfung <Prüfung> <result> und <resultTwo>
    Beispiele:
      | Prüfung         | PrüferEins          | PrüferZwei   | Prüferentfernen     | result              | resultTwo    |
      | "Analysis"      | "Prof. Dr. Harms"   |              | "Prof. Dr. Harms"   |                     |              |
      | "Analysis"      | "Prof. Dr. Harms"   |              | "Kein Namen"        | "Prof. Dr. Harms"   |              |
      | "Analysis"      | "PrüferEins"        | "PrüferZwei" | "Kein Name"         | "PrüferEins"        | "PrüferZwei" |
      | "Analysis"      | "PrüferEins"        | "PrüferZwei" | "PrüferEins"        | "PrüferZwei"        |              |
      | "Analysis"      | "PrüferEins"        | "PrüferZwei" | "PrüferZwei"        | "PrüferEins"        |              |
      | "Analysis"      |                     |              | "PrüferZwei"        |                     |              |
      | "IT-Sicherheit" | "Prof. Dr. Beuster" |              | "Prof. Dr. Beuster" |                     |              |
      | "IT-Sicherheit" | "Prof. Dr. Beuster" |              | "Kein Namen"        | "Prof. Dr. Beuster" |              |
      | "IT-Sicherheit" | "PrüferEins"        | "PrüferZwei" | "Kein Name"         | "PrüferEins"        | "PrüferZwei" |
      | "IT-Sicherheit" | "PrüferEins"        | "PrüferZwei" | "PrüferEins"        | "PrüferZwei"        |              |
      | "IT-Sicherheit" | "PrüferEins"        | "PrüferZwei" | "PrüferZwei"        | "PrüferEins"        |              |
      | "IT-Sicherheit" |                     |              | "PrüferZwei"        |                     |              |
