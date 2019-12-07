package io.leangen.spqr.samples.demo.controller;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.spqr.samples.demo.dto.GraphQLResponse;
import io.leangen.spqr.samples.demo.query.annotated.PersonQuery;
import io.leangen.spqr.samples.demo.query.annotated.SocialNetworkQuery;
import io.leangen.spqr.samples.demo.query.annotated.VendorQuery;
import io.leangen.spqr.samples.demo.query.unannotated.DomainQuery;
import io.leangen.spqr.samples.demo.dto.GraphQLRequest;
import io.leangen.spqr.samples.demo.query.unannotated.ProductQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RestController
public class GraphQLSampleController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLSampleController.class);

    private final GraphQL graphQL;

    @Autowired
    public GraphQLSampleController(PersonQuery personQuery,
                                   SocialNetworkQuery socialNetworkQuery,
                                   DomainQuery domainQuery,
                                   ProductQuery productQuery,
                                   VendorQuery vendorQuery) {

        //Schema generated from query classes
        GraphQLSchema schema = new GraphQLSchemaGenerator()
                .withBasePackages("io.leangen.spqr.samples.demo")
                .withOperationsFromSingletons(personQuery, socialNetworkQuery, vendorQuery, domainQuery, productQuery)
                .generate();
        graphQL = GraphQL.newGraphQL(schema).build();

        LOGGER.info("Generated GraphQL schema using SPQR");
    }

    @PostMapping(value = "/graphql", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public GraphQLResponse singleRequest(@RequestBody GraphQLRequest request, HttpServletRequest raw) {
//        ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
//                .query(request.get("query"))
//                .operationName(request.get("operationName"))
//                .context(raw)
//                .build());

        CompletableFuture<ExecutionResult> executionResultFuture = graphQL.executeAsync(
                ExecutionInput.newExecutionInput()
                        .query(request.getQuery())
                        .operationName(request.getOperationName())
                        .context(raw)
                        .build()
        );
        final GraphQLResponse.Builder builder = GraphQLResponse.builder();
        executionResultFuture.whenComplete((executionResult, ex) ->
                                           {
                                               if (ex != null)
                                               {
                                                   builder.code(1).data(ex.getMessage());
                                                   return;
                                               }
                                               if (executionResult.getErrors() != null && !executionResult.getErrors().isEmpty())
                                               {
                                                   builder.code(1).data("" + executionResult.getErrors());
                                                   return;
                                               }
                                               else
                                               {
                                                   builder.code(0).data(executionResult.getData());
                                               }

                                           });

        try
        {
            executionResultFuture.get(5, TimeUnit.SECONDS);
            return builder.build();
        }catch (InterruptedException | ExecutionException | TimeoutException ex)
        {
            return builder.code(1).data(ex.getMessage()).build();
        }
    }

    @PostMapping(value = "/batch", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<Object> executeBatch(@RequestBody List<GraphQLRequest> requests, HttpServletRequest raw) {
        List<ExecutionInput> inputs = requests.stream().map(request -> ExecutionInput.newExecutionInput()
                                                      .query(request.getQuery())
                                                      .operationName(request.getOperationName())
                                                      .context(raw)
                                                      .build())
                .collect(Collectors.toList());

        final List<CompletableFuture<ExecutionResult>> incompleteResults= inputs.stream()
                    .map(input -> graphQL.executeAsync(input))
                    .collect(Collectors.toList());

        List<Object> results = new ArrayList<>(inputs.size());

        incompleteResults.parallelStream().forEach(
                (CompletableFuture<ExecutionResult> future) ->
                {
                    try
                    {
                        future.get(5, TimeUnit.SECONDS);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
        );


        incompleteResults.forEach((CompletableFuture<ExecutionResult> future) ->
        {
            try
            {
                if (future.isDone())
                {
                    results.add(future.get().getData());
                }
                else
                {
                    results.add(null);
                }
            }catch (Exception ex)
            {
                ex.printStackTrace();
            }

        });

        return results;

    }
}
