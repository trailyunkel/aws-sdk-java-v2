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

package software.amazon.awssdk;

import com.google.common.io.ByteStreams;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class AsyncAbortLatencyTest {

    static final int MB = 1024 * 1024;

    static String bucket = "";
    static String key = "";

    S3AsyncClient s3Client;

    @BeforeEach
    void init() {

        SdkAsyncHttpClient http = NettyNioAsyncHttpClient.builder().build();

        s3Client = S3AsyncClient.builder()
                           // .endpointOverride(URI.create("http://s3.aws-master.amazon.com")) // gamma
                           // .serviceConfiguration(c -> c.checksumValidationEnabled(false).pathStyleAccessEnabled(true)) // gamma
                           .serviceConfiguration(c -> c.checksumValidationEnabled(false))
                           .region(Region.US_EAST_1)
                           .httpClient(http)
                           .build();

    }

    @Test
    void v2Request() throws Exception {
        doAbort(s3Client, "NETTY");
    }

    void doAbort(S3AsyncClient s3Client, String client) throws Exception {

        System.out.println(client);
        System.out.println("GETTING FILE");
        GetObjectRequest getRequest = GetObjectRequest.builder()
                                                      .bucket(bucket)
                                                      .key(key)
                                                      .range("bytes=0-209715199") // 200 MB
                                                      .build();
        ResponseInputStream<GetObjectResponse> ris = s3Client.getObject(getRequest,
                                                                        AsyncResponseTransformer.toBlockingInputStream())
                                                             .join();
        readBlock(ris, 20 * MB);
        System.out.println("ABORT");
        long startTime = System.nanoTime();
        ris.abort();
        System.out.println("done abort in " + Duration.ofNanos(System.nanoTime() - startTime).toMillis() + "ms");
    }

    static void readBlock(ResponseInputStream<GetObjectResponse> is, int size) throws Exception {
        System.out.println("READING TO BUFFER");
        byte[] megablock = new byte[size];
        ByteStreams.readFully(is, megablock);
    }

}
