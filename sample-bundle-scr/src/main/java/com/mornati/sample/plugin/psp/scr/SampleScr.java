package com.mornati.sample.plugin.psp.scr;

import com.mornati.sample.commons.plugins.IPlugin;
import com.mornati.sample.commons.plugins.dto.ActionResponse;
import com.mornati.sample.commons.plugins.dto.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(service = IPlugin.class)
@Slf4j
public class SampleScr implements IPlugin {

  @Activate
  public void activate() {
    log.info("Activating Service via SCR");
  }

  @Override
  public ActionResponse doAction() {
    return ActionResponse.builder().body("SCR Action Worked !!").build();
  }

  @Override
  public NotificationResponse doNotification() {
    return NotificationResponse.builder().body("SRC Notification Processed...").build();
  }

}
