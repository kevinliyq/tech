package io.leangen.spqr.samples.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;

/**
 * @author: yoli
 * @since: 2019/11/15
 */
public class GraphQLResponse
{
    @JsonProperty("code")
    protected int code = 0;

    @JsonProperty("data")
    protected Object data = null;

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public Object getData()
    {
        return data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder{
        private int code;

        private Object data;

        public Builder code(int code)
        {
            this.code = code;
            return this;
        }

        public Builder data(Object data)
        {
            this.data = data;
            return this;
        }

        public GraphQLResponse build()
        {
            GraphQLResponse response = new GraphQLResponse();
            response.code = code;
            response.data = data;

            return response;
        }
    }
}
