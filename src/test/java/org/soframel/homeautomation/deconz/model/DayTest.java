package org.soframel.homeautomation.deconz.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;


public class DayTest {
    @Test
    public void testToBitmap(){

        Day schedule=new Day(6);
        assertEquals(1, schedule.toBitmap());
        schedule=new Day(4);
        assertEquals(4, schedule.toBitmap());
        schedule=new Day(0);
        assertEquals(64, schedule.toBitmap());
        schedule=new Day(7);
        assertEquals(0, schedule.toBitmap());
    }

    @Test
    public void testParse(){
        List<Day> schedules=Day.parse("W2");
        assertEquals(1, schedules.size());
        assertEquals(5, schedules.get(0).index);

        schedules=Day.parse("W65");
        assertEquals(2, schedules.size());
        assertTrue(schedules.contains(new Day(0)));
        assertTrue(schedules.contains(new Day(6)));

        schedules=Day.parse("W0");
        assertEquals(1, schedules.size());
        assertEquals(7, schedules.get(0).index);
    }
}
