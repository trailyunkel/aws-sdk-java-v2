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

package software.amazon.awssdk.transfer.s3;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;

public class ThreadWaitInvestigationTest {

    private static final String TEST_SNAPSHOT_FILE = "/tmp/4GB.dat";

    public static final int GB = 1024 * 1024 * 1024;

    private String s3BucketName = "olapplin-test-bucket";
    private S3TransferManager manager;

    @BeforeEach
    void init() throws Exception {
        S3AsyncClient s3AsyncClient = S3AsyncClient.builder()
            .multipartEnabled(true)
            .region(Region.US_WEST_2)
            .build();

        this.manager = S3TransferManager.builder()
                                        .s3Client(s3AsyncClient)
                                        .build();
    }

    @Test
    void testThreadWait() {
        uploadObjectToS3(new File(TEST_SNAPSHOT_FILE));
    }

    public void uploadObjectToS3(File file) {
        System.out.println("Uploading to S3");
        try {
            byte[] hashBytes = DigestUtils.sha256(new FileInputStream(file));
            String sha256Checksum = Base64.getEncoder().encodeToString(hashBytes);
            Map<String, String> metadata = new HashMap<>();
            metadata.put("checksum-sha256", sha256Checksum);
            metadata = Collections.unmodifiableMap(metadata);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                                                .bucket(this.s3BucketName)
                                                                .metadata(metadata)
                                                                .key(file.getName() + UUID.randomUUID())
                                                                .build();
            UploadFileRequest uploadFileRequest = UploadFileRequest.builder()
                                                                   .putObjectRequest(putObjectRequest)
                                                                   .source(file)
                                                                   .build();
            FileUpload fileUpload = this.manager.uploadFile(uploadFileRequest);
            fileUpload.completionFuture().join();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to upload file.", ex);
        } finally {
            System.out.println("Done uploading");
        }
    }

    private void uploadObjectToS3AndCountDown(File file, CountDownLatch latch) {
        try {
            uploadObjectToS3(file);
        } finally {
            System.out.println("Done " + latch.getCount());

            latch.countDown();
        }
    }

}
