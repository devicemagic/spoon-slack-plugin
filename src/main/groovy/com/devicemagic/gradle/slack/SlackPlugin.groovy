package com.devicemagic.gradle.slack

import com.devicemagic.gradle.slack.model.SlackMessageTransformer
import net.gpedro.integrations.slack.SlackApi
import net.gpedro.integrations.slack.SlackMessage
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.logging.StandardOutputListener
import org.gradle.api.tasks.TaskState

class SlackPlugin implements Plugin<Project> {

    SlackPluginExtension mExtension
    StringBuilder mTaskLogBuilder

    void apply(Project project) {

        mTaskLogBuilder = new StringBuilder()
        mExtension = project.extensions.create('slack-spoon', SlackPluginExtension)

        project.task("spoon-build") {
            new ByteArrayOutputStream().withStream { os ->
                def result = exec {
                    executable = 'gradlew'
                    args = ['spoon']
                    standardOutput = os
                }

                def output = os.toString()
                println(output)

                if(output.contains("Tests") && output.contains("html")) {
                    def url = output.substring(output.indexOf("Tests"), output.indexOf("html"))
                    sendSpoonUrlToSlack(url, this)
                }
                else
                {
                    println("An error occurred with the test results.")
                }
            }

        }

        project.afterEvaluate {
            if (mExtension.url != null && mExtension.enabled)
                monitorTasksLifecycle(project)
        }
    }

    void sendSpoonUrlToSlack(String url, Task task) {
        SlackMessage slackMessage = SlackMessageTransformer.buildSlackMessage(mExtension.title, url, task, task.getState(), mTaskLogBuilder.toString())
        SlackApi api = new SlackApi(mExtension.url)
        api.call(slackMessage)
    }

    void monitorTasksLifecycle(Project project) {


        project.getGradle().getTaskGraph().addTaskExecutionListener(new TaskExecutionListener() {
            @Override
            void beforeExecute(Task task) {
                task.logging.addStandardOutputListener(new StandardOutputListener() {
                    @Override
                    void onOutput(CharSequence charSequence) {
                        mTaskLogBuilder.append(charSequence)
                    }
                })
            }

            @Override
            void afterExecute(Task task, TaskState state) {
            }
        })
    }


}