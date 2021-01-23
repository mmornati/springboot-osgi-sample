package com.mornati.sample.commons.plugins;

import com.mornati.sample.commons.plugins.dto.ActionResponse;
import com.mornati.sample.commons.plugins.dto.NotificationResponse;

public interface IPlugin {

  ActionResponse doAction();

  NotificationResponse doNotification();
}
