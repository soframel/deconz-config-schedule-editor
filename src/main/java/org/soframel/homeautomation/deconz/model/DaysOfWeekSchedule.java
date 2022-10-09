package org.soframel.homeautomation.deconz.model;

import java.util.BitSet;

/**
 * schedule for one or multiple days of week for deCONZ API.
 */
public class DaysOfWeekSchedule {
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;
    // holiday is different, because it overrides the others (and its bitmap value
    // is 0!)
    private boolean holidays;

    public DaysOfWeekSchedule() {
    }

    public DaysOfWeekSchedule(boolean holidays) {
        this.holidays=holidays;
    }

    public DaysOfWeekSchedule(boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday,
            boolean saturday, boolean sunday) {
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
        this.holidays=false;
    }

    public int toBitmap() {
        if (holidays) {
            return 0;
        } else {
            BitSet bits = new BitSet(7);
            bits.set(0, sunday);
            bits.set(1, saturday);
            bits.set(2, friday);
            bits.set(3, thursday);
            bits.set(4, wednesday);
            bits.set(5, tuesday);
            bits.set(6, monday);
            byte[] bytes = bits.toByteArray();
            return bytes[0];
        }
    }
    public String toBitmapString(){
        return "W"+Integer.toString(this.toBitmap());
    }

    public static DaysOfWeekSchedule parse(String s) {
        if (s != null && s.length() > 0) {
            DaysOfWeekSchedule schedule = new DaysOfWeekSchedule();

            if (s.length() == 2 && s.equals("W0")) {
                schedule.setHolidays(true);
            } else {
                // remove W
                String decimalString = s.substring(1);
                Integer i = Integer.parseInt(decimalString);
                String binaryString = Integer.toBinaryString(i);
                int length=binaryString.length();
                schedule.setSunday(getBooleanForPosition(binaryString, length-1));
                schedule.setSaturday(getBooleanForPosition(binaryString, length-2));
                schedule.setFriday(getBooleanForPosition(binaryString, length-3));
                schedule.setThursday(getBooleanForPosition(binaryString, length-4));
                schedule.setWednesday(getBooleanForPosition(binaryString, length-5));
                schedule.setTuesday(getBooleanForPosition(binaryString, length-6));
                schedule.setMonday(getBooleanForPosition(binaryString, length-7));
            }

            return schedule;
        } else {
            return null;
        }
    }

    protected static boolean getBooleanForPosition(String binaryString, int position) {
        if(position>=0 && binaryString.length()>position){
            return "1".equals(binaryString.substring(position, position + 1));
        }
        else{
            return false;
        }
    }

    public boolean isMonday() {
        return monday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }

    public boolean isThursday() {
        return thursday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    public boolean isFriday() {
        return friday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    public boolean isSaturday() {
        return saturday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }

    public boolean isSunday() {
        return sunday;
    }

    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }

    public boolean isHolidays() {
        return holidays;
    }

    public void setHolidays(boolean holidays) {
        this.holidays = holidays;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (monday ? 1231 : 1237);
        result = prime * result + (tuesday ? 1231 : 1237);
        result = prime * result + (wednesday ? 1231 : 1237);
        result = prime * result + (thursday ? 1231 : 1237);
        result = prime * result + (friday ? 1231 : 1237);
        result = prime * result + (saturday ? 1231 : 1237);
        result = prime * result + (sunday ? 1231 : 1237);
        result = prime * result + (holidays ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DaysOfWeekSchedule other = (DaysOfWeekSchedule) obj;
        if (monday != other.monday)
            return false;
        if (tuesday != other.tuesday)
            return false;
        if (wednesday != other.wednesday)
            return false;
        if (thursday != other.thursday)
            return false;
        if (friday != other.friday)
            return false;
        if (saturday != other.saturday)
            return false;
        if (sunday != other.sunday)
            return false;
        if (holidays != other.holidays)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DaysOfWeekSchedule [monday=" + monday + ", tuesday=" + tuesday + ", wednesday=" + wednesday
                + ", thursday=" + thursday + ", friday=" + friday + ", saturday=" + saturday + ", sunday=" + sunday
                + ", holidays=" + holidays + "]";
    }

}
