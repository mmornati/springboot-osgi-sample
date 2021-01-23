package com.mornati.sample.service;

import com.mornati.sample.commons.plugins.IPlugin;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpringAwareBundleListener implements BundleListener {

  private final PluginList pluginList;

  @Override
  public void bundleChanged(BundleEvent bundleEvent) {

    log.info(
        String.format(
            "Bundle %s:%s changed state to %s - Type: %s",
            bundleEvent.getBundle().getSymbolicName(),
            bundleEvent.getBundle().getVersion(),
            getBundleStateAsString(bundleEvent.getBundle().getState()),
            getBundleStateAsString(bundleEvent.getType())
        )
    );

    if (bundleEvent.getBundle().getState() == Bundle.ACTIVE) {
      try {
        ServiceReference<?>[] services = bundleEvent.getBundle().getBundleContext().getAllServiceReferences(IPlugin.class.getName(), null);
        if (services != null && services.length>0)
        Arrays.asList(services)
            .forEach(s -> pluginList.getPlugins().put(s.getBundle().getSymbolicName(), (IPlugin) s.getBundle().getBundleContext().getService(s)));

      } catch (InvalidSyntaxException e) {
        log.warn("Problem reading services from BundleContext");
      }

    } else if (bundleEvent.getBundle().getState() == Bundle.UNINSTALLED) {
      pluginList.getPlugins().remove(bundleEvent.getBundle().getSymbolicName());
    }

  }

  private String getBundleStateAsString(int state) {
    return switch (state) {
      case Bundle.ACTIVE -> "Active";
      case Bundle.INSTALLED -> "Installed";
      case Bundle.RESOLVED -> "Resolved";
      case Bundle.STARTING -> "Starting";
      case Bundle.STOPPING -> "Stopping";
      case Bundle.UNINSTALLED -> "Uninstalled";
      default -> "Unknown";
    };

  }
}
