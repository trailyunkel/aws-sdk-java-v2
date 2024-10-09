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
import com.sun.management.HotSpotDiagnosticMXBean;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.MBeanServer;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHost;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.internal.metrics.BytesReadTrackingInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.SdkHttpConfigurationOption;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.internal.conn.SdkTlsSocketFactory;
import software.amazon.awssdk.http.apache.internal.net.DelegateSocket;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.utils.AttributeMap;
import software.amazon.awssdk.utils.StringUtils;

public class ApacheStreamLatencyTest {

    static final String pathToDump = "/Users/olapplin/Develop/tmp/";

    static final int MB = 1024 * 1024;

    static String bucket = "";
    static String key = "";

    S3Client s3Client;

    @BeforeEach
    void init() {
        SdkHttpClient http = ApacheHttpClient.builder()
                                             // .socketFactory(TestSocketFactory.create()) // read from socket directly
                                             .build();

        s3Client = S3Client.builder()
                           .serviceConfiguration(c -> c.checksumValidationEnabled(false) // make the request similar to v1
                                                       // .pathStyleAccessEnabled(true)
                           )
                           // .endpointOverride(URI.create("http://s3.aws-master.amazon.com"))
                           .region(Region.US_WEST_2)
                           .httpClient(http)
                           .build();
    }

    // @Test
    void createBucket() {
        CreateBucketResponse res = s3Client.createBucket(r -> r.bucket(bucket));
        System.out.println(res);
    }

    // @Test
    void uploadObject() {
        PutObjectResponse res = s3Client.putObject(r -> r.bucket(bucket).key(key), RequestBody.fromString(rand(400 * MB)));
        System.out.println(res);
    }

    void headObject() {
        HeadObjectResponse res = s3Client.headObject(r -> r.bucket(bucket).key(key));
        System.out.println(res);
    }

    @Test
    void v2request() throws Exception {
        // doClose(s3Client, client);
        // TestSocketFactory socketFactory = TestSocketFactory.create();

        doAbort(s3Client, "====== APACHE CLIENT =====");
        // System.out.println("BYTES READ: " + socketFactory.inputStreamHolder.bytesRead());
    }

    static void doClose(S3Client s3Client, String client) throws Exception {
        System.out.println(client);
        System.out.println("GETTING FILE");
        GetObjectRequest getRequest = GetObjectRequest.builder()
                                                      .bucket(bucket)
                                                      .key(key)
                                                      .range("bytes=0-209715199") // 200 MB
                                                      .build();
        ResponseInputStream<GetObjectResponse> ris = s3Client.getObject(getRequest, ResponseTransformer.toInputStream());
        readBlock(ris, 20 * MB);

        System.out.println("CLOSE");
        long startTime = System.nanoTime();
        try {
            ris.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("done close in " + Duration.ofNanos(System.nanoTime() - startTime).toMillis() + "ms");
    }

    static void doAbort(S3Client s3Client, String client) throws Exception {

        System.out.println(client);
        System.out.println("GETTING FILE");
        GetObjectRequest getRequest = GetObjectRequest.builder()
                                                      .bucket(bucket)
                                                      .key(key)
                                                      .range("bytes=0-209715199") // 200 MB
                                                      .build();
        ResponseInputStream<GetObjectResponse> ris = s3Client.getObject(getRequest, ResponseTransformer.toInputStream());
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

    // creates a java heap dump
    public static void dumpHeap(String filePath, boolean live) throws IOException {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(
            server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
        System.out.println("HEAP DUMP : " + filePath);
        mxBean.dumpHeap(filePath, live);
    }

    // Socket that tracks the number of bytes, directly from the connection socket's input stream
    private static class TestSocketFactory extends SdkTlsSocketFactory {
        BytesReadTrackingInputStream inputStreamHolder;

        public TestSocketFactory(SSLContext sslContext, HostnameVerifier hostnameVerifier) {
            super(sslContext, hostnameVerifier);
        }

        @Override
        public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
                                    InetSocketAddress localAddress, HttpContext context) throws IOException {
            Socket s = super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context);
            System.out.printf("[wireshark] PORT: %s%n", +s.getPort());
            System.out.printf("[wireshark] REMOTE ADDR: %s%n", s.getRemoteSocketAddress());
            return new DelegateSocket(s) {
                @Override
                public InputStream getInputStream() throws IOException {
                    inputStreamHolder = new BytesReadTrackingInputStream(AbortableInputStream.create(s.getInputStream()),
                                                                         new AtomicLong(0));
                    return inputStreamHolder;
                }
            };
        }

        static TestSocketFactory create() {
            AttributeMap resolvedOptions = SdkHttpConfigurationOption.GLOBAL_HTTP_DEFAULTS;
            return new TestSocketFactory(ApacheHttpClient.ApacheConnectionManagerFactory.getSslContext(resolvedOptions),
                                         ApacheHttpClient.ApacheConnectionManagerFactory.getHostNameVerifier(resolvedOptions));
        }
    }

    static String rand(int size) {
        Random r = new Random();
        byte[] content = new byte[size];
        r.nextBytes(content);
        return new String(content);
    }

}
