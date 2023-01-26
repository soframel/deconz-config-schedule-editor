package org.soframel.homeautomation.deconz.model;

import java.util.List;
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

    /**
     * remove all entries  with temperature<=0
     */
    public void filterOutEmptyTemperatures(){        
        transitions=transitions.stream().filter((TransitionModel tr)-> {return tr.temperature>0;})
            .collect(Collectors.toList());
    }
}
