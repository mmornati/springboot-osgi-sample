package com.mornati.sample.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Sample {
  String name;
  Long id;
}
