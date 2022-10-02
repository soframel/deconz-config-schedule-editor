package org.soframel.homeautomation.deconz.dto;

public class Transition {
    private int heatsetpoint;
    private String localtime;

    public Transition() {}

    public Transition(int heatsetpoint, String localtime) {
        this.heatsetpoint = heatsetpoint;
        this.localtime = localtime;
    }
    public int getHeatsetpoint() {
        return heatsetpoint;
    }
    public void setHeatsetpoint(int heatsetpoint) {
        this.heatsetpoint = heatsetpoint;
    }
    public String getLocaltime() {
        return localtime;
    }
    public void setLocaltime(String localtime) {
        this.localtime = localtime;
    }
    @Override
    public String toString() {
        return "Transition [heatsetpoint=" + heatsetpoint + ", localtime=" + localtime + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + heatsetpoint;
        result = prime * result + ((localtime == null) ? 0 : localtime.hashCode());
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
        Transition other = (Transition) obj;
        if (heatsetpoint != other.heatsetpoint)
            return false;
        if (localtime == null) {
            if (other.localtime != null)
                return false;
        } else if (!localtime.equals(other.localtime))
            return false;
        return true;
    }

    
}
