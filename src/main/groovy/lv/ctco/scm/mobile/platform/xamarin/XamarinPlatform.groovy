/*
 * @(#)XamarinPlatform.groovy
 *
 * Copyright C.T.Co Ltd, 33 Meistaru Street, Riga LV-1076, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin

import lv.ctco.scm.mobile.core.objects.TaskGroup
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class XamarinPlatform {

    private static final Logger logger = Logging.getLogger(XamarinPlatform.class)

    public static final String NAME = "xamarin"

    protected Project project

    private String releaseVersionIos
    private String releaseVersionAndroid

    XamarinPlatform(Project project) {
        this.project = project
    }

    void configure(XamarinConfiguration extXios, XandroidConfiguration extXand) {
        if (extXios.solutionFile == null) {
            if (XamarinUtil.getSlnCount(project.getProjectDir()) == 1) {
                String[] extensions = ["sln"]
                extXios.solutionFile = FileUtils.listFiles(project.projectDir, extensions, false)[0]
            } else {
                throw new IOException("solutionFile for ctcoMobile.xamarin extension is not defined")
            }
        }
        if (extXios.getProjectName() == null) {
            String projectBaseName = FilenameUtils.getBaseName(extXios.getSolutionFile().getName())
            extXios.setProjectName(projectBaseName+".iOS")
            logger.info("Guessing iOS project name as '"+extXios.getProjectName()+"'")
            File projectDir = new File(extXios.getProjectName())
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                logger.error("Can not find iOS project directory")
            }
        }

        if (extXios.solutionFile.exists()) {
            logger.info("Parsing solution '"+extXios.getSolutionFile()+"'...")
            SolutionParser sp = new SolutionParser(extXios.solutionFile)
            Solution solution = sp.parse()

            if (!solution.containsProject(extXios.getProjectName())) {
                throw new IOException("Project "+extXios.getProjectName()+" does not exist in solution "+extXios.getSolutionFile())
            }

            SlnProjectSection sectionXios = solution.getProject(extXios.projectName)
            File csprojXiosFile = new File(extXios.solutionFile.parentFile.absolutePath, sectionXios.buildFilePath)
            Csproj csprojXios = new CsprojParser(csprojXiosFile).parse()

            if (extXios.automaticConfiguration) {
                performAutomaticEnvironmentConfiguration(extXios, solution)
            }

            validateXamarinExtension(extXios)

            logger.info(extXios.toString())
            logger.info(extXand.toString())

            // Getting release versions from according csproj files
            releaseVersionIos = XamarinUtil.getReleaseVersion(csprojXios)
            if (extXand.isValid()) {
                SolutionParser spXand = new SolutionParser(extXand.solutionFile)
                Solution slnXand = spXand.parse()
                if (!slnXand.containsProject(extXand.projectName)) {
                    throw new IOException("Project "+extXand.projectName+" does not exist in solution "+extXand.solutionFile)
                }
                SlnProjectSection sectionXand = solution.getProject(extXand.projectName)
                File csprojXandFile = new File(extXand.solutionFile.parentFile.absolutePath, sectionXand.buildFilePath)
                Csproj csprojXand = new CsprojParser(csprojXandFile).parse()
                releaseVersionAndroid = XamarinUtil.getReleaseVersion(csprojXand)
            }

            setupCleanTasks(extXios, extXand)
            setupBuildTasks(extXios, extXand)

            if (extXios.getEnvironments().size() == 0 && releaseVersionAndroid != null) {
                setupProjectInfoTask(releaseVersionAndroid)
            } else {
                setupProjectInfoTask(releaseVersionIos)
            }
            getOrCreateIncrementVersionTask(project, extXios.getSolutionFile(), csprojXios)
        } else {
            logger.info("Defined solution file was not found! Will not configure build tasks...")
            logger.info(extXios.toString())
        }
    }

    protected void validateXamarinExtension(XamarinConfiguration extXios) throws IOException {
        if (!extXios.isValid()) {
            throw new IOException("xamarin extension is invalid")
        }
    }

    protected void setupProjectInfoTask(String releaseVersion) {
        XamarinTasks.getOrCreateProjectInfoTask(project, releaseVersion)
    }

    protected void setupCleanTasks(XamarinConfiguration extXios, XandroidConfiguration extXand) {
        Task cleanTask = XamarinTasks.getOrCreateCleanTask(project)
        Task cleanIos = XamarinTasks.getOrCreateCleanIosTask(project, extXios.getSolutionFile())
        cleanTask.dependsOn cleanIos
        if (extXand.isValid()) {
            Task cleanAnd = XamarinTasks.getOrCreateCleanAndroidTask(project, extXand.getSolutionFile())
            cleanTask.dependsOn cleanAnd
        }
    }

    protected void setupBuildTasks(XamarinConfiguration extXios, XandroidConfiguration extXand) {
        Task buildTask = XamarinTasks.getOrCreateBuildTask(project)

        Task buildIosTask = XamarinTasks.getOrCreateBuildIosTask(project)
        buildTask.dependsOn(buildIosTask)

        Task dependencyRestoreTask = XamarinTasks.getOrCreateRestoreDependenciesTask(project)
        Task dependencyRestoreIosTask = XamarinTasks.getOrCreateRestoreDependenciesIosTask(project, extXios.getSolutionFile(), null)
        dependencyRestoreTask.dependsOn(dependencyRestoreIosTask)

        for (Environment _env : extXios.getEnvironments()) {
            Task envTask = XamarinTasks.getOrCreateBuildIosEnvTask(project, _env, extXios)
            Task profilingTask = getOrCreateProfileIosEnvTask(project, _env, extXios)
            Task updateVersionTask = XamarinTasks.getOrCreateUpdateVersionIosEnvTask(project, _env, extXios, releaseVersionIos)
            envTask.dependsOn(dependencyRestoreIosTask)
            if (extXios.skipUpdateVersionForAppstoreConfiguration && "AppStore".equals(_env.getConfiguration())) {
                logger.info("Disabling updateVersion task for $envTask.name as requested by configuration!")
            } else {
                envTask.dependsOn(updateVersionTask)
            }
            envTask.dependsOn(profilingTask)
            envTask.finalizedBy(XamarinTasks.getOrCreateRevertProfileTask(project, getCamelCase(_env.getName())))
            buildIosTask.dependsOn(envTask)
            profilingTask.mustRunAfter(dependencyRestoreIosTask)
            updateVersionTask.mustRunAfter(profilingTask)
        }

        Task unitTestTask = XamarinTasks.getOrCreateRunUnitTestsTask(project, extXios.unitTestProject)
        unitTestTask.dependsOn(dependencyRestoreTask)

        if (extXand.isValid()) {
            Task buildAndroidTask = XamarinTasks.getOrCreateBuildAndroidTask(project)
            buildTask.dependsOn(buildAndroidTask)

            Task dependencyRestoreAndroidTask = XamarinTasks.getOrCreateRestoreDependenciesAndroidTask(project, extXand.getSolutionFile(), null)
            dependencyRestoreTask.dependsOn(dependencyRestoreAndroidTask)

            extXand.getEnvironments().each { Environment _env ->
                Task envTask = project.task type: BuildAndroidTask, "buildAndroid"+getCamelCase(_env.getName()), {
                    group = 'Mobile Build'
                    description = "Builds Android "+_env.getName()+" environment with "+_env.getConfiguration()+" configuration"
                    env = _env
                    projectFile = extXand.getProjectFile()
                    projectName = extXand.projectName
                    signingKeystore = extXand.signingKeystore
                    signingCertificateAlias = extXand.signingCertificateAlias
                }
                Task profilingTask = project.task type: ProfilingTask, "applyProfileAndroid"+getCamelCase(_env.getName()), {
                    description = "Profiles files for Android "+_env.getName()+" environment"
                    projectDir = new File(extXand.projectName)
                    profiles = extXand.getSpecificProfiles(_env.getName(), "build")
                }
                Task versionUpdateTask = XamarinTasks.getOrCreateUpdateVersionAndroidTask(project, _env, extXand, releaseVersionAndroid)
                //
                envTask.dependsOn(dependencyRestoreAndroidTask)
                envTask.dependsOn(versionUpdateTask)
                envTask.dependsOn(profilingTask)
                envTask.finalizedBy(XamarinTasks.getOrCreateRevertProfileTask(project, "Android"+getCamelCase(_env.getName())))
                buildAndroidTask.dependsOn(envTask)
                profilingTask.mustRunAfter(dependencyRestoreAndroidTask)
                versionUpdateTask.mustRunAfter(profilingTask)
            }
        }
    }

    void performAutomaticEnvironmentConfiguration(XamarinConfiguration extXios, Solution solution) {
        Set<String> solutionConfigurations = solution.solutionConfigurations
        if (extXios.getEnvironments().size() == 0) {
            // Use Ad-Hoc or Release configuration when there are no specified environment configurations
            Environment defaultEnv = new Environment()
            defaultEnv.setName("DEFAULT")
            defaultEnv.setPlatform("iPhone")
            if (solutionConfigurations.contains('Ad-Hoc|iPhone')) {
                defaultEnv.setConfiguration("Ad-Hoc")
            } else if (solutionConfigurations.contains('Release|iPhone')) {
                defaultEnv.setConfiguration("Release")
            } else {
                throw new IOException("No defined or default environments detected")
            }
            extXios.addEnvironment(defaultEnv)
        }
    }

    private static String getCamelCase(String string) {
        return StringUtils.capitalize(string.toLowerCase())
    }

    private static Task getOrCreateProfileIosEnvTask(Project project, Environment _env, XamarinConfiguration extXios) {
        String taskName = "applyProfile"+getCamelCase(_env.getName())
        Task existingTask = project.getTasks().findByName(taskName)
        if (existingTask == null) {
            ProfilingTask newTask = project.getTasks().create(taskName, ProfilingTask)
            newTask.setDescription("Profiles files for iOS "+ _env.getName()+" environment")
            newTask.setProjectDir(new File(extXios.projectName))
            newTask.setProfiles(extXios.getSpecificProfiles(_env.getName(), "build"))
            return newTask
        } else {
            return existingTask
        }
    }

    private static Task getOrCreateIncrementVersionTask(Project project, File sln, Csproj csproj) {
        String taskName = "incrementProjectVersion"
        Task existingTask = project.getTasks().findByName(taskName)
        if (existingTask == null) {
            IncrementProjectVersionTask newTask = project.getTasks().create(taskName, IncrementProjectVersionTask.class)
            newTask.setGroup(TaskGroup.UTILITY.getLabel())
            newTask.setDescription("Increments version in .sln and .csproj files")
            newTask.setSolutionFile(sln)
            newTask.setCsproj(csproj)
            return newTask
        } else {
            return existingTask
        }
    }

}
