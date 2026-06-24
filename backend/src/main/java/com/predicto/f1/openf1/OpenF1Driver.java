package com.predicto.f1.openf1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenF1Driver {

    @JsonProperty("driver_number")
    private Integer driverNumber;

    @JsonProperty("session_key")
    private Integer sessionKey;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("name_acronym")
    private String nameAcronym;

    @JsonProperty("headshot_url")
    private String headshotUrl;

    @JsonProperty("team_name")
    private String teamName;

    @JsonProperty("team_colour")
    private String teamColour;
}
