package org.soframel.homeautomation.deconz.resources;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.soframel.homeautomation.deconz.SchedulerException;
import org.soframel.homeautomation.deconz.client.DeconzConfigScheduleClientInterface;
import org.soframel.homeautomation.deconz.dto.Sensor;
import org.soframel.homeautomation.deconz.dto.Transition;
import org.soframel.homeautomation.deconz.model.MultipartBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/import")
@ApplicationScoped
public class ScheduleImportResource {
    private static Logger logger = Logger.getLogger(ScheduleImportResource.class.getName());
 
    
    @ConfigProperty(name = "thermostat") 
    Map<String,String> thermostats;

    @Inject
    DeconzConfigScheduleClientInterface client;
    //for tests: comment @Inject and add =new MockConfigScheduleClient()

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance scheduleImport(Map<String,String> thermostats, String id);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@DefaultValue("") @QueryParam("id") String sensorid) {       
        return Templates.scheduleImport(thermostats,sensorid);        
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public TemplateInstance saveSchedules(@MultipartForm MultipartBody data) throws SchedulerException {        
        // clean existing schedules
        client.deleteAllSchedules(data.id);        
        this.sleep(1000);        

        //load schedules for each bitmap
        logger.info(() -> String.format("Saving schedules for sensor %s", data.id));
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String,Object> bitmaps = mapper.readValue(data.file, Map.class);
            //order the bitmaps first
            List<String> bitmapsList=new ArrayList<>();
            bitmapsList.addAll(bitmaps.keySet());
            bitmapsList.sort(new BitmapComparator());
            //save all schedules
            for(String bitmap: bitmapsList){
                List schedule=(List) bitmaps.get(bitmap);
                logger.info(() -> String.format("for bitmap %s, schedule=%s)", bitmap, schedule));
                client.createScheduleRaw(data.id, bitmap, schedule);
            }

        } catch (IOException e) {
            logger.severe("IOException while reading file: "+e.getMessage());
            e.printStackTrace();
        }
        this.sleep(3000);
        
        logger.info(() -> String.format("Saved schedules for sensor %s", data.id));

        return Templates.scheduleImport(thermostats,data.id);        
    }

    private void sleep(int milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            //nothing
        }
    }

    class BitmapComparator implements Comparator<String>{

        @Override
        public int compare(String b1, String b2) {
            if(b1==null && b2!=null){
                return 1;
            }
            else if(b1!=null && b2==null){
                return -1;
            }
            else if(b1.startsWith("W") && b2.startsWith("W")){
                //remove first "W"
                int n1=Integer.parseInt(b1.substring(1));
                int n2=Integer.parseInt(b2.substring(1));
                return n2-n1;
            }
            else{
                throw new IllegalArgumentException("these are not bitmaps: "+b1+", "+b2);
            }
        }
    }

    @Path("{id}/export")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String export(@DefaultValue("") @PathParam("id") String sensorid) throws SchedulerException, JsonProcessingException {       
        Sensor s=client.getSensorConfig(sensorid);
        Map<String, List<Transition>> schedulesJSON = s.getConfig().getSchedule();
        
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(schedulesJSON);
    }
}
