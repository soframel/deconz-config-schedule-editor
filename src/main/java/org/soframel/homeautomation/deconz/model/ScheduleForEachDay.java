package org.soframel.homeautomation.deconz.model;

import java.util.ArrayList;
import java.util.List;
/**
 * List for each day (starting with 0 = holidays, 1=monday....7=sunday) of the transition models
 */
public class ScheduleForEachDay {
    List<List<TransitionModel>> schedules;

    public ScheduleForEachDay(){
        schedules=new ArrayList<>(8);
        for(int i=0;i<8;i++){
            schedules.add(new ArrayList<>());        
        }
    }

    public void addTransitionsToDay(int day, List<TransitionModel> transitions){
        schedules.get(day).addAll(transitions);
    }

    public void orderTransitions(){
        for(List<TransitionModel> daySchedule: schedules){
            this.orderTransitionsForDay(daySchedule);
        }
    }

    public void orderTransitionsForDay(List<TransitionModel> transitions){
        transitions.sort((TransitionModel t1, TransitionModel t2) -> t1.time.compareTo(t2.time) );
    }

    public List<TransitionModel> getHolidaySchedules(){
        return schedules.get(0);
    }
    public List<TransitionModel> getMondaySchedules(){
        return schedules.get(1);
    }
    public List<TransitionModel> getTuesdaySchedules(){
        return schedules.get(2);
    }
    public List<TransitionModel> getWednesdaySchedules(){
        return schedules.get(3);
    }
    public List<TransitionModel> getThursdaySchedules(){
        return schedules.get(4);
    }
    public List<TransitionModel> getFridaySchedules(){
        return schedules.get(5);
    }
    public List<TransitionModel> getSaturdaySchedules(){
        return schedules.get(6);
    }
    public List<TransitionModel> getSundaySchedules(){
        return schedules.get(7);
    }
}
