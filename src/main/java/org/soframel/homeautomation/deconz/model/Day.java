package org.soframel.homeautomation.deconz.model;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * schedule for one day of the week for deCONZ API.
 */
public class Day {
    /**
     * index starts on 0=monday -> 7: holidays
     */
    int index;

    public Day(int index) {
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

    public int toBitmap() {
        if (index==7) {
            return 0;
        } else {
            BitSet bits = new BitSet(7);
            bits.clear();
            //only set the concerned day to true
            bits.set(6-index, true);
            byte[] bytes = bits.toByteArray();
            return bytes[0];
        }
    }

    public String toBitmapString() {
        return "W" + Integer.toString(this.toBitmap());
    }

    public static List<Day> parse(String s) {
        if (s != null && s.length() > 0) {

            ArrayList list = new ArrayList<>();

            if (s.equals("W0")) {
                list.add(new Day(7));
            } else {
                // remove W
                String decimalString = s.substring(1);
                Integer i = Integer.parseInt(decimalString);
                String binaryString = Integer.toBinaryString(i);
                // binaryString contains 8 positions,
                int length = binaryString.length();

                for (int position = length - 1; position >= 0; position--) {
                    if (Day.getBooleanForPosition(binaryString, position)) {
                        list.add(new Day(7 - length + position));
                    }
                }
            }
            return list;
        } else {
            return null;
        }
    }

    protected static boolean getBooleanForPosition(String binaryString, int position) {
        if (position >= 0 && binaryString.length() > position) {
            return "1".equals(binaryString.substring(position, position + 1));
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        switch(index){
            case 0: return "Monday";
            case 1 : return "Tuesday";
            case 2 : return "Wednesday";
            case 3 : return "Thursday";
            case 4 : return "Friday";
            case 5 : return "Saturday";
            case 6 : return "Sunday";
            case 7 : return "Holidays";
            default : return "UNKNOWN "+index;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + index;
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
        Day other = (Day) obj;
        if (index != other.index)
            return false;
        return true;
    }

    
}
