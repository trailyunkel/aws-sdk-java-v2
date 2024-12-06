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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import software.amazon.awssdk.codegen.emitters.GeneratorTask;
import software.amazon.awssdk.codegen.emitters.GeneratorTaskParams;

/**
 * Common generator tasks.
 */
class CommonGeneratorTasks extends CompositeGeneratorTask {
    CommonGeneratorTasks(GeneratorTaskParams params) {
        super(tasks(params));
    }

    private static List<GeneratorTask> tasks(GeneratorTaskParams params) {
        List<GeneratorTask> tasks = new ArrayList<>();
        tasks.add(new CommonClientGeneratorTasks(params));
        tasks.add(new SyncClientGeneratorTasks(params));
        tasks.add(new MarshallerGeneratorTasks(params));
        tasks.add(new ModelClassGeneratorTasks(params));
        tasks.add(new PackageInfoGeneratorTasks(params));
        tasks.add(new BaseExceptionClassGeneratorTasks(params));
        tasks.add(new CommonInternalGeneratorTasks(params));
        if (params.getModel().getCustomizationConfig().isMultiReleaseJarLog()) {
            tasks.add(new MultiReleaseSyncClientGeneratorTasks(params));
        }
        return tasks;
    }
}
