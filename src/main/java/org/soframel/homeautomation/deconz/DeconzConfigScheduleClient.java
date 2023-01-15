package org.soframel.homeautomation.deconz;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.soframel.homeautomation.deconz.dto.Sensor;
import org.soframel.homeautomation.deconz.dto.Transition;
import org.soframel.homeautomation.deconz.model.DaysOfWeekSchedule;
import org.soframel.homeautomation.deconz.model.TransitionModel;

/**
 * REST client class
 */
@ApplicationScoped
public class DeconzConfigScheduleClient {
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

    public Map<DaysOfWeekSchedule, List<TransitionModel>> getAllSchedules(String sensorId) {

        Invocation.Builder invocationBuilder = webTarget.path(sensorId).request(MediaType.APPLICATION_JSON);
        Sensor s = invocationBuilder.get(Sensor.class);
        logger.info("sensor=" + s);

        Map<String, List<Transition>> schedulesJSON = s.getConfig().getSchedule();

        if(logger.isLoggable(Level.FINE)){
            for(String time: schedulesJSON.keySet()){
                List<Transition> transitions=schedulesJSON.get(time);
                logger.fine("For schedule "+time+": transitions: "+transitions);
            }
        }

        Map<DaysOfWeekSchedule, List<TransitionModel>> schedules= schedulesJSON.keySet().stream()
                .collect(Collectors.toMap(
                        DaysOfWeekSchedule::parse,
                        ((String schedule) -> {
                            return schedulesJSON.get(schedule).stream().map((Transition t) -> {
                                return TransitionModel.parse(t);
                            })
                                    .collect(Collectors.toList());
                        })));

        if(logger.isLoggable(Level.FINE)){
            for(DaysOfWeekSchedule schedule: schedules.keySet()){
                List<TransitionModel> transitions=schedules.get(schedule);
                logger.fine("Interpreted: For schedule "+schedule+": transitions: "+transitions);
            }
        }

        return schedules;
    }

    public void deleteAllSchedules(String sensorId) throws SchedulerException {
        Map<DaysOfWeekSchedule, List<TransitionModel>> schedules = this.getAllSchedules(sensorId);
        for (DaysOfWeekSchedule schedule : schedules.keySet()) {
            logger.info("deleting schedule=" + schedule);
            this.deleteSchedule(sensorId, schedule);
        }
    }

    public void deleteSchedule(String sensorId, DaysOfWeekSchedule schedule) throws SchedulerException {
        String bitmap = schedule.toBitmapString();
        Response r = webTarget.path(sensorId + "/config/schedule/" + bitmap).request().delete();
        if (r.getStatus() == 200) {
            logger.info("Deleted schedule " + bitmap);
        } else {
            String message = "An error occured while deleting " + bitmap + ": " + r.getStatus() + ", "
                    + r.getStatusInfo();
            logger.info(message);
            throw new SchedulerException(message);
        }
    }

    public void createSchedule(String sensorId, DaysOfWeekSchedule schedule, TransitionModel... transitions)
            throws SchedulerException {
        if (transitions.length == 0) {
            logger.info("nothing to create for " + schedule);
        } else {
            logger.info("for schedule " + schedule + ", creating transitions " + Arrays.deepToString(transitions));
            String bitmap = schedule.toBitmapString();
            Invocation.Builder invocationBuilder = webTarget.path(sensorId + "/config/schedule/" + bitmap)
                    .request(MediaType.APPLICATION_JSON);
            List<Transition> list = Arrays.asList(transitions).stream()
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
