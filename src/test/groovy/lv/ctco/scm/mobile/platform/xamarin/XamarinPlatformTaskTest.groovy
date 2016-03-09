/*
 * @(#)XamarinPlatformTaskTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin

import lv.ctco.scm.mobile.core.objects.Environment

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.testfixtures.ProjectBuilder

import org.junit.Test

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class XamarinPlatformTaskTest {

    @Test
    public void xamarinOnlyStandardTaskGeneration() {
        Project project = getProject()
        XamarinPlatform platform = getPlatform(project)

        XamarinExtension extXios = getStandardXamarinExtension()
        addStandardEnvironments(extXios)

        XandroidExtension extXand = getEmptyXandroidExtension()

        platform.validateXamarinExtension(extXios)
        //platform.validateXandroidExtension(extXand) // TODO : reimplement check

        platform.setupProjectInfoTask(null)
        platform.setupCleanTasks(extXios, extXand)
        platform.setupBuildTasks(extXios, extXand, null)
        platform.setupUIATestTasks(extXios)

        checkStandardXamarinConfiguration(project.tasks)
        assertFalse taskExists(project.tasks, 'restoreDependenciesForAndroid')
        assertFalse taskExists(project.tasks, 'applyProfileAndroidDev')
        assertFalse taskExists(project.tasks, 'applyProfileAndroidDefault')
        assertFalse taskExists(project.tasks, 'updateManifestVersion')
        assertFalse taskExists(project.tasks, 'buildAndroid')
        assertFalse taskExists(project.tasks, 'buildAndroidDev')
        assertFalse taskExists(project.tasks, 'buildAndroidDefault')
        assertFalse taskExists(project.tasks, 'cleanupBuildAndroidDev')
        assertFalse taskExists(project.tasks, 'cleanupBuildAndroidDefault')
    }

    @Test
    public void xamarinAndXandroidStandardTaskGeneration() {
        Project project = getProject()
        XamarinPlatform platform = getPlatform(project)

        XamarinExtension extXios = getStandardXamarinExtension()
        addStandardEnvironments(extXios)

        XandroidExtension extXand = getStandardXandroidExtension()
        addStandardEnvironments(extXand)

        platform.validateXamarinExtension(extXios)
        //platform.validateXandroidExtension(extXand)  // TODO : reimplement check

        platform.setupProjectInfoTask(null)
        platform.setupCleanTasks(extXios, extXand)
        platform.setupBuildTasks(extXios, extXand, null)
        platform.setupUIATestTasks(extXios)

        checkStandardXamarinConfiguration(project.tasks)
        checkStandardXandroidConfiguration(project.tasks)
        assertTrue taskExists(project.tasks, "runUnitTests")
    }

    @Test
    public void xamarinAndXandroidStandardUtTaskTaskGeneration() {
        Project project = getProject()
        XamarinPlatform platform = getPlatform(project)

        XamarinExtension extXios = getStandardXamarinExtension()
        addStandardEnvironments(extXios)
        extXios.unitTestProject = 'TestU.nunit'

        XandroidExtension extXand = getStandardXandroidExtension()
        addStandardEnvironments(extXand)

        platform.validateXamarinExtension(extXios)
        //platform.validateXandroidExtension(extXand)  // TODO : reimplement check

        platform.setupProjectInfoTask(null)
        platform.setupCleanTasks(extXios, extXand)
        platform.setupBuildTasks(extXios, extXand, null)
        platform.setupUIATestTasks(extXios)

        checkStandardXamarinConfiguration(project.tasks)
        checkStandardXandroidConfiguration(project.tasks)
        assertTrue taskExists(project.tasks, 'runUnitTests')
    }

    //

    /*
     * Project configurations
     */

    public Project getProject() {
        return ProjectBuilder.builder().build()
    }

    public XamarinPlatform getPlatform(Project project) {
        return (new XamarinPlatform(project))
    }

    public XamarinExtension getEmptyXamarinExtension() {
        return (new XamarinExtension())
    }

    public XamarinExtension getStandardXamarinExtension() {
        XamarinExtension extXios = new XamarinExtension()
        extXios.solutionFile = new File('TestX.sln')
        extXios.projectName = 'TestX'
        extXios.assemblyName = 'TestX'
        return extXios
    }

    public void addStandardEnvironments(XamarinExtension extXios) {
        extXios.addEnvironment(new Environment('DEV', 'Ad-Hoc|iPhone', new File('noPath')))
        extXios.addEnvironment(new Environment('TRAIN', 'Ad-Hoc|iPhone', new File('noPath')))
    }

    public XandroidExtension getStandardXandroidExtension() {
        XandroidExtension extXand = new XandroidExtension()
        extXand.solutionFile = new File('TestA.sln')
        extXand.projectFile = new File('TestA.csproj')
        extXand.projectName = 'TestA'
        extXand.assemblyName = 'TestA'
        extXand.signingCertificateAlias = 'aliasA'
        return extXand
    }

    public void addStandardEnvironments(XandroidExtension extXand) {
        extXand.addEnvironment(new Environment('DEV', 'Release', new File('noPath')))
        extXand.addEnvironment(new Environment('Train', 'Release', new File('noPath')))
    }

    public void checkStandardXamarinConfiguration(TaskContainer tasks) {

        assertTrue taskExists(tasks, 'projectInfo')
        assertTrue taskExists(tasks, 'restoreDependencies')
        assertTrue taskExists(tasks, 'restoreDependenciesForIos')
        assertTrue taskExists(tasks, 'applyProfileDev')
        assertTrue taskExists(tasks, 'applyProfileTrain')
        //assertTrue taskExists(tasks, 'updateBundleVersion')
        assertTrue taskExists(tasks, 'updateVersionDev')
        assertTrue taskExists(tasks, 'updateVersionTrain')
        assertTrue taskExists(tasks, 'build')
        assertTrue taskExists(tasks, 'buildIos')
        assertTrue taskExists(tasks, 'buildDev')
        assertTrue taskExists(tasks, 'buildTrain')
        assertTrue taskExists(tasks, 'cleanupBuildDev')
        assertTrue taskExists(tasks, 'cleanupBuildTrain')

        assertTrue taskDependsOn(tasks, 'restoreDependencies', 'restoreDependenciesForIos')
        assertTrue taskDependsOn(tasks, 'build', 'buildDev')
        assertTrue taskDependsOn(tasks, 'build', 'buildTrain')
        assertTrue taskDependsOn(tasks, 'buildIos', 'buildDev')
        assertTrue taskDependsOn(tasks, 'buildIos', 'buildTrain')

        assertTrue taskDependsOn(tasks, 'buildDev', 'restoreDependenciesForIos')
        assertTrue taskDependsOn(tasks, 'buildDev', 'applyProfileDev')
        //assertTrue taskDependsOn(tasks, 'buildDev', 'updateBundleVersion')
        assertTrue taskDependsOn(tasks, 'buildDev', 'updateVersionDev')
        assertTrue taskDependsOn(tasks, 'buildTrain', 'restoreDependenciesForIos')
        assertTrue taskDependsOn(tasks, 'buildTrain', 'applyProfileTrain')
        //assertTrue taskDependsOn(tasks, 'buildTrain', 'updateBundleVersion')
        assertTrue taskDependsOn(tasks, 'buildTrain', 'updateVersionTrain')

        assertTrue taskFinalizedBy(tasks, 'buildDev', 'cleanupBuildDev')
        assertTrue taskFinalizedBy(tasks, 'buildTrain', 'cleanupBuildTrain')

    }

    public void checkStandardXandroidConfiguration(TaskContainer tasks) {

        assertTrue taskExists(tasks, 'projectInfo')
        assertTrue taskExists(tasks, 'restoreDependencies')
        assertTrue taskExists(tasks, 'restoreDependenciesForAndroid')
        assertTrue taskExists(tasks, 'applyProfileAndroidDev')
        assertTrue taskExists(tasks, 'applyProfileAndroidTrain')
        assertTrue taskExists(tasks, 'updateManifestVersion')
        assertTrue taskExists(tasks, 'build')
        assertTrue taskExists(tasks, 'buildAndroid')
        assertTrue taskExists(tasks, 'buildAndroidDev')
        assertTrue taskExists(tasks, 'buildAndroidTrain')
        assertTrue taskExists(tasks, 'cleanupBuildAndroidDev')
        assertTrue taskExists(tasks, 'cleanupBuildAndroidTrain')

        assertTrue taskDependsOn(tasks, 'restoreDependencies', 'restoreDependenciesForAndroid')
        assertTrue taskDependsOn(tasks, 'build', 'buildAndroidDev')
        assertTrue taskDependsOn(tasks, 'build', 'buildAndroidTrain')
        assertTrue taskDependsOn(tasks, 'buildAndroid', 'buildAndroidDev')
        assertTrue taskDependsOn(tasks, 'buildAndroid', 'buildAndroidTrain')

        assertTrue taskDependsOn(tasks, 'buildAndroidDev', 'restoreDependenciesForAndroid')
        assertTrue taskDependsOn(tasks, 'buildAndroidDev', 'applyProfileAndroidDev')
        assertTrue taskDependsOn(tasks, 'buildAndroidDev', 'updateManifestVersion')
        assertTrue taskDependsOn(tasks, 'buildAndroidTrain', 'restoreDependenciesForAndroid')
        assertTrue taskDependsOn(tasks, 'buildAndroidTrain', 'applyProfileAndroidTrain')
        assertTrue taskDependsOn(tasks, 'buildAndroidTrain', 'updateManifestVersion')

        assertTrue taskFinalizedBy(tasks, 'buildAndroidDev', 'cleanupBuildAndroidDev')
        assertTrue taskFinalizedBy(tasks, 'buildAndroidTrain', 'cleanupBuildAndroidTrain')

    }

    public XandroidExtension getEmptyXandroidExtension() {
        return (new XandroidExtension())
    }

    public boolean taskExists(TaskContainer tasks, String taskName) {
        if (tasks.findByName(taskName) == null ) {
            return false
        } else {
            return true
        }
    }

    public boolean taskDependsOn(TaskContainer tasks, String taskTarget, String taskDependency) {
        Task taskT = tasks.findByName(taskTarget)
        for (Object taskD : taskT.getDependsOn().toArray()) {
            if ( taskD instanceof Task && taskD.getName().equals(taskDependency) ) {
                return true
            }
        }
        return false
    }

    public boolean taskFinalizedBy(TaskContainer tasks, String taskTarget, String taskFinalizer) {
        Task taskT = tasks.findByName(taskTarget)
        for (Object taskF : taskT.getFinalizedBy().getDependencies(taskT).toArray()) {
            if ( taskF instanceof Task && taskF.getName().equals(taskFinalizer)) {
                return true
            }
        }
        return false
    }

}
