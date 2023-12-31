package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.BuildStep
import jetbrains.buildServer.configs.kotlin.buildSteps.DotnetVsTestStep
import jetbrains.buildServer.configs.kotlin.buildSteps.dotnetVsTest
import jetbrains.buildServer.configs.kotlin.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'Tdd'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("Tdd")) {
    expectSteps {
        step {
            name = "Create and Deploy Release"
            type = "octopus.create.release"
            param("secure:octopus_apikey", "%OctoApiKey%")
            param("octopus_releasenumber", "%build.number%")
            param("octopus_additionalcommandlinearguments", "--variable=ResourceGroupName:%TDD-Resource-Group%-%build.number% --variable=container_app_name:%TDD-App-Name%")
            param("octopus_space_name", "%OctoSpaceName%")
            param("octopus_waitfordeployments", "true")
            param("octopus_version", "3.0+")
            param("octopus_host", "%OctoURL%")
            param("octopus_project_name", "%OctoProject%")
            param("octopus_deploymenttimeout", "00:30:00")
            param("octopus_deployto", "TDD")
            param("octopus_git_ref", "%teamcity.build.branch%")
        }
        powerShell {
            name = "Get Container App URL"
            scriptMode = script {
                content = """
                    Invoke-WebRequest -Uri https://aka.ms/installazurecliwindows -OutFile .\AzureCLI.msi
                    Start-Process msiexec.exe -Wait -ArgumentList '/I AzureCLI.msi /quiet'
                    Remove-Item .\AzureCLI.msi
                    ${'$'}env:PATH += ";C:\Program Files (x86)\Microsoft SDKs\Azure\CLI2\wbin"
                    
                    
                    az config set extension.use_dynamic_install=yes_without_prompt
                    # Log in to Azure
                    az login --service-principal --username %AzAppId% --password %AzPassword% --tenant %AzTenant%
                    ${'$'}containerAppURL = az containerapp show --resource-group %TDD-Resource-Group%-%build.number% --name %TDD-App-Name% --query properties.configuration.ingress.fqdn
                    ${'$'}containerAppURL = ${'$'}containerAppURL -replace '"', ''
                    Write-Host "url retrieved from AZ: ${'$'}containerAppURL"
                    [System.Environment]::SetEnvironmentVariable("containerAppURL", ${'$'}containerAppURL, "User")
                    Write-Host "ContainerAppURL after retrieval: ${'$'}env:containerAppURL"
                """.trimIndent()
            }
        }
        powerShell {
            name = "Download Acceptance Test Package"
            scriptMode = script {
                content = """
                    ${'$'}nupkgPath = "build/ChurchBulletin.AcceptanceTests.%build.number%.nupkg"
                    ${'$'}destinationPath = "."
                    
                    Add-Type -AssemblyName System.IO.Compression.FileSystem
                    
                    [System.IO.Compression.ZipFile]::ExtractToDirectory(${'$'}nupkgPath, ${'$'}destinationPath)
                    ${'$'}currentPath = (Get-Location).Path
                    # Set the download URL for the Chrome driver
                    ${'$'}chromeDriverUrl = "http://chromedriver.storage.googleapis.com/114.0.5735.90/chromedriver_win32.zip"
                    ${'$'}chromeDriverPath = "./chromedriver.zip"
                    
                    # Download the Chrome driver
                    Invoke-WebRequest -Uri ${'$'}chromeDriverUrl -OutFile ${'$'}chromeDriverPath
                    
                    ${'$'}chromedriverdestinationPath = "C:\SeleniumWebDrivers\ChromeDriver"
                    
                    Expand-Archive -Path ${'$'}chromeDriverPath -DestinationPath ${'$'}chromedriverdestinationPath
                    
                    # Add the Chrome driver to the PATH environment variable
                    ${'$'}env:PATH += ";${'$'}chromedriverdestinationPath"
                    
                    ${'$'}LocalTempDir = ${'$'}env:TEMP; 
                    ${'$'}ChromeInstaller = "ChromeInstaller.exe"; 
                    ${'$'}ChromeInstallerFile = "${'$'}LocalTempDir\${'$'}ChromeInstaller"; 
                    ${'$'}WebClient = New-Object System.Net.WebClient; 
                    ${'$'}WebClient.DownloadFile("https://download.filepuma.com/files/web-browsers/google-chrome-64bit-/Google_Chrome_(64bit)_v114.0.5735.199.exe", ${'$'}ChromeInstallerFile); 
                    Start-Process -FilePath ${'$'}ChromeInstallerFile -Args "/silent /install" -Verb RunAs -Wait; 
                    Remove-Item ${'$'}ChromeInstallerFile
                """.trimIndent()
            }
        }
        dotnetVsTest {
            name = "Run Acceptance Tests"
            assemblies = "*AcceptanceTests.dll"
            version = DotnetVsTestStep.VSTestVersion.CrossPlatform
            platform = DotnetVsTestStep.Platform.Auto
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
    }
    steps {
        update<BuildStep>(0) {
            clearConditions()
            param("secure:octopus_apikey", "credentialsJSON:5923eadd-621b-4e8a-9b46-42d1fbbc65c6")
        }
    }
}
