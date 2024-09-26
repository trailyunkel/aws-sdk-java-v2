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

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ChecksumAlgorithm;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

public class TestVeyronMulti {

    static int MB = 1024 * 1024;
    static String bucket = "olapplin-veyron-test--usw2-az1--x-s3";

    @Test
    void veyronMultiPartUploadTest() {
        S3AsyncClient s3 = S3AsyncClient.crtBuilder().region(Region.US_WEST_2).build();

        String key = "test-veyron-multi-" + System.currentTimeMillis() + ".dat";
        CreateMultipartUploadResponse res = s3.createMultipartUpload(req -> req.bucket(bucket)
                                                                               .key(key)
                                                                               .checksumAlgorithm(ChecksumAlgorithm.CRC32))
                                              .join();

        int part = 1;
        UploadPartRequest uploadPartRequest =
            UploadPartRequest.builder()
                             .bucket(bucket)
                             .key(key)
                             .uploadId(res.uploadId())
                             .partNumber(part)
                             .checksumAlgorithm(ChecksumAlgorithm.CRC32)
                             .build();

        UploadPartResponse uploadPart = s3.uploadPart(uploadPartRequest, AsyncRequestBody.fromString(randomString(2 * MB)))
                                          .join();

        CompleteMultipartUploadRequest completeMultipartUploadRequest =
            CompleteMultipartUploadRequest.builder()
                                          .bucket(bucket)
                                          .key(key)
                                          .multipartUpload(CompletedMultipartUpload
                                                               .builder()
                                                               .parts(Arrays.asList(CompletedPart.builder()
                                                                                                 .partNumber(1)
                                                                                                 .eTag(uploadPart.eTag())
                                                                                                 .build()))
                                                               .build())
                                          .uploadId(res.uploadId())
                                          .build();

        CompleteMultipartUploadResponse completeRes = s3.completeMultipartUpload(completeMultipartUploadRequest).join();

        System.out.println(completeRes);

    }

    String randomString(int len) {
        Random r = new Random();
        byte[] bytes = new byte[len];
        r.nextBytes(bytes);
        return new String(bytes);
    }

}
