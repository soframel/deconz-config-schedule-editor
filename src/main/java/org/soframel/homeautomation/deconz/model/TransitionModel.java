package org.soframel.homeautomation.deconz.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.soframel.homeautomation.deconz.dto.Transition;

public class TransitionModel {
    public LocalTime time;
    public int temperature;

    public final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'T'HH:mm");
    
    public TransitionModel(){}
    public TransitionModel(LocalTime time, int temperature) {
        this.time = time;
        this.temperature = temperature;
    }
    public static TransitionModel parse(Transition t){
        TransitionModel model= new TransitionModel ();
        model.setTemperature(t.getHeatsetpoint());
        model.setTime(LocalTime.parse(t.getLocaltime(),formatter));
        return model;
    }

    public LocalTime getTime() {
        return time;
    }
    public void setTime(LocalTime time) {
        this.time = time;
    }
    public int getTemperature() {
        return temperature;
    }
    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        result = prime * result + temperature;
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
        TransitionModel other = (TransitionModel) obj;
        if (time == null) {
            if (other.time != null)
                return false;
        } else if (!time.equals(other.time))
            return false;
        if (temperature != other.temperature)
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "TransitionModel [time=" + time + ", temperature=" + temperature + "]";
    }

}
