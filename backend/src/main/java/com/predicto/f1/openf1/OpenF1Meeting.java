package com.predicto.f1.openf1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenF1Meeting {

    @JsonProperty("meeting_key")
    private Integer meetingKey;

    @JsonProperty("meeting_name")
    private String meetingName;

    @JsonProperty("meeting_official_name")
    private String meetingOfficialName;

    @JsonProperty("country_name")
    private String countryName;

    @JsonProperty("country_flag")
    private String countryFlag;

    @JsonProperty("circuit_short_name")
    private String circuitShortName;

    @JsonProperty("circuit_image")
    private String circuitImage;

    @JsonProperty("location")
    private String location;

    @JsonProperty("date_start")
    private String dateStart;

    @JsonProperty("date_end")
    private String dateEnd;

    @JsonProperty("year")
    private Integer year;

    @JsonProperty("is_cancelled")
    private Boolean isCancelled;
}
