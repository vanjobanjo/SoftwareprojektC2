package integrationTests;

import io.cucumber.java.ParameterType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DataTypes {

  /**
   * Wandelt ein String in Format dd.mm.yyyy um hh:mm Uhr in eine LocalDateTime mit der Uhrzeit
   * 09:00
   *
   * @param dateAndTime das Datum und Zeit fuer die neue LocalDateTime
   * @return den String als LocalDateTime
   */
  public LocalDateTime parseLocalDateTime(String dateAndTime) {
    if (dateAndTime != null) {
      String[] splitDateAndTime = dateAndTime.split(" ");
      LocalDate date = parseDate(splitDateAndTime[0]);
      if (splitDateAndTime.length > 1) {
        LocalTime time = parseTime(splitDateAndTime[2]);
        return LocalDateTime.of(date, time);
      } else {
        return LocalDateTime.of(date, LocalTime.of(9, 0));
      }
    } else {
      return null;
    }
  }


  public static LocalDate parseDate(String dateTxt) {
    String[] tmp = dateTxt.split("\\.");
    int day = Integer.parseInt(tmp[0]);
    int month = Integer.parseInt(tmp[1]);
    int year = Integer.parseInt(tmp[2]);
    return LocalDate.of(year, month, day);
  }

  public static LocalTime parseTime(String timeTxt) {
    String[] tmp = timeTxt.split(":");
    int hours = Integer.parseInt(tmp[0]);
    int minutes = Integer.parseInt(tmp[1]);
    return LocalTime.of(hours, minutes);
  }


}
