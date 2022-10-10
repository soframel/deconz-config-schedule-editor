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
 * mvn org.codehaus.mojo:exec-maven-plugin:java (adapt arguments in the pom.xml, maven-exec-plugin)
 */
public class DeconzConfigScheduleEditorMain {

    public static void main(String[] args) {
        try (FileReader reader = new FileReader("local.properties")) {
            Properties props = new Properties();
            props.load(reader);
            String url = props.getProperty("url");
            String key = props.getProperty("key");
            String sensorId = props.getProperty("sensorid");

            DeconzConfigScheduleClient client = new DeconzConfigScheduleClient(url, key, sensorId);
            DeconzConfigScheduleEditorMain editor = new DeconzConfigScheduleEditorMain(client);            
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

        } catch (IOException e) {
            System.out.println("an exception occured: "+e.getMessage());
            e.printStackTrace();
        }
    }

    DeconzConfigScheduleClient client;

    public DeconzConfigScheduleEditorMain(DeconzConfigScheduleClient client) {
        this.client = client;
    }

    public void printAllSchedules() {
        Map<DaysOfWeekSchedule, List<TransitionModel>> schedules = client.getAllSchedules();
        for (DaysOfWeekSchedule schedule : schedules.keySet()) {
            System.out.println("For schedule=" + schedule);
            for(TransitionModel tr: schedules.get(schedule)){
                System.out.println("transitions: "+tr);
            }
        }
    }

    public void deleteAllSchedules() {
        Map<DaysOfWeekSchedule, List<TransitionModel>> schedules = client.getAllSchedules();
        for (DaysOfWeekSchedule schedule : schedules.keySet()) {
            System.out.println("deleting schedule=" + schedule);
            client.deleteSchedule(schedule);
        }
    }

    /**
     * Schedules to be created
     */
    public void createWeekSchedules() {
        DaysOfWeekSchedule scheduleSemaine = new DaysOfWeekSchedule(true, true, true, true, true, false, false);
        TransitionModel matinSemaine = new TransitionModel(LocalTime.of(7, 00), 1700);
        TransitionModel soirSemaine = new TransitionModel(LocalTime.of(21, 00), 1800);
        client.createSchedule(scheduleSemaine, matinSemaine, soirSemaine);

        DaysOfWeekSchedule scheduleWE = new DaysOfWeekSchedule(false, false, false, false, false, true, true);
        TransitionModel matinWE = new TransitionModel(LocalTime.of(9, 00), 1700);
        TransitionModel soirWE = new TransitionModel(LocalTime.of(21, 00), 1800);
        client.createSchedule(scheduleWE, matinWE, soirWE);
    }

}
