package com.devicemagic.gradle.slack

class SlackPluginExtension {
    String url
    List<Object> dependsOnTasks
    String title
    boolean enabled = true

    void dependsOnTasks(Object... paths) {
        this.dependsOnTasks = Arrays.asList(paths)
    }
}