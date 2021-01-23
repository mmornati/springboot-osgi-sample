package com.mornati.sample.commons.plugins;

import java.util.Hashtable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PluginDescriptor {

  Object implementation;
  String name;
  Hashtable<String, Object> params;

}
