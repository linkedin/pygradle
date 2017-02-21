package com.linkedin.gradle.python.plugin

import com.linkedin.gradle.python.util.StandardTextValuesConfiguration
import com.linkedin.gradle.python.util.StandardTextValuesTasks
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration

abstract class AbstractPluginBase implements Plugin<Project> {

    Project project

    abstract void apply(Project project)

    /**
     * This adds the plugin to the local project only if it doesn't already exist
     * @param project project object
     * @param plugin plug class to add
     * @return
     */
//    protected <T extends Plugin> T addPluginLocal(Class<T> plugin){
//        if (!project.plugins.withType(plugin)){
//            project.plugins.apply(plugin)
//        }
//    }
//
//    protected <T extends Plugin> T addPluginRootProject(Class<T> plugin){
//        project.rootProject.plugins.apply(plugin)
//    }
//
//    protected <T extends Plugin> T addPluginSubProjects(Class<T> plugin){
//        project.subprojects { proj ->
//            proj.plugins.apply(plugin)
//        }
//        null
//    }
//
//    protected <T extends Plugin> T addPluginAllProjects(Class<T> plugin){
//        project.allprojects { proj ->
//            if (!proj.plugins.hasPlugin(plugin)) {
//                proj.plugins.apply(plugin)
//            }
//        }
//        null
//    }
//
//    protected Task addTaskIfOnRoot(Map<String, ?> task) {
//        if (project.rootProject == project) {
//            if (!project.tasks.findByName(task.get('name').toString())){
//                project.tasks.create(task)
//            }
//        }
//        null
//    }

    private Map<String, ?> fillTaskMap(Map<String, ?> task){
        def taskid = task.get('name')
        if (taskid instanceof StandardTextValuesTasks){
            task['group'] = taskid.group
            task['description'] = taskid.description
        }
        return task
    }
    protected Task addTaskLocal(Map<String, ?> task) {
        task = fillTaskMap(task)

        if (!project.tasks.findByName(task.get('name').toString())){
            project.tasks.create(task)
        } else {
            null
        }
    }

    protected Task addTaskLocal(Map<String, ?> task, Closure configureClosure) {
        task = fillTaskMap(task)

        if (!project.tasks.findByName(task.get('name').toString())){
            project.tasks.create(task).configure(configureClosure)
        } else {
            null
        }
    }

//    protected void addTaskAllProjects(Map<String, ?> task) {
//        project.allprojects {
//            if (!it.tasks.findByName(task.get('name').toString())){
//                it.tasks.create(task)
//            }
//        }
//    }

    protected Configuration createConfiguration(StandardTextValuesConfiguration confName) {
        if (!project.configurations.findByName(confName.value)){
            return project.configurations.create(confName.value)
        } else {
            null
        }
    }

    protected void addDependency(StandardTextValuesConfiguration confName, dependency) {
        project.dependencies.add(confName.value, dependency)
    }

    protected void aDependsOnB(StandardTextValuesTasks aTask, StandardTextValuesTasks bTask) {
        def atmp = project.tasks.findByName(aTask.value)
        def btmp = project.tasks.findByName(bTask.value)

        if ((atmp != null) && (btmp != null)){
            atmp.dependsOn(btmp)
        }
    }
}
