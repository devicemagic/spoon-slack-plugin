package com.devicemagic.gradle.slack

import com.devicemagic.gradle.slack.model.SlackMessageTransformer
import net.gpedro.integrations.slack.SlackApi
import net.gpedro.integrations.slack.SlackMessage
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class SlackPlugin implements Plugin<Project> {

    SlackPluginExtension mExtension

    void apply(Project project) {


        mExtension = project.extensions.create('slack', SlackPluginExtension)

        project.task("spoon-build") {

            def process = "gradlew.bat spoon".execute()

            def stringBuffer = new StringBuffer()
            def errorBuffer = new StringBuffer()

            process.waitForProcessOutput(stringBuffer,errorBuffer)
            def output = stringBuffer.toString()

            println("DEBUGGGG TESTER DEBUGGGG"+output)

            def printErr = System.err.&println

            printErr(errorBuffer.toString())

            if (output.contains("Tests") && output.contains("html")) {
                def url = output.substring(output.indexOf("Tests"), output.indexOf("html"))
                sendSpoonUrlToSlack(url, this)
            } else {
                printErr("An error occurred with the test results and the link wasn't generated.")
            }
        }

    }

    void sendSpoonUrlToSlack(String url, Task task) {
        SlackMessage slackMessage = SlackMessageTransformer.buildSlackMessage(mExtension.title, url, task, task.getState())
        SlackApi api = new SlackApi(mExtension.url)
        api.call(slackMessage)
    }

}

