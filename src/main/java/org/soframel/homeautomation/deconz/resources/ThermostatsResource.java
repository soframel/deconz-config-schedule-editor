package org.soframel.homeautomation.deconz.resources;

import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

@Path("/")
public class ThermostatsResource {
    private static Logger logger = Logger.getLogger(ThermostatsResource.class.getName());

    @ConfigProperty(name = "thermostats") 
    Map<String,String> thermostats;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance thermostats(Map<String,String> thermostats);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("thermostat") String thermostat) {
        
        return Templates.thermostats(thermostats);
    }


}
