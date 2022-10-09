package org.soframel.homeautomation.deconz;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.soframel.homeautomation.deconz.model.DaysOfWeekSchedule;
import org.soframel.homeautomation.deconz.model.ScheduleForEachDay;
import org.soframel.homeautomation.deconz.model.TransitionModel;

import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.CheckedTemplate;

@Path("/")
public class DeconzConfigScheduleResource {
    private static Logger logger = Logger.getLogger(DeconzConfigScheduleResource.class.getName());

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance schedules(String thermostat, ScheduleForEachDay schedules);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("thermostat") String thermostat) {
        Map<DaysOfWeekSchedule, List<TransitionModel>> data = this.getTestData();

        return Templates.schedules(thermostat, ScheduleForEachDay.parseFromScheduleMap(data));
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance saveSchedule(@QueryParam("thermostat") String thermostat,
            MultivaluedMap<String, String> form) throws SchedulerException {
        logger.info("saving schedule: " + form);
        ScheduleForEachDay schedule = new ScheduleForEachDay();

        for (String key : form.keySet()) {
            List<String> dataList = form.get(key);
            // we only have one data per key in our case
            if (dataList.size() > 0) {
                String data = dataList.get(0);
                this.addDataToSchedule(schedule, key, data);
            }
        }
        // TODO: save thermostat schedule
        return Templates.schedules(thermostat, schedule);
    }

    private void addDataToSchedule(ScheduleForEachDay schedule, String key, String data) throws SchedulerException {
        if (data != null && !data.equals("")) {
            StringTokenizer tokenizer = new StringTokenizer(key, "-", false);
            String day = tokenizer.nextToken();
            int index=-1;
            try{
                index = Integer.parseInt(tokenizer.nextToken());
            } catch(NumberFormatException e){
                throw new SchedulerException("Could not parse entry index in "+key);
            }
            boolean isTime = (tokenizer.nextToken().equals("time"));

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
                try{
                    LocalTime time = LocalTime.parse(data);
                    trans.setTime(time);
                } catch(DateTimeParseException e){
                    throw new SchedulerException("Could not parse time for entry "+key+", time="+data);
                }                
            } else { // temperature
                try{
                int temp = Integer.parseInt(data);
                trans.setTemperature(temp);
                }catch(NumberFormatException e){
                    throw new SchedulerException("Could not parse temperature for entry "+key+", temperature="+data);
                }
            }
        }else{
            //TODO: manage deleted entries
        }
    }

    private TransitionModel getOrCreateTransition(List<TransitionModel> list, int index) {
        if (list.size() > index) {
            return list.get(index);
        } else {
            while(list.size()<index+1){
                list.add(new TransitionModel());
            }
            return list.get(index);
        }
    }

    // to be replaced by REST calls
    private Map<DaysOfWeekSchedule, List<TransitionModel>> getTestData() {
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
    }
}
