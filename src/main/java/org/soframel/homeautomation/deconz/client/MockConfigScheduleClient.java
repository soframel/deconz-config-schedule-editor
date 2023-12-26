package org.soframel.homeautomation.deconz.client;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.soframel.homeautomation.deconz.SchedulerException;
import org.soframel.homeautomation.deconz.dto.Sensor;
import org.soframel.homeautomation.deconz.model.Day;
import org.soframel.homeautomation.deconz.model.TransitionModel;

/**
 * simple in memory client for tests, not calling anything
 */
public class MockConfigScheduleClient implements DeconzConfigScheduleClientInterface{
    Logger logger=Logger.getLogger(MockConfigScheduleClient.class.getName());

    Map<Day, List<TransitionModel>> map = new HashMap<>();

    @Override
    public void close() {
        
    }

    public MockConfigScheduleClient(){
        Day s = new Day(0);
        TransitionModel t1 = new TransitionModel(LocalTime.of(7, 0), 1600);
        TransitionModel t2 = new TransitionModel(LocalTime.of(18, 0), 1800);
        map.put(s, List.of(t1, t2));

        Day s2 = new Day(1);
        TransitionModel t3 = new TransitionModel(LocalTime.of(9, 30), 2100);
        TransitionModel t4 = new TransitionModel(LocalTime.of(21, 0), 1800);
        map.put(s2, List.of(t3, t4));

        Day s3 = new Day(7);
        TransitionModel t5 = new TransitionModel(LocalTime.of(9, 30), 2100);
        TransitionModel t6 = new TransitionModel(LocalTime.of(21, 0), 1800);
        map.put(s3, List.of(t5, t6));
    }

    @Override
    public Map<Day, List<TransitionModel>> getAllSchedules(String sensorId) {
        return map;
    }

    @Override
    public void deleteAllSchedules(String sensorId) throws SchedulerException {
        map=new HashMap<>();
    }

    @Override
    public void deleteSchedule(String sensorId, Day schedule) throws SchedulerException {
        map.remove(schedule);
    }

    @Override
    public void createSchedule(String sensorId, Day schedule, List<TransitionModel> transitions)
            throws SchedulerException {
        map.put(schedule, transitions)        ;
    }

    @Override
    public void createScheduleRaw(String sensorId, String scheduleBitmap, List json) throws SchedulerException {
        logger.info(() -> String.format(">> replacing complete schedule for sensor %s and bitmap %s: \n %s", sensorId, scheduleBitmap, json));                
    }

    @Override
    public Sensor getSensorConfig(String sensodId) throws SchedulerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSensorConfig'");
    }
    
}
