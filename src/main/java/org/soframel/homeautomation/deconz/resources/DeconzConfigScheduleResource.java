package org.soframel.homeautomation.deconz.resources;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.soframel.homeautomation.deconz.DeconzConfigScheduleClient;
import org.soframel.homeautomation.deconz.SchedulerException;
import org.soframel.homeautomation.deconz.model.DaysOfWeekSchedule;
import org.soframel.homeautomation.deconz.model.ScheduleForEachDay;
import org.soframel.homeautomation.deconz.model.TransitionModel;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

@Path("/")
@ApplicationScoped
public class DeconzConfigScheduleResource {
    private static Logger logger = Logger.getLogger(DeconzConfigScheduleResource.class.getName());

    @ConfigProperty(name = "thermostats") 
    Map<String,String> thermostats;

    @Inject
    DeconzConfigScheduleClient client;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance schedules(Map<String,String> thermostats, String name, ScheduleForEachDay schedules);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@DefaultValue("") @QueryParam("name") String name) {
        ScheduleForEachDay schedules=null;
        if(name!=null && !name.equals("")){
            Map<DaysOfWeekSchedule, List<TransitionModel>> data =client.getAllSchedules(thermostats.get(name));
            schedules=ScheduleForEachDay.parseFromScheduleMap(data);
        }
        return Templates.schedules(thermostats,name,schedules);        
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance saveSchedule(
            MultivaluedMap<String, String> form) throws SchedulerException {
        logger.info("saving schedule: " + form);
        //Transform schedule data
        ScheduleForEachDay schedule = new ScheduleForEachDay();
        String thermostat="";
        String name="";
        for (String key : form.keySet()) {
            if("name".equals(key)){
                name=form.getFirst(key);
                thermostat=thermostats.get(name);
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
        //then filter out all TransitionModel with no temperature=deletes
        schedule.filterOutEmptyTemperatures();

        // save thermostat schedule
        client.deleteAllSchedules(thermostat);
        Map<DaysOfWeekSchedule, List<TransitionModel>> formattedSchedules=schedule.getDayOfWeekSchedules();
        for(DaysOfWeekSchedule s: formattedSchedules.keySet()){
            client.createSchedule(thermostat, s, formattedSchedules.get(s).toArray(new TransitionModel[0]));
        }
        logger.info("schedules saved for thermostat "+thermostat);

        //return page
        return Templates.schedules(thermostats, name, schedule);
    }

    private void addDataToSchedule(ScheduleForEachDay schedule, String key, String data) throws SchedulerException {
        if (data != null && !data.equals("")) {
            logger.info("parsing param "+key+", data="+data);
            StringTokenizer tokenizer = new StringTokenizer(key, "-", false);
            String day = tokenizer.nextToken();
            int index = -1;
            try {
                index = Integer.parseInt(tokenizer.nextToken());
            } catch (NumberFormatException e) {
                throw new SchedulerException("Could not parse entry index in " + key);
            }
            String nextToken=tokenizer.nextToken();
            boolean isTime = (nextToken.equals("time"));
            boolean isDelete = (nextToken.equals("delete"));

            TransitionModel trans = switch (day) {
                case "holidays" -> this.getOrCreateTransition(schedule.getHolidaySchedules(), index);
                case "monday" -> this.getOrCreateTransition(schedule.getMondaySchedules(), index);
                case "tuesday" -> this.getOrCreateTransition(schedule.getTuesdaySchedules(), index);
                case "wednesday" -> this.getOrCreateTransition(schedule.getWednesdaySchedules(), index);
                case "thursday" -> this.getOrCreateTransition(schedule.getThursdaySchedules(), index);
                case "friday" -> this.getOrCreateTransition(schedule.getFridaySchedules(), index);
                case "saturday" -> this.getOrCreateTransition(schedule.getSaturdaySchedules(), index);
                case "sunday" -> this.getOrCreateTransition(schedule.getSundaySchedules(), index);
                default -> throw new SchedulerException("Unrecognized day in entry " + key);
            };

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

    // to be replaced by REST calls
    /*private Map<DaysOfWeekSchedule, List<TransitionModel>> getTestData() {
        Map<DaysOfWeekSchedule, List<TransitionModel>> map = new HashMap<>();

        DaysOfWeekSchedule s = new DaysOfWeekSchedule(true, false, false, false, true, false, false);
        TransitionModel t1 = new TransitionModel(LocalTime.of(7, 0), 16);
        TransitionModel t2 = new TransitionModel(LocalTime.of(18, 0), 18);
        map.put(s, List.of(t1, t2));

        DaysOfWeekSchedule s2 = new DaysOfWeekSchedule(false, true, false, true, true, true, false);
        TransitionModel t3 = new TransitionModel(LocalTime.of(9, 30), 21);
        TransitionModel t4 = new TransitionModel(LocalTime.of(21, 0), 18);
        map.put(s2, List.of(t3, t4));

        DaysOfWeekSchedule s3 = new DaysOfWeekSchedule(true);
        TransitionModel t5 = new TransitionModel(LocalTime.of(9, 30), 21);
        TransitionModel t6 = new TransitionModel(LocalTime.of(21, 0), 18);
        map.put(s3, List.of(t5, t6));

        return map;
    }*/
}
