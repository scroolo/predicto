package com.predicto.f1.openf1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenF1SessionResult {

    @JsonProperty("driver_number")
    private Integer driverNumber;

    @JsonProperty("position")
    private Integer position;
}
