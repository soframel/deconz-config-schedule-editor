package org.soframel.homeautomation.deconz;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.soframel.homeautomation.deconz.dto.Sensor;
import org.soframel.homeautomation.deconz.dto.Transition;
import org.soframel.homeautomation.deconz.model.DaysOfWeekSchedule;
import org.soframel.homeautomation.deconz.model.TransitionModel;

/**
 * REST client class
 */
public class DeconzConfigScheduleClient {
    private String sensorURL;
    WebTarget webTarget;

    public DeconzConfigScheduleClient(String apiUrl, String apiKey, String sensorId) {
        this.sensorURL = apiUrl +"/api/" + apiKey + "/sensors/" + sensorId;
        System.out.println("DeconzConfigScheduleClient, sensor URL=" + sensorURL);

        ResteasyClient client = (ResteasyClient)ClientBuilder.newClient();
        webTarget = client.target(sensorURL);
    }

    public Map<DaysOfWeekSchedule, List<TransitionModel>> getAllSchedules(){
        
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        Sensor s=invocationBuilder.get(Sensor.class);
        System.out.println("sensor="+s);

        Map<String,List<Transition>> schedulesJSON=s.getConfig().getSchedule();

        return schedulesJSON.keySet().stream()
        .collect(Collectors.toMap(
            DaysOfWeekSchedule::parse, 
            ( (String schedule) -> { 
                return schedulesJSON.get(schedule).stream().
                map((Transition t) -> { return TransitionModel.parse(t); })
                .collect(Collectors.toList());
                }
                ))
        );
    }

    public void deleteSchedule(DaysOfWeekSchedule schedule){
            String bitmap=schedule.toBitmapString();
            Response r=webTarget.path("/config/schedule/"+bitmap).request().delete();
            if(r.getStatus()==200){
                System.out.println("Deleted schedule "+bitmap);
            }
            else{
                
                System.out.println("An error occured while deleting "+bitmap+": "+r.getStatus()+", "+r.readEntity(String.class));
            }
    }

    public void createSchedule(DaysOfWeekSchedule schedule, TransitionModel ... transitions){
        String bitmap=schedule.toBitmapString();
        Invocation.Builder invocationBuilder =  webTarget.path("/config/schedule/"+bitmap).request(MediaType.APPLICATION_JSON);
        List<Transition> list=Arrays.asList(transitions).stream()
        .map((TransitionModel t) -> {return new Transition(t.getTemperature(), TransitionModel.formatter.format(t.getTime()));})
        .toList();
        Response r=invocationBuilder.post(Entity.entity(list, MediaType.APPLICATION_JSON));
        if(r.getStatus()==200){
            System.out.println("Schedule created for "+bitmap);
        }
        else{
            System.out.println("An error occured while creating "+bitmap+": "+r);
        }
    }
}
