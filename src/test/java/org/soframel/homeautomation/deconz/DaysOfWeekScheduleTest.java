package org.soframel.homeautomation.deconz;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.soframel.homeautomation.deconz.model.DaysOfWeekSchedule;

public class DaysOfWeekScheduleTest {
    @Test
    public void testToBitmap(){

        DaysOfWeekSchedule schedule=new DaysOfWeekSchedule(false, false, false, false, false, false, true, false);
        assertEquals(1, schedule.toBitmap());
        schedule.setFriday(true);
        assertEquals(5, schedule.toBitmap());
        schedule.setSunday(false);
        schedule.setMonday(true);
        assertEquals(68, schedule.toBitmap());

        schedule.setHolidays(true);
        assertEquals(0, schedule.toBitmap());
    }

    @Test
    public void testParse(){
        DaysOfWeekSchedule schedule=DaysOfWeekSchedule.parse("W2");
        assertEquals(new DaysOfWeekSchedule(false, false, false, false, false, true, false, false), schedule);

        schedule=DaysOfWeekSchedule.parse("W65");
        assertEquals(new DaysOfWeekSchedule(true, false, false, false, false, false, true, false), schedule);

        schedule=DaysOfWeekSchedule.parse("W0");
        assertEquals(new DaysOfWeekSchedule(false, false, false, false, false, false, false, true), schedule);
    }
}
