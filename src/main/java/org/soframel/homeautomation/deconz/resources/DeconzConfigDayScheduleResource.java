package org.soframel.homeautomation.deconz.resources;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.soframel.homeautomation.deconz.SchedulerException;
import org.soframel.homeautomation.deconz.client.DeconzConfigScheduleClientInterface;
import org.soframel.homeautomation.deconz.client.MockConfigScheduleClient;
import org.soframel.homeautomation.deconz.model.Day;
import org.soframel.homeautomation.deconz.model.DaySchedule;
import org.soframel.homeautomation.deconz.model.TransitionModel;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

@Path("/")
@ApplicationScoped
public class DeconzConfigDayScheduleResource {
    private static Logger logger = Logger.getLogger(DeconzConfigDayScheduleResource.class.getName());

    @ConfigProperty(name = "thermostat") 
    Map<String,String> thermostats;

    @Inject
    DeconzConfigScheduleClientInterface client;
    //for tests: comment @Inject and add =new MockConfigScheduleClient()

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance main(Map<String,String> thermostats, String id, DaySchedule[] schedules);
        public static native TemplateInstance schedule(Map<String,String> thermostats, String id, DaySchedule schedule);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@DefaultValue("") @QueryParam("id") String sensorid) {
        logger.info("getting schedules");
        
        DaySchedule[] schedules=new DaySchedule[8];
        if(sensorid!=null && !sensorid.equals("")){
            Map<Day, List<TransitionModel>> data =client.getAllSchedules(sensorid);
            
            for(Day day: data.keySet()){
                List<TransitionModel> trans=data.get(day);
                DaySchedule schedule=new DaySchedule(trans, day);
                schedules[day.getIndex()]=schedule;
            }            
        }
        //the fill missing days
        for(int i=0;i<=7;i++){
            if(schedules[i]==null){
                schedules[i]=new DaySchedule(new ArrayList<TransitionModel>(), new Day(i));
            }
            logger.fine("schedule["+i+"]: "+schedules[i]);
        }

        return Templates.main(thermostats,sensorid, schedules);        
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance saveSchedule(
            MultivaluedMap<String, String> form) throws SchedulerException {
        logger.info("saving schedule: " + form);
        //Transform schedule data
        DaySchedule schedule=new DaySchedule();
        schedule.setTransitions(new ArrayList<TransitionModel>());
        String thermostatId="";
        int day=-1;
        for (String key : form.keySet()) {
            if("id".equals(key)){
                thermostatId=form.getFirst(key);
            }
            else if("day".equals(key)){
                day=Integer.parseInt(form.getFirst(key));
            }
            else{
                List<String> dataList = form.get(key);
                // we only have one data per key in our case
                if (dataList.size() > 0) {
                    String data = dataList.get(0);
                    this.addDataToSchedule(schedule, key, data);
                }
            }
        }
        schedule.setDay(new Day(day));
        //then filter out all TransitionModel with no temperature=deletes
        schedule.filterOutEmptyTemperatures();

        // save thermostat schedule
        client.deleteSchedule(thermostatId, schedule.getDay());
        this.sleep(1000);
        client.createSchedule(thermostatId, schedule.getDay(), schedule.getTransitions());
        this.sleep(3000);
        logger.info("schedule saved for day "+schedule.getDay()+" for thermostat "+thermostatId);

        //return main page, reloaded
        return this.get(thermostatId);
    }

    private void sleep(int milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            //nothing
        }
    }

    private void addDataToSchedule(DaySchedule schedule, String key, String data) throws SchedulerException {
        if (data != null && !data.equals("")) {
            logger.info("parsing param "+key+", data="+data);
            StringTokenizer tokenizer = new StringTokenizer(key, "-", false);            
            int index = -1;
            try {
                index = Integer.parseInt(tokenizer.nextToken());
            } catch (NumberFormatException e) {
                throw new SchedulerException("Could not parse entry index in " + key);
            }
            String nextToken=tokenizer.nextToken();
            boolean isTime = (nextToken.equals("time"));
            boolean isDelete = (nextToken.equals("delete"));
            
            TransitionModel trans =  this.getOrCreateTransition(schedule.getTransitions(), index);

            if (isTime) {
                // parse value
                try {
                    LocalTime time = LocalTime.parse(data);
                    trans.setTime(time);
                } catch (DateTimeParseException e) {
                    throw new SchedulerException("Could not parse time for entry " + key + ", time=" + data);
                }
            } 
            else if(isDelete){
                trans.setTemperature(-1);
            }
            else if(trans.getTemperature()>-1) { // temperature, only if -1 not already set (otherwise another form entry had a "delete")
                try {
                    int temp = Integer.parseInt(data);
                    trans.setTemperature(temp);
                } catch (NumberFormatException e) {
                    throw new SchedulerException(
                            "Could not parse temperature for entry " + key + ", temperature=" + data);
                }
            }
            
        }
    }

    private TransitionModel getOrCreateTransition(List<TransitionModel> list, int index) {
        if (list.size() > index) {
            return list.get(index);
        } else {
            while (list.size() < index + 1) {
                list.add(new TransitionModel());
            }
            return list.get(index);
        }
    }

}
