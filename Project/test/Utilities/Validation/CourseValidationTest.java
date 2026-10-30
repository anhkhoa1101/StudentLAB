package utilities.validation;

import Support.CsvLogger;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class CourseValidationTest {

    @Rule
    public final CsvLogger tcLog = new CsvLogger();

    @Test public void parseDate_ddMMyyyy_ok() {
        assertNotNull(CourseValidation.parseDate("15/09/2030"));
    }

    @Test public void parseDate_invalidDay_returnsNull() {
        assertNull(CourseValidation.parseDate("31/02/2030"));
    }

    @Test public void parseDate_textMonth_ok() {
        assertNotNull(CourseValidation.parseDate("Sep 9, 2030"));
    }

    @Test public void duration_boundaries() {
        assertTrue(CourseValidation.isValidDuration(CourseValidation.MIN_DURATION));
        assertTrue(CourseValidation.isValidDuration(CourseValidation.MAX_DURATION));
        assertFalse(CourseValidation.isValidDuration(CourseValidation.MIN_DURATION - 1));
        assertFalse(CourseValidation.isValidDuration(CourseValidation.MAX_DURATION + 1));
    }
}
