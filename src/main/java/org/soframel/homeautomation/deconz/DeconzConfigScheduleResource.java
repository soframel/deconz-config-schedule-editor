package org.soframel.homeautomation.deconz;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.soframel.homeautomation.deconz.model.DaysOfWeekSchedule;
import org.soframel.homeautomation.deconz.model.ScheduleForEachDay;
import org.soframel.homeautomation.deconz.model.TransitionModel;

import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.CheckedTemplate;

@Path("/")
public class DeconzConfigScheduleResource {
    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance schedules(String thermostat, ScheduleForEachDay schedules); 
    } 

    private  Map<DaysOfWeekSchedule, List<TransitionModel>> getTestData(){
        Map<DaysOfWeekSchedule, List<TransitionModel>> map=new HashMap<>();

        DaysOfWeekSchedule s=new DaysOfWeekSchedule(true, false, false, false, true, false, false);
        TransitionModel t1=new TransitionModel(LocalTime.of(7, 0), 16);
        TransitionModel t2=new TransitionModel(LocalTime.of(18, 0), 18);
        map.put(s, List.of(t1, t2));


        DaysOfWeekSchedule s2=new DaysOfWeekSchedule(false, true, false, true, true, true, false);
        TransitionModel t3=new TransitionModel(LocalTime.of(9, 30), 21);
        TransitionModel t4=new TransitionModel(LocalTime.of(21, 0), 18);
        map.put(s2, List.of(t3, t4));

        DaysOfWeekSchedule s3=new DaysOfWeekSchedule(true);
        TransitionModel t5=new TransitionModel(LocalTime.of(9, 30), 21);
        TransitionModel t6=new TransitionModel(LocalTime.of(21, 0), 18);
        map.put(s3, List.of(t5, t6));

        return map;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("thermostat") String thermostat) {
        Map<DaysOfWeekSchedule, List<TransitionModel>> data=this.getTestData();

        return Templates.schedules(thermostat, this.transformForEachDay(data));
    }

    protected ScheduleForEachDay transformForEachDay(Map<DaysOfWeekSchedule, List<TransitionModel>> schedule){
        ScheduleForEachDay result=new ScheduleForEachDay();

        for(DaysOfWeekSchedule s: schedule.keySet()){
            if(s.isHolidays()){
                result.addTransitionsToDay(0, schedule.get(s));
            }
            if(s.isMonday()){
                result.addTransitionsToDay(1, schedule.get(s));
            }
            if(s.isTuesday()){
                result.addTransitionsToDay(2, schedule.get(s));
            }
            if(s.isWednesday()){
                result.addTransitionsToDay(3, schedule.get(s));
            }
            if(s.isThursday()){
                result.addTransitionsToDay(4, schedule.get(s));
            }
            if(s.isFriday()){
                result.addTransitionsToDay(5, schedule.get(s));
            }
            if(s.isSaturday()){
                result.addTransitionsToDay(6, schedule.get(s));
            }    
            if(s.isSunday()){
                result.addTransitionsToDay(7, schedule.get(s));
            }                                                                                    
        }
        result.orderTransitions();
        return result;
    }
}
