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

package software.amazon.awssdk.codegen.emitters.tasks;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import software.amazon.awssdk.codegen.emitters.GeneratorTask;
import software.amazon.awssdk.codegen.emitters.GeneratorTaskParams;
import software.amazon.awssdk.codegen.poet.client.SyncClientClass;

public class MultiReleaseSyncClientGeneratorTasks extends SyncClientGeneratorTasks {
    public MultiReleaseSyncClientGeneratorTasks(GeneratorTaskParams dependencies) {
        super(dependencies);
    }

    protected GeneratorTask createClientClassTask() throws IOException {
        log.info(">>>>>>>>>>>>>>>>>> MultiReleaseSyncClientGeneratorTasks");
        return createMultiReleasePoetGeneratorTask(new SyncClientClassWithLog(generatorTaskParams));
    }

    protected static class SyncClientClassWithLog extends SyncClientClass {
        public SyncClientClassWithLog(GeneratorTaskParams taskParams) {
            super(taskParams);
        }

        @Override
        protected TypeSpec.Builder createTypeSpec() {
            TypeSpec.Builder builder = super.createTypeSpec();
            builder.addStaticBlock(
                CodeBlock.builder()
                         .addStatement("$T thread = $T.ofVirtual().start(() -> System.out.println($S))",
                                       Thread.class, Thread.class,
                                       "~~~~~ !!!!!!!!!! FROM VIRTUAL THREAD, JAVA 21 ONLY !!!!!!!!! ~~~~~")
                         .build());
            return builder;
        }
    }

}
