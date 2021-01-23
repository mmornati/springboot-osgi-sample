package com.mornati.sample.service;

import com.mornati.sample.config.FelixConfiguration;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.resource.Capability;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PluginService {

  @Getter
  private Framework framework;
  private final SpringAwareBundleListener springAwareBundleListener;
  private final FelixConfiguration felixConfiguration;

  @EventListener(ApplicationReadyEvent.class)
  public void startFramework() throws BundleException {
    log.info("Starting Plugin OSGi service...");
    // Get Felix properties from Spring Boot config

    try {
      // Create an instance and initialise the framework.
      FrameworkFactory factory = new org.apache.felix.framework.FrameworkFactory();
      framework = factory.newFramework(felixConfiguration.getPluginsService());
      framework.init();

      // Use the system bundle context to process the auto-deploy
      // and auto-install/auto-start properties.
      AutoProcessor.process(felixConfiguration.getPluginsService(), framework.getBundleContext());

      // Log Bundle Activations
      framework.getBundleContext().addBundleListener(springAwareBundleListener);

      // Start the framework.
      framework.start();

      Bundle b = framework.getBundleContext().getBundle(0);
      BundleRevision br = b.adapt(BundleRevision.class);
      List<Capability> caps = br.getCapabilities("osgi.ee");
      log.debug("OSGi capabilities: " + caps);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(0);
    }
  }

  @PreDestroy
  public void destroy() throws BundleException, InterruptedException {
    log.info("Stopping plugins OSGi service...");
    framework.stop();
    framework.waitForStop(0);
  }

}
