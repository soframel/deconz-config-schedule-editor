package org.soframel.homeautomation.deconz.model;

import static org.junit.Assert.assertEquals;

import java.time.LocalTime;

import org.junit.Test;
import org.soframel.homeautomation.deconz.dto.Transition;
import org.soframel.homeautomation.deconz.model.TransitionModel;

public class TransitionModelTest {
 
    @Test
    public void testParse(){
        assertEquals(new TransitionModel(LocalTime.of(18,45), 1850), TransitionModel.parse(new Transition(1850, "T18:45")));
    }
}
