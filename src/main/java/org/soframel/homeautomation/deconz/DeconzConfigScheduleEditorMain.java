package org.soframel.homeautomation.deconz;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.soframel.homeautomation.deconz.model.DaysOfWeekSchedule;
import org.soframel.homeautomation.deconz.model.TransitionModel;

/**
 * Main class to call the REST client to configure schedulers.
 * First set the properties in local.properties:
 * url (http://deconzHost:deconzPort)
 * key (your API Key)
 * sensorId
 * then call with either no parameters (will print existing schedules), or "delete" to delete all, and/or "create" to (re-)create the schedules:
 * mvn org.codehaus.mojo:exec-maven-plugin:java (adapt action arguments in the pom.xml, maven-exec-plugin, to only delete or create if needed)
 */
public class DeconzConfigScheduleEditorMain {

    public static void main(String[] args) {
        try (FileReader reader = new FileReader(".env")) {
            Properties props = new Properties();
            props.load(reader);
            String url = props.getProperty("URL");
            String key = props.getProperty("KEY");
            String sensorId = props.getProperty("SENSORID");

            DeconzConfigScheduleClient client = new DeconzConfigScheduleClient(url, key);
            DeconzConfigScheduleEditorMain editor = new DeconzConfigScheduleEditorMain(client, sensorId);            
            editor.printAllSchedules();

            List<String> argsList=Arrays.asList(args);
            if(argsList.contains("delete")){
                System.out.println("Deleting existing schedules");
                editor.deleteAllSchedules();
            }
            if(argsList.contains("create")){
                System.out.println("Creating schedules");
                editor.createWeekSchedules();
            }

        } catch (Exception e) {
            System.out.println("an exception occured: "+e.getMessage());
            e.printStackTrace();
        }
    }

    DeconzConfigScheduleClient client;
    String sensorId;

    public DeconzConfigScheduleEditorMain(DeconzConfigScheduleClient client, String sensorId) {
        this.client = client;
        this.sensorId=sensorId;
    }

    public void printAllSchedules() {
        Map<DaysOfWeekSchedule, List<TransitionModel>> schedules = client.getAllSchedules(sensorId);
        for (DaysOfWeekSchedule schedule : schedules.keySet()) {
            System.out.println("For schedule=" + schedule);
            for(TransitionModel tr: schedules.get(schedule)){
                System.out.println("transitions: "+tr);
            }
        }
    }

    public void deleteAllSchedules() throws SchedulerException {
        client.deleteAllSchedules(sensorId);
    }

    /**
     * Schedules to be created
     * @throws SchedulerException
     */
    public void createWeekSchedules() throws SchedulerException {
        DaysOfWeekSchedule scheduleSemaine = new DaysOfWeekSchedule(true, true, true, true, true, false, false);
        TransitionModel matinSemaine = new TransitionModel(LocalTime.of(7, 00), 1700);
        TransitionModel soirSemaine = new TransitionModel(LocalTime.of(21, 00), 1800);
        client.createSchedule(sensorId, scheduleSemaine, matinSemaine, soirSemaine);

        DaysOfWeekSchedule scheduleWE = new DaysOfWeekSchedule(false, false, false, false, false, true, true);
        TransitionModel matinWE = new TransitionModel(LocalTime.of(9, 00), 1700);
        TransitionModel soirWE = new TransitionModel(LocalTime.of(21, 00), 1800);
        client.createSchedule(sensorId, scheduleWE, matinWE, soirWE);
    }

}
