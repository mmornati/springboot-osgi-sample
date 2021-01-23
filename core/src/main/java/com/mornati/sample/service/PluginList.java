package com.mornati.sample.service;

import com.mornati.sample.commons.plugins.IPlugin;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class PluginList {
  private Map<String, IPlugin> plugins = new HashMap<>();
}
