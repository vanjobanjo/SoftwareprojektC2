# language: de
Funktionalität: Als Planender möchte ich erfahren können welche Klausuren nicht eingeplant sind.

  Szenario: Es gibt keine Klausuren
    Angenommen es existieren keine Klausuren
    Wenn ich alle ungeplanten Klausuren anfrage
    Dann bekomme ich keine Klausuren

  Szenario: Es gibt nur ungeplante Klausuren
    Angenommen es existieren die folgenden Klausuren:
      | geplant | Name | StartZeit | EndZeit |
      | nein    | abc  |           |         |
      | nein    | def  |           |         |
    Wenn ich alle ungeplanten Klausuren anfrage
    Dann bekomme ich die Klausuren:
      | Name | StartZeit | EndZeit |
      |      |           |         |
      |      |           |         |
      |      |           |         |

  Szenario: Es gibt nur geplante Klausuren
    Angenommen es existieren die folgenden Klausuren:
      | geplant | Name | StartZeit | EndZeit |
      | ja      | abc  |           |         |
      | ja      | def  |           |         |
    Wenn ich alle ungeplanten Klausuren anfrage
    Dann bekomme ich keine Klausuren

  Szenario: Es gibt geplante und geplante Klausuren
    Angenommen es existieren die folgenden Klausuren:
      | geplant | Name | StartZeit | EndZeit |
      | nein    | abc  |           |         |
      | ja      | def  |           |         |
      | nein    | def  |           |         |
      | ja      | def  |           |         |
    Wenn ich alle ungeplanten Klausuren anfrage
    Dann bekomme ich keine Klausuren

  Szenario: Es gibt ungeplante Klausuren die in einem Block sind
    Angenommen es existieren die folgenden Klausuren:
      | geplant | Name | StartZeit | EndZeit | Block |
      | nein    | abc  |           |         | #1    |
      | nein    | def  |           |         |       |
      | nein    | def  |           |         | #1    |
    Wenn ich alle ungeplanten Klausuren anfrage
    Dann bekomme ich den Block als Teil der ungeplanten Klausuren
