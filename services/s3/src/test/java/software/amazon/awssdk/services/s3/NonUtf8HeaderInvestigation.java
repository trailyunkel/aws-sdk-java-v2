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

package software.amazon.awssdk.services.s3;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@WireMockTest
public class NonUtf8HeaderInvestigation {
    static byte[] headerArray = {
        105, 110, 108, 105, 110, 101, 59, 32, 102, 105, 108, 101, 110, 97, 109, 101, 61, 115, 97, 109, 112, 108, 101, 95, 54, 52,
        48, (byte) 215, 52, 50, 54, 46, 106, 112, 101, 103
    };
    S3Client s3;

    @BeforeEach
    void setup(WireMockRuntimeInfo wiremock) {
        this.s3 = S3Client.builder()
                          .region(Region.US_WEST_2)
                          .endpointOverride(URI.create("http://localhost:" + wiremock.getHttpPort()))
                          .serviceConfiguration(S3Configuration.builder()
                                                               .pathStyleAccessEnabled(true)
                                                               .build())
                          .build();
    }

    @Test
    void testNonUtf8Header() {
        String s1 = new String(headerArray, ISO_8859_1);
        stubFor(get(anyUrl()).willReturn(
            aResponse()
                .withHeader("content-disposition", s1)
                .withBody("body")));

        ResponseInputStream<GetObjectResponse> res = s3.getObject(req -> req.bucket("bucket").key("key"));
        System.out.println(res.response().contentDisposition());
    }

}
