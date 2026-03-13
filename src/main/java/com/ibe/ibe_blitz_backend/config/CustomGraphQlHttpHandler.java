package com.ibe.ibe_blitz_backend.config;

import graphql.GraphQLError;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.graphql.server.webmvc.GraphQlHttpHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

public class CustomGraphQlHttpHandler extends GraphQlHttpHandler {

    public CustomGraphQlHttpHandler(org.springframework.graphql.server.WebGraphQlHandler webGraphQlHandler) {
        super(webGraphQlHandler);
    }

    @Override
    protected HttpStatus selectResponseStatus(WebGraphQlResponse response, MediaType mediaType) {
        List<GraphQLError> errors = response.getExecutionResult().getErrors();
        if (!errors.isEmpty()) {
            for (GraphQLError error : errors) {
                if (error.getErrorType() == ErrorType.BAD_REQUEST) {
                    return HttpStatus.BAD_REQUEST;
                }
                if (error.getErrorType() == ErrorType.NOT_FOUND) {
                    return HttpStatus.NOT_FOUND;
                }
            }
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return super.selectResponseStatus(response, mediaType);
    }
}





