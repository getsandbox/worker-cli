package org.jliquid.liqp.filters;

import java.util.Locale;
import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import java.text.SimpleDateFormat;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DateTest {

    @Test
    public void applyTest() throws RecognitionException {

        final int seconds = 946702800;
        final java.util.Date date = new java.util.Date(seconds * 1000L);

        String[][] tests = {
            {"{{" + seconds + " | date: 'mu'}}", "mu"},
            {"{{" + seconds + " | date: '%'}}", "%"},
            {"{{" + seconds + " | date: '%? %%'}}", "%? %"},
            {"{{" + seconds + " | date: '%a'}}", new SimpleDateFormat("EEE").format(date)},
            {"{{" + seconds + " | date: '%A'}}", new SimpleDateFormat("EEEE").format(date)},
            {"{{" + seconds + " | date: '%b'}}", new SimpleDateFormat("MMM").format(date)},
            {"{{" + seconds + " | date: '%B'}}", new SimpleDateFormat("MMMM").format(date)},
            {"{{" + seconds + " | date: '%c'}}", new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").format(date)},
            {"{{" + seconds + " | date: '%d'}}", new SimpleDateFormat("dd").format(date)},
            {"{{" + seconds + " | date: '%H'}}", new SimpleDateFormat("HH").format(date)},
            {"{{" + seconds + " | date: '%I'}}", new SimpleDateFormat("hh").format(date)},
            {"{{" + seconds + " | date: '%j'}}", new SimpleDateFormat("DDD").format(date)},
            {"{{" + seconds + " | date: '%m'}}", new SimpleDateFormat("MM").format(date)},
            {"{{" + seconds + " | date: '%M'}}", new SimpleDateFormat("mm").format(date)},
            {"{{" + seconds + " | date: '%p'}}", new SimpleDateFormat("a").format(date)},
            {"{{" + seconds + " | date: '%S'}}", new SimpleDateFormat("ss").format(date)},
            {"{{" + seconds + " | date: '%U'}}", new SimpleDateFormat("ww").format(date)},
            {"{{" + seconds + " | date: '%W'}}", new SimpleDateFormat("ww").format(date)},
            {"{{" + seconds + " | date: '%w'}}", new SimpleDateFormat("F").format(date)},
            {"{{" + seconds + " | date: '%x'}}", new SimpleDateFormat("MM/dd/yy").format(date)},
            {"{{" + seconds + " | date: '%X'}}", new SimpleDateFormat("HH:mm:ss").format(date)},
            {"{{" + seconds + " | date: 'x=%y'}}", "x=" + new SimpleDateFormat("yy").format(date)},
            {"{{" + seconds + " | date: '%Y'}}", new SimpleDateFormat("yyyy").format(date)},
            {"{{" + seconds + " | date: '%Z'}}", new SimpleDateFormat("z").format(date)}
        };

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_date
     *   assert_equal 'May', @filters.date(Time.parse("2006-05-05 10:00:00"), "%B")
     *   assert_equal 'June', @filters.date(Time.parse("2006-06-05 10:00:00"), "%B")
     *   assert_equal 'July', @filters.date(Time.parse("2006-07-05 10:00:00"), "%B")
     *
     *   assert_equal 'May', @filters.date("2006-05-05 10:00:00", "%B")
     *   assert_equal 'June', @filters.date("2006-06-05 10:00:00", "%B")
     *   assert_equal 'July', @filters.date("2006-07-05 10:00:00", "%B")
     *
     *   assert_equal '2006-07-05 10:00:00', @filters.date("2006-07-05 10:00:00", "")
     *   assert_equal '2006-07-05 10:00:00', @filters.date("2006-07-05 10:00:00", nil)
     *
     *   assert_equal '07/05/2006', @filters.date("2006-07-05 10:00:00", "%m/%d/%Y")
     *
     *   assert_equal "07/16/2004", @filters.date("Fri Jul 16 01:00:00 2004", "%m/%d/%Y")
     *
     *   assert_equal nil, @filters.date(nil, "%B")
     *
     *   assert_equal "07/05/2006", @filters.date(1152098955, "%m/%d/%Y")
     *   assert_equal "07/05/2006", @filters.date("1152098955", "%m/%d/%Y")
     * end
     */
    @Test
    public void applyOriginalTest() throws Exception {

        final Filter filter = Filter.getFilter("date");

        assertThat(filter.apply(seconds("2006-05-05 10:00:00"), "%B"), is((Object) "May"));
        assertThat(filter.apply(seconds("2006-06-05 10:00:00"), "%B"), is((Object) "June"));
        assertThat(filter.apply(seconds("2006-07-05 10:00:00"), "%B"), is((Object) "July"));

        assertThat(filter.apply("2006-05-05 10:00:00", "%B"), is((Object) "May"));
        assertThat(filter.apply("2006-06-05 10:00:00", "%B"), is((Object) "June"));
        assertThat(filter.apply("2006-07-05 10:00:00", "%B"), is((Object) "July"));

        assertThat(filter.apply("2006-07-05 10:00:00", ""), is((Object) "2006-07-05 10:00:00"));
        assertThat(filter.apply("2006-07-05 10:00:00", null), is((Object) "2006-07-05 10:00:00"));

        assertThat(filter.apply("2006-07-05 10:00:00", "%m/%d/%Y"), is((Object) "07/05/2006"));

        assertThat(filter.apply("Fri Jul 16 01:00:00 2004", "%m/%d/%Y"), is((Object) "07/16/2004"));

        assertThat(filter.apply(null, "%B"), is((Object) null));

        assertThat(filter.apply(1152098955, "%m/%d/%Y"), is((Object) "07/05/2006"));
        assertThat(filter.apply("1152098955", "%m/%d/%Y"), is((Object) "07/05/2006"));
    }
    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    private static long seconds(String str) throws Exception {
        long milliSeconds = formatter.parse(str).getTime();
        return milliSeconds / 1000L;
    }
}
