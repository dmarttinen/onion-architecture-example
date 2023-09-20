package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.NuGetPublishStep
import jetbrains.buildServer.configs.kotlin.buildSteps.nuGetPublish
import jetbrains.buildServer.configs.kotlin.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'IntegrationBuild'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("IntegrationBuild")) {
    expectSteps {
        powerShell {
            name = "Enable LocalDB"
            formatStderrAsError = true
            scriptMode = script {
                content = """
                    # Download the SqlLocalDB.msi installer from the Microsoft website
                    ${'$'}installerPath = "${'$'}env:TEMP\SqlLocalDB.msi"
                    Invoke-WebRequest "https://download.microsoft.com/download/7/c/1/7c14e92e-bdcb-4f89-b7cf-93543e7112d1/SqlLocalDB.msi" -OutFile ${'$'}installerPath
                    
                    # Install SqlLocalDB
                    Start-Process -FilePath msiexec -ArgumentList "/i `"${'$'}installerPath`" /qn IACCEPTSQLLOCALDBLICENSETERMS=YES" -Wait
                    
                    # Remove the installer file
                    Remove-Item ${'$'}installerPath
                    
                    # Reload env vars
                    ${'$'}env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
                    
                    Write-Host "Starting LocalDB"
                    sqllocaldb start mssqllocaldb
                """.trimIndent()
            }
        }
        powerShell {
            name = "Build.ps1"
            scriptMode = script {
                content = """
                    dotnet tool install Octopus.DotNet.Cli --global
                    
                    # Reload env vars
                    ${'$'}env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
                    
                    # Run build script
                    . .\build.ps1 ; CIBuild
                    
                    
                    dotnet tool install --global dotnet-reportgenerator-globaltool
                    
                    ${'$'}coverageFile = "build\test\**\In\**\coverage.cobertura.xml"
                    ${'$'}outputDir = "build\reports"
                    reportgenerator "-reports:${'$'}coverageFile" "-targetdir:${'$'}outputDir"
                """.trimIndent()
            }
        }
        nuGetPublish {
            name = "Publish Packages"
            toolPath = "%teamcity.tool.NuGet.CommandLine.6.1.0%"
            packages = "**/*.nupkg"
            serverUrl = "%teamcity.nuget.feed.httpAuth.OnionArchitectureDotnet7ContainerApps.Onion_Architecture_Container_Apps.v3%"
            apiKey = "%teamcity.nuget.feed.api.key%"
        }
    }
    steps {
        update<NuGetPublishStep>(2) {
            clearConditions()
            apiKey = "credentialsJSON:dd6dcea7-88fc-42ac-9afe-d3ead71deecc"
        }
    }
}
