package com.linkedin.gradle.python.plugin

import com.linkedin.gradle.python.PythonExtension
import com.linkedin.gradle.python.util.values.PyGradleConfiguration
import com.linkedin.gradle.python.util.values.PyGradleTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration

import static PyGradleConfiguration.*

abstract class AbstractPluginBase implements Plugin<Project> {

    public static final String PYTHON_EXTENSION_NAME = 'python'

    public Project project
    public PythonExtension settings

    @Override
    void apply(Project target) {
        this.project = target

        project.plugins.apply('base')

        settings = addGetExtensionLocal(PYTHON_EXTENSION_NAME, PythonExtension)
        applyTo(target)
    }

    abstract void applyTo(Project project)

    /**
     * This adds the plugin to the local project only if it doesn't already exist
     * @param project project object
     * @param plugin plug class to add
     * @return
     */
    protected <T extends Plugin> T addPluginLocal(Class<T> plugin) {
        if (!project.plugins.withType(plugin)) {
            project.plugins.apply(plugin)
        }
    }
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

    private Map<String, ?> fillTaskMap(Map<String, ?> task) {
        def taskid = task.get('name')
        if (taskid instanceof PyGradleTask) {
            task['group'] = taskid.group
            task['description'] = taskid.description
        }
        return task
    }

    protected Task addTaskLocal(Map<String, ?> task) {
        def taskFilled = fillTaskMap(task)

        if (!project.tasks.findByName(taskFilled.get('name').toString())) {
            project.tasks.create(taskFilled)
        } else {
            null
        }
    }

    protected Task addTaskLocal(Map<String, ?> task, Closure configureClosure) {
        def taskFilled = fillTaskMap(task)

        if (!project.tasks.findByName(taskFilled.get('name').toString())) {
            project.tasks.create(taskFilled).configure(configureClosure)
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

    protected Configuration createConfiguration(PyGradleConfiguration confName) {
        if (!project.configurations.findByName(confName.value)) {
            return project.configurations.create(confName.value)
        } else {
            null
        }
    }
    /**
     * Checks to see if the specified extension exists.  If it does not, it creates it and returns it.
     * if the extension already exists, it returns the existing one.
     * @param name
     * @param ext
     * @return
     */
    protected <T> T addGetExtensionLocal(String name, Class<T> ext) {
        def tst = project.extensions.findByName(name)
        if (tst == null) {
            return project.extensions.create(name, ext, project)
        } else {
            return tst as T
        }
    }


    protected void addDependency(PyGradleConfiguration confName, dependency) {
        project.dependencies.add(confName.value, dependency)
    }

    /**
     * Creates a taskA.dependsOn B relationship, but only if it can find both task A and task B
     * @param aTask
     * @param bTask
     */
    protected void aDependsOnB(PyGradleTask aTask, PyGradleTask bTask) {
        def atmp = project.tasks.findByName(aTask.value)
        def btmp = project.tasks.findByName(bTask.value)

        if ((atmp != null) && (btmp != null)) {
            atmp.dependsOn << btmp
        }
    }

    /**
     * Add vended build and test dependencies to projects that apply this plugin.
     * Notice that virtualenv contains the latest versions of setuptools,
     * pip, and wheel, vended in. Make sure to use versions we can actually
     * use based on various restrictions. For example, pex may limit the
     * highest version of setuptools used. Provide the dependencies in the
     * best order they should be installed in setupRequires configuration.
     */
    def createDependenciesVenv(PythonExtension settings) {
        addDependency(BOOTSTRAP_REQS, settings.forcedVersions['virtualenv'])
        addDependency(SETUP_REQS, settings.forcedVersions['pip'])
        addDependency(SETUP_REQS, settings.forcedVersions['setuptools'])
        addDependency(SETUP_REQS, settings.forcedVersions['setuptools-git'])

        pinForcedVersions(settings)
    }

    def createDependenciesSphinx(PythonExtension settings) {
        addDependency(BUILD_REQS, settings.forcedVersions['Sphinx'])

        pinForcedVersions(settings)
    }

    def createDependenciesPython(PythonExtension settings) {
        addDependency(SETUP_REQS, settings.forcedVersions['appdirs'])
        addDependency(SETUP_REQS, settings.forcedVersions['packaging'])
        addDependency(SETUP_REQS, settings.forcedVersions['wheel'])
        addDependency(SETUP_REQS, settings.forcedVersions['pbr'])
        addDependency(BUILD_REQS, settings.forcedVersions['flake8'])
        addDependency(TEST, settings.forcedVersions['pytest'])
        addDependency(TEST, settings.forcedVersions['pytest-cov'])
        addDependency(TEST, settings.forcedVersions['pytest-xdist'])

        pinForcedVersions(settings)
    }

    /**
    * To prevent base dependencies, such as setuptools, from installing/reinstalling, we will
    * pin their versions to values in the extension.forcedVersions map, which will contain known
    * good versions that satisfy all the requirements.
    */
    def pinForcedVersions(PythonExtension settings) {
        def dependencyResolveDetails = new PyGradleDependencyResolveDetails(settings.forcedVersions)
        //noinspection GroovyAssignabilityCheck
        project.configurations.each { configuration ->
            configuration.resolutionStrategy.eachDependency(dependencyResolveDetails)
        }
    }
}
