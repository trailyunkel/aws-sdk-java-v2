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

import software.amazon.awssdk.annotations.SdkInternalApi;

@SdkInternalApi
public class JavaPrinter implements Printer {
    public void print(String msg) {
        Thread thread = Thread.ofVirtual().start(() -> System.out.println(">>>>>>>>>>>>>>>> logging from java21: " + msg));
        try {
            thread.join();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
}
