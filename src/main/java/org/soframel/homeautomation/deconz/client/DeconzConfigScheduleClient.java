package org.soframel.homeautomation.deconz.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.soframel.homeautomation.deconz.SchedulerException;
import org.soframel.homeautomation.deconz.dto.Sensor;
import org.soframel.homeautomation.deconz.dto.Transition;
import org.soframel.homeautomation.deconz.model.Day;
import org.soframel.homeautomation.deconz.model.TransitionModel;

/**
 * REST client class
 */
@ApplicationScoped
public class DeconzConfigScheduleClient implements DeconzConfigScheduleClientInterface{
    private static Logger logger = Logger.getLogger(DeconzConfigScheduleClient.class.getName());

    private ResteasyClient client;
    private String sensorURL;
    WebTarget webTarget;

    public DeconzConfigScheduleClient() {
        this(ConfigProvider.getConfig().getValue("url", String.class),
                ConfigProvider.getConfig().getValue("key", String.class));
    }

    public DeconzConfigScheduleClient(String apiUrl, String apiKey) {
        this.sensorURL = apiUrl + "/api/" + apiKey + "/sensors/";
        logger.info("DeconzConfigScheduleClient, sensor URL=" + sensorURL);

        client = (ResteasyClient) ClientBuilder.newClient();
        webTarget = client.target(sensorURL);
    }

    public void close() {
        client.close();
    }

    public Map<Day, List<TransitionModel>> getAllSchedules(String sensorId) {

        Invocation.Builder invocationBuilder = webTarget.path(sensorId).request(MediaType.APPLICATION_JSON);
        Sensor s = invocationBuilder.get(Sensor.class);
        logger.info("sensor=" + s);

        Map<String, List<Transition>> schedulesJSON = s.getConfig().getSchedule();
        if(schedulesJSON==null){
            schedulesJSON=new HashMap<String,List<Transition>>();
        }
        Map<Day, List<TransitionModel>> schedules=new HashMap<Day, List<TransitionModel>>();
        for(String time: schedulesJSON.keySet()){
            //covert transitions
            List<Transition> transitions=schedulesJSON.get(time);
            if(logger.isLoggable(Level.FINE)){
                logger.fine("For schedule "+time+": transitions: "+transitions);
            }
            List<TransitionModel> models=transitions.stream().map((Transition t) -> {
                return TransitionModel.parse(t);
            }).collect(Collectors.toList());

            //convert day
            //there may be more than 1 day in one Transition
            List<Day> days=Day.parse(time);            
            for(Day day: days){
                if(schedules.containsKey(day)){
                    List<TransitionModel> previousModels=schedules.get(day);
                    previousModels.addAll(models);
                }
                else{
                    schedules.put(day, models);
                }
            }
        }


        if(logger.isLoggable(Level.FINE)){
            for(Day day: schedules.keySet()){
                List<TransitionModel> transitions=schedules.get(day);
                logger.fine("Interpreted: For day "+day+": transitions: "+transitions);
            }
        }

        return schedules;
    }

    public void deleteAllSchedules(String sensorId) throws SchedulerException {
        Map<Day, List<TransitionModel>> schedules = this.getAllSchedules(sensorId);
        for (Day schedule : schedules.keySet()) {
            logger.info("deleting schedule=" + schedule);
            this.deleteSchedule(sensorId, schedule);
        }
    }

    public void deleteSchedule(String sensorId, Day schedule) throws SchedulerException {
        String bitmap = schedule.toBitmapString();
        Response r = webTarget.path(sensorId + "/config/schedule/" + bitmap).request().delete();
        if (r.getStatus() == 200) {
            logger.info("Deleted schedule " + bitmap);
        } 
        else if(r.getStatus()==404){
            //not important, didnt exist -> only log
            logger.info("Schedule did not exist");
        }
        else {
            String message = "An error occured while deleting " + bitmap + ": " + r.getStatus() + ", "
                    + r.getStatusInfo();
            logger.info(message);
            throw new SchedulerException(message);
        }
    }

    public void createSchedule(String sensorId, Day schedule, List<TransitionModel> transitions)
            throws SchedulerException {
        if (transitions.size() == 0) {
            logger.info("nothing to create for " + schedule);
        } else {
            logger.info("for schedule " + schedule + ", creating transitions " + transitions);
            String bitmap = schedule.toBitmapString();
            Invocation.Builder invocationBuilder = webTarget.path(sensorId + "/config/schedule/" + bitmap)
                    .request(MediaType.APPLICATION_JSON);
            List<Transition> list = transitions.stream()
                    .map((TransitionModel t) -> {
                        return new Transition(t.getTemperature(), TransitionModel.formatter.format(t.getTime()));
                    })
                    .collect(Collectors.toList());
            Response r = invocationBuilder.post(Entity.entity(list, MediaType.APPLICATION_JSON));
            if (r.getStatus() == 200) {
                logger.info("Schedule created for " + bitmap);
            } else {
                String message = "An error occured while creating " + bitmap + ": " + r.getStatus() + ", "
                        + r.getStatusInfo();
                logger.info(message);
                throw new SchedulerException(message);
            }
        }
    }
}
