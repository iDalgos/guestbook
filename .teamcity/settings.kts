import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2020.1"

project {

    buildType(BuildFrontend)
    buildType(BuildFrontendImage)
    buildType(CreateDockerImage)
    buildType(BuildBackend)

    features {
        feature {
            id = "PROJECT_EXT_2"
            type = "CloudImage"
            param("podTemplateMode", "simple")
            param("dockerImage", "jetbrains/teamcity-agent")
            param("imageInstanceLimit", "2")
            param("profileId", "kube-1")
            param("imageDescription", "Run container: jetbrains/teamcity-agent")
            param("agent_pool_id", "-2")
            param("source-id", "jetbrains/teamcity-agent")
        }
        feature {
            id = "kube-1"
            type = "CloudProfile"
            param("secure:authToken", "credentialsJSON:cbfbd7d5-5d3f-4f2b-86c6-daa2894f3643")
            param("oidcClientId", "")
            param("secure:eksSecretKey", "")
            param("description", "")
            param("enabled", "true")
            param("agentPushPreset", "")
            param("profileInstanceLimit", "")
            param("idpIssuerUrl", "")
            param("apiServerUrl", "https://172.30.102.103")
            param("eksAccessId", "")
            param("eksIAMRoleArn", "")
            param("secure:oidcClientSecret", "")
            param("profileServerUrl", "")
            param("authStrategy", "token")
            param("total-work-time", "")
            param("cloud-code", "kube")
            param("secure:oidcRefreshToken", "")
            param("secure:clientKeyData", "")
            param("eksClusterName", "")
            param("profileId", "kube-1")
            param("secure:clientCertData", "")
            param("secure:password", "")
            param("name", "openshift_tryout")
            param("namespace", "teamcity-test")
            param("next-hour", "")
            param("secure:caCertData", "")
            param("terminate-idle-time", "30")
            param("username", "")
        }
    }
}

object BuildBackend : BuildType({
    name = "build-backend"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        gradle {
            tasks = "clean build"
            buildFile = "backend/build.gradle"
            gradleWrapperPath = "backend"
        }
    }
})

object BuildFrontend : BuildType({
    name = "build-frontend"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        step {
            name = "Add NPM installer"
            type = "jonnyzzz.nvm"
        }
        step {
            type = "jonnyzzz.npm"
            enabled = false
            param("teamcity.build.workingDir", "frontend")
            param("npm_commands", """
                install
                run-script build
                test
            """.trimIndent())
        }
        script {
            name = "shell command"
            scriptContent = "npm --version"
        }
    }
})

object BuildFrontendImage : BuildType({
    name = "Build frontend image"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        dockerCommand {
            commandType = build {
                source = file {
                    path = "frontend/Dockerfile"
                }
            }
        }
    }
})

object CreateDockerImage : BuildType({
    name = "create docker image"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        dockerCommand {
            commandType = build {
                source = file {
                    path = "backend/Dockerfile"
                }
            }
        }
    }
})
