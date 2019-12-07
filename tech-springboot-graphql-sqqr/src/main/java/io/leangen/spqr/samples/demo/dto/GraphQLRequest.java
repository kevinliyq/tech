package io.leangen.spqr.samples.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public class GraphQLRequest
    {
        //theoritically, variables is a map, but client lots of time send it is null
        //then unable to convert to Map.
        @JsonProperty("variables")
        protected String variables = null;

        @JsonProperty("operationName")

        protected String operationName = null;

        @JsonProperty("query")

        protected String query = null;


        @JsonProperty("requestContext")
        protected RequestContext requestContext;

        public void setQuery(String query)
        {
            this.query = query;
        }

        public void setVariables(String variables)
        {
            this.variables = variables;
        }

        public void setOperationName(String operationName)
        {
            this.operationName = operationName;
        }

        public String getQuery()
        {
            return query;
        }

        public String getVariables()
        {
            return variables;
        }

        public String getOperationName()
        {
            return operationName;
        }

        public RequestContext getRequestContext()
        {
            return requestContext;
        }
    }