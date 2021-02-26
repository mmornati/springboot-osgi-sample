package com.mornati.sample.service;

import com.mornati.sample.config.FelixConfiguration;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

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
      Map<String, String> felixProperties = new HashMap<>(felixConfiguration.getConfig());
      felixProperties.put("org.osgi.framework.system.packages.extra",
          findPackageNamesStartingWith(
              Optional.ofNullable(felixProperties.get("auto.exported.packages"))
                  .map(s -> s.split(","))
                  .map(Arrays::asList)
                  .orElse(List.of())
          ) + "," + felixProperties.get("org.osgi.framework.system.packages.extra"));
      framework = factory.newFramework(felixProperties);
      framework.init();

      // Use the system bundle context to process the auto-deploy
      // and auto-install/auto-start properties.
      AutoProcessor.process(felixConfiguration.getConfig(), framework.getBundleContext());

      // Log Bundle Activations
      framework.getBundleContext().addBundleListener(springAwareBundleListener);

      // Start the framework.
      framework.start();

      Bundle b = framework.getBundleContext().getBundle(0);
      BundleRevision br = b.adapt(BundleRevision.class);
      List<Capability> caps = br.getCapabilities("osgi.ee");
      log.debug("OSGi capabilities: " + caps);
    } catch (Exception ex) {
      log.error("Error initializing the OSGi framework. As it is mandatory the system will be halted", ex);
      System.exit(1);
    }
  }

  @PreDestroy
  public void destroy() throws BundleException, InterruptedException {
    log.info("Stopping plugins OSGi service...");
    framework.stop();
    framework.waitForStop(0);
  }

  /**
   * Felix provides an isolated classloader to each bundle. Bundles need to declare what packages they need in there manifest. If a Bundle needs e.g. something from org.slf4j it either needs to have it as classes itself or another bundle must export this packages. java.* packages will be provided by the framework from the classloader it was created with. We can modify this, by giving the framework a list of additional packages, that are provided by the spring boot container (should include e.g.slf4j)
   */
  protected String findPackageNamesStartingWith(List<String> packages) {
    return packages.stream().map(this::getPackages)
        .flatMap(Set::stream)
        .collect(Collectors.joining(","));
  }

  protected Set<String> getPackages(String basePackage) {
    Set<String> packagesNames = new HashSet<>();

    try {
      ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
      MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

      String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resolveBasePackage(basePackage) + "/" + "**/*.class";
      Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
      for (Resource resource : resources) {
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
        Class aClass = Class.forName(metadataReader.getClassMetadata().getClassName());
        String packageName = aClass.getPackage().getName();
        packagesNames.add(packageName);
      }
    } catch (ClassNotFoundException | IOException e) {
      log.error("Error looking for provided package", e);
    }
    return packagesNames;
  }

  private static String resolveBasePackage(String basePackage) {
    return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
  }

}
