package com.mornati.sample.service;

import com.mornati.sample.config.FelixConfiguration;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

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
      
      Map<String, String> felixProperties = new HashMap<>(felixConfiguration.getPluginsService());
      felixProperties.put("org.osgi.framework.system.packages.extra", findPackageNamesStartingWith());
      
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

  /**
   * Felix provides an isolated classloader to each bundle. Bundles need to declare what packages they need in there manifest.
   * If a Bundle needs e.g. something from orgl.slf4j it aither needs to have it as classes itself or another bundle must export this packages.
   * java.* packages will be provided by the framework from the classloader it was created with. We can modify this, by giving the framework
   * a list of additional packages, that are provided by the spring boot container (should include e.g.slf4j)
  * @return
  */
  public String findPackageNamesStartingWith() {
	  StringJoiner joiner = new StringJoiner(",");  
	  Arrays.asList(Package.getPackages()).stream()
	        .map(Package::getName)
	        .filter(n -> !n.startsWith("java")) // the framework will expose this automatically
	        .filter(n -> !n.startsWith("org.apache.felix")) // we don't want the inner workings of the felix framework exposed
	        .forEach(joiner::add);
	  return joiner.toString();
	}
  
  @PreDestroy
  public void destroy() throws BundleException, InterruptedException {
    log.info("Stopping plugins OSGi service...");
    framework.stop();
    framework.waitForStop(0);
  }

}
