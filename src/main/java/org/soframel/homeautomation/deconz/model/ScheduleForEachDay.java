package org.soframel.homeautomation.deconz.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    public static ScheduleForEachDay parseFromScheduleMap(Map<DaysOfWeekSchedule, List<TransitionModel>> schedule){
        ScheduleForEachDay result=new ScheduleForEachDay();

        for(DaysOfWeekSchedule s: schedule.keySet()){
            if(s.isHolidays()){
                result.addTransitionsToDay(0, schedule.get(s));
            }
            if(s.isMonday()){
                result.addTransitionsToDay(1, schedule.get(s));
            }
            if(s.isTuesday()){
                result.addTransitionsToDay(2, schedule.get(s));
            }
            if(s.isWednesday()){
                result.addTransitionsToDay(3, schedule.get(s));
            }
            if(s.isThursday()){
                result.addTransitionsToDay(4, schedule.get(s));
            }
            if(s.isFriday()){
                result.addTransitionsToDay(5, schedule.get(s));
            }
            if(s.isSaturday()){
                result.addTransitionsToDay(6, schedule.get(s));
            }    
            if(s.isSunday()){
                result.addTransitionsToDay(7, schedule.get(s));
            }                                                                                    
        }
        result.orderTransitions();
        return result;
    }   
    
    public DaysOfWeekSchedule getDayOfWeekSchedule(int index){        
            if(index==0){
                return new DaysOfWeekSchedule(true);
            }
            else if(index==1){
                return new DaysOfWeekSchedule(true, false, false, false, false, false, false);
            }
            else if(index==2){
                return new DaysOfWeekSchedule(false, true, false, false, false, false, false);
            }
            else if(index==3){
                return new DaysOfWeekSchedule(false, false, true, false, false, false, false);
            }
            else if(index==4){
                return new DaysOfWeekSchedule(false, false, false, true, false, false, false);
            }
            else if(index==5){
                return new DaysOfWeekSchedule(false, false, false, false, true, false, false);
            }
            else if(index==6){
                return new DaysOfWeekSchedule(false, false, false, false, false, true, false);
            }
            else if(index==7){
                return new DaysOfWeekSchedule(false, false, false, false, false, false, true);
            }   
            else{
                throw new IllegalArgumentException("invalid index for schedule in ScheduleForEachDay: "+index);
            }
    }

    public Map<DaysOfWeekSchedule, List<TransitionModel>> getDayOfWeekSchedules(){
        Map<DaysOfWeekSchedule, List<TransitionModel>> map=new HashMap<DaysOfWeekSchedule, List<TransitionModel>>();

        for(int i=0; i<schedules.size();i++){
            DaysOfWeekSchedule schedule=this.getDayOfWeekSchedule(i);
            List<TransitionModel> transitions=schedules.get(i);
            map.put(schedule, transitions);
        }

        return map;
    }

    /**
     * remove all entries for all days of week with temperature<=0
     */
    public void filterOutEmptyTemperatures(){        
        schedules=schedules.stream().map((List<TransitionModel> list) -> {
            return list.stream().filter((TransitionModel tr)-> {return tr.temperature>0;})
            .collect(Collectors.toList());
        }).collect(Collectors.toList());
    }
}
