package org.soframel.homeautomation.deconz.client;

import java.util.List;
import java.util.Map;

import org.soframel.homeautomation.deconz.SchedulerException;
import org.soframel.homeautomation.deconz.model.Day;
import org.soframel.homeautomation.deconz.model.TransitionModel;

public interface DeconzConfigScheduleClientInterface {

    public void close();

    public Map<Day, List<TransitionModel>> getAllSchedules(String sensorId);

    public void deleteAllSchedules(String sensorId) throws SchedulerException;

    public void deleteSchedule(String sensorId, Day schedule) throws SchedulerException;

    public void createSchedule(String sensorId, Day schedule, List<TransitionModel> transitions)
            throws SchedulerException;
}
