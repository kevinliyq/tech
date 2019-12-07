package io.leangen.spqr.samples.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: yoli
 * @since: 2019/11/15
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestContext
{
    private static final long serialVersionUID = 1L;

    @JsonProperty("source")

    protected String source = null;

    @JsonProperty("loggedInUserId")

    protected String loggedInUserId = null;

    public String getSource()
    {
        return source;
    }

    public String getLoggedInUserId()
    {
        return loggedInUserId;
    }
}
