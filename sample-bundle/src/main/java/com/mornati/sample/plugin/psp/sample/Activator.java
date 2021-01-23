package com.mornati.sample.plugin.psp.sample;

import com.mornati.sample.commons.plugins.AbstractPluginActivator;
import com.mornati.sample.commons.plugins.IPlugin;
import com.mornati.sample.commons.plugins.PluginDescriptor;
import java.util.Hashtable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Activator extends AbstractPluginActivator {

  @Override
  protected PluginDescriptor registerService() {
    Hashtable<String, Object> props = new Hashtable<>();
    props.put("Plugin-Name", "SamplePlugin");
    return PluginDescriptor.builder()
        .implementation(new Sample())
        .name(IPlugin.class.getName())
        .params(props)
        .build();
  }
}
