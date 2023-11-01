package org.soframel.homeautomation.deconz.model;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class DaySchedule {
    List<TransitionModel> transitions;
    Day day;
    
    public List<TransitionModel> getTransitions() {
        return transitions;
    }
    public Day getDay() {
        return day;
    }
    public void setTransitions(List<TransitionModel> transitions) {
        this.transitions = transitions;
    }
    public void setDay(Day day) {
        this.day = day;
    }
    public DaySchedule(List<TransitionModel> transitions, Day day) {
        this.transitions = transitions;
        this.day = day;
    }
    public DaySchedule() {
    }
    @Override
    public String toString() {
        return "DaySchedule [ day=" + day + ", transitions=" + transitions+"]";
    }

    //transitions must be ordered, adapt transitions list so that they are
    public void orderTransitions(){
        Comparator<TransitionModel> comp=new Comparator<TransitionModel>() {
            public int compare(TransitionModel t1, TransitionModel t2){
                if(t1==null){
                    return -1;
                }
                else if(t2==null){
                    return 1;
                }
                else {
                    return t1.time.compareTo(t2.time);
                }
            }
        };
        transitions.sort(comp);
    }

    /**
     * remove all entries  with temperature<=0
     */
    public void filterOutEmptyTemperatures(){        
        transitions=transitions.stream().filter((TransitionModel tr)-> {return tr.temperature>0;})
            .collect(Collectors.toList());
    }
}
