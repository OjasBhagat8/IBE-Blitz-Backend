package com.ibe.ibe_blitz_backend.config;

import com.ibe.ibe_blitz_backend.exceptions.BadRequestException;
import com.ibe.ibe_blitz_backend.exceptions.NotFoundException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof BadRequestException) {
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.BAD_REQUEST)
                    .extensions(Map.of("code", "BAD_REQUEST"))
                    .message(ex.getMessage())
                    .build();
        }

        if (ex instanceof NotFoundException) {
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.NOT_FOUND)
                    .extensions(Map.of("code", "NOT_FOUND"))
                    .message(ex.getMessage())
                    .build();
        }

        return null;
    }
}