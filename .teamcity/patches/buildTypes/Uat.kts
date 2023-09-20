package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.BuildStep
import jetbrains.buildServer.configs.kotlin.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'Uat'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("Uat")) {
    expectSteps {
        step {
            name = "Deploy To UAT"
            type = "octopus.deploy.release"
            param("octopus_space_name", "%OctoSpaceName%")
            param("octopus_waitfordeployments", "true")
            param("octopus_version", "3.0+")
            param("octopus_host", "%OctoURL%")
            param("octopus_project_name", "%OctoProject%")
            param("octopus_deploymenttimeout", "00:30:00")
            param("octopus_deployto", "UAT")
            param("secure:octopus_apikey", "%OctoApiKey%")
            param("octopus_releasenumber", "%build.number%")
        }
    }
    steps {
        update<BuildStep>(0) {
            clearConditions()
            param("secure:octopus_apikey", "credentialsJSON:5923eadd-621b-4e8a-9b46-42d1fbbc65c6")
        }
    }
}
