package de.sepulzera.notes.bf.helper;

import org.junit.Test;

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DateUtilTest {
  @Test
  public void parseDate_NullableTest() {
    assertNull(DateUtil.parseDate(null));
  }

  @Test
  public void formatDate_NullableTest() {
    final String ret = DateUtil.toISO8601(null);
    assertEquals("", ret);
  }

  @Test
  public void parseFormat_Test() {
    final Date date = GregorianCalendar.getInstance().getTime();
    final DateFormat df = DateUtil.getDefaultDateFormatter();
    Date parsedDate = Objects.requireNonNull(DateUtil.parseDate(DateUtil.toISO8601(date)));
    assertEquals(df.format(date), df.format(parsedDate));
  }
}
