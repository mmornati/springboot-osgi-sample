package com.mornati.sample.plugin.psp.sample;

import com.mornati.sample.commons.plugins.dto.ActionResponse;
import com.mornati.sample.commons.plugins.IPlugin;
import com.mornati.sample.commons.plugins.dto.NotificationResponse;

public class Sample implements IPlugin {
  @Override
  public ActionResponse doAction() {
    return ActionResponse.builder().body("Action Worked !!").build();
  }

  @Override
  public NotificationResponse doNotification() {
    return NotificationResponse.builder().body("Notification Processed...").build();
  }

}
