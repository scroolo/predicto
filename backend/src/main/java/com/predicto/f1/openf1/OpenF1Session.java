package com.predicto.f1.openf1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenF1Session {

    @JsonProperty("session_key")
    private Integer sessionKey;

    @JsonProperty("meeting_key")
    private Integer meetingKey;

    @JsonProperty("session_name")
    private String sessionName;

    @JsonProperty("session_type")
    private String sessionType;

    @JsonProperty("date_start")
    private String dateStart;

    @JsonProperty("date_end")
    private String dateEnd;

    @JsonProperty("is_cancelled")
    private Boolean isCancelled;

    @JsonProperty("location")
    private String location;

    @JsonProperty("year")
    private Integer year;
}
