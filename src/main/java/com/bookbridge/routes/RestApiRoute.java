package com.bookbridge.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;

public class RestApiRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .component("undertow")
                .host("0.0.0.0")
                .port(8080)
                .bindingMode(RestBindingMode.auto);

        // The REST endpoint: any query parameter supplied will be mapped to a header.
        rest("/").description("Book API")
                .get("fetch-books")
                .param().name("q").type(RestParamType.query).endParam()
                .param().name("fields").type(RestParamType.query).endParam()
                .param().name("key").type(RestParamType.query).endParam()
                .to("direct:fetchBookData");


    }
}
