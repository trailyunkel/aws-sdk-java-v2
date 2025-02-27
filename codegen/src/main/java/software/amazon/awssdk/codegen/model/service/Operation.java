/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.codegen.model.service;

import java.util.List;
import java.util.Map;
import software.amazon.awssdk.codegen.checksum.HttpChecksum;
import software.amazon.awssdk.codegen.compression.RequestCompression;
import software.amazon.awssdk.codegen.model.intermediate.EndpointDiscovery;

public class Operation {

    private String name;

    private boolean deprecated;

    private String deprecatedMessage;

    private Http http;

    private Input input;

    private Output output;

    private String documentation;

    private String authorizer;

    private List<ErrorMap> errors;

    private EndpointDiscovery endpointdiscovery;

    private boolean endpointoperation;

    private EndpointTrait endpoint;

    private AuthType authtype;

    private List<String> auth;

    private boolean httpChecksumRequired;

    private HttpChecksum httpChecksum;

    private RequestCompression requestcompression;

    private Map<String, StaticContextParam> staticContextParams;

    private Map<String, OperationContextParam> operationContextParams;

    private boolean unsignedPayload;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Operation withName(String name) {
        this.name = name;
        return this;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getDeprecatedMessage() {
        return deprecatedMessage;
    }

    public void setDeprecatedMessage(String deprecatedMessage) {
        this.deprecatedMessage = deprecatedMessage;
    }

    public Http getHttp() {
        return http;
    }

    public void setHttp(Http http) {
        this.http = http;
    }

    public Operation withHttp(Http http) {
        this.http = http;
        return this;
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public Operation withInput(Input input) {
        this.input = input;
        return this;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public List<ErrorMap> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorMap> errors) {
        this.errors = errors;
    }

    public AuthType getAuthtype() {
        return authtype;
    }

    public void setAuthtype(String authtype) {
        this.authtype = AuthType.fromValue(authtype);
    }

    public List<String> getAuth() {
        return auth;
    }

    public void setAuth(List<String> auth) {
        this.auth = auth;
    }

    public String getAuthorizer() {
        return authorizer;
    }

    public void setAuthorizer(String authorizer) {
        this.authorizer = authorizer;
    }

    public EndpointDiscovery getEndpointdiscovery() {
        return endpointdiscovery;
    }

    public void setEndpointdiscovery(EndpointDiscovery endpointdiscovery) {
        this.endpointdiscovery = endpointdiscovery;
    }

    public boolean isEndpointoperation() {
        return endpointoperation;
    }

    public void setEndpointoperation(boolean endpointoperation) {
        this.endpointoperation = endpointoperation;
    }

    public EndpointTrait getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(EndpointTrait endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isHttpChecksumRequired() {
        return httpChecksumRequired;
    }

    public void setHttpChecksumRequired(boolean httpChecksumRequired) {
        this.httpChecksumRequired = httpChecksumRequired;
    }

    public HttpChecksum getHttpChecksum() {
        return httpChecksum;
    }

    public void setHttpChecksum(HttpChecksum httpChecksum) {
        this.httpChecksum = httpChecksum;
    }

    public RequestCompression getRequestcompression() {
        return requestcompression;
    }

    public void setRequestcompression(RequestCompression requestcompression) {
        this.requestcompression = requestcompression;
    }

    public Map<String, StaticContextParam> getStaticContextParams() {
        return staticContextParams;
    }

    public void setStaticContextParams(Map<String, StaticContextParam> staticContextParams) {
        this.staticContextParams = staticContextParams;
    }

    public Map<String, OperationContextParam> getOperationContextParams() {
        return operationContextParams;
    }

    public void setOperationContextParams(Map<String, OperationContextParam> operationContextParams) {
        this.operationContextParams = operationContextParams;
    }

    public boolean isUnsignedPayload() {
        return unsignedPayload;
    }

    public void setUnsignedPayload(boolean unsignedPayload) {
        this.unsignedPayload = unsignedPayload;
    }
}
