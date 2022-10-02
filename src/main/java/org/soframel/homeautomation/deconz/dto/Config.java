package org.soframel.homeautomation.deconz.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private boolean schedule_on;
    private Map<String,List<Transition>> schedule;
    public boolean isSchedule_on() {
        return schedule_on;
    }
    public void setSchedule_on(boolean schedule_on) {
        this.schedule_on = schedule_on;
    }
    public Map<String,List<Transition>> getSchedule() {
        return schedule;
    }
    public void setSchedule(Map<String,List<Transition>> schedule) {
        this.schedule = schedule;
    }
    @Override
    public String toString() {
        return "Config [schedule_on=" + schedule_on + ", schedule=" + schedule + "]";
    } 

    
}
