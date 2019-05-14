<#
Defining common functions and constant values for the docker environment for unicorn
#>
$UnicornVolume = "UnicornBPT"
$UnicornImage = "bpt/unicorn"
$UnicornContainer = "UnicornHost"
$UnicornMount = "${PWD}/.."

function writeInfo {
    param (
        [string]$info
    )
    Write-Host -ForegroundColor Yellow "[INFO]" $info
}

function writeAttentionMSG {
    param (
        [string]$attentionMsg
    )
    Write-Host -ForegroundColor Red "[ATTENTION]" $attentionMsg
}

function writeErrorMSG {
    param (
        [string]$errorMsg
    )
    Write-Host -ForegroundColor Red "[ERROR]" $errorMsg
}

function checkForInstance () {
    param(
        [string]$checkString
    )
    writeInfo("Search for existing instance...")
    foreach ($item in $(docker ps)){
        if ($item -match $checkString){
            writeInfo("Unicorn already started")
            return $true
        }
    }
    writeInfo("There is no instance of Unicorn")
    return $false
}

function startUnicorn {
    param (
        [string]$imageName,
        [string]$containerName,
        [string]$mountDirectory,
        [string]$volumeName
    )
    if (checkForInstance($UnicornImage))
    {
        writeErrorMSG("Unicorn already running")
        Exit
    }
    writeInfo("Start an instance of Unicorn...")
    docker run --rm -d -i -t --name $containerName -v ${volumeName}:/usr/local/Tomcat7 -v ${mountDirectory}:/home/Unicorn/ -p 8080:8080 -p 3306:3306 $imageName
    writeInfo("Unicorn started")
}

function killUnicorn (){
    param(
        [bool]$doChecking = $true,
        [string]$imageName,
        [string]$containerName
    )
    [bool]$anyRun = $false
    if ($doChecking){
        $anyRun = checkForInstance($imageName)
    }
    if (($doChecking -and $anyRun) -or (-not $doChecking)){
        writeAttentionMSG("Killing all running instances of Unicorn-Image...")
        foreach ($item in $(docker ps)){
            if ($item -match $imageName){
                docker kill $containerName
            }
        }
    }
    writeInfo("Shutdown finished")
}

function checkForVolume {
    param (
        [string]$volumeName
    )
    writeInfo("Search for existing Unicorn volume...")
    foreach ($item in $(docker volume ls)){
        if ($item -match $volumeName){
            writeInfo("An Unicorn volume was found")
            return $true
        }
    }
    writeInfo("An Unicorn volume was not found")
    return $false
}

function delExistingVolume {
    param (
        [string]$volumeName
    )
    if (checkForVolume($volumeName)){
        writeAttentionMSG("Removing existing Unicorn volume...")
        if ($item -match $volumeName){
            docker volume rm $volumeName
        }
    }
    else {
        return
    }
}

function createVolume {
    param (
        [string]$volumeName
    )
    writeInfo(Create new volume for Unicorn...)
    docker volume create $volumeName     
}

function delExistingImage {
    param (
        [string]$imageName
    )
    foreach ($item in $(docker image ls -a)){
        if ($item -match $imageName){
            writeAttentionMSG("Removing existing Unicorn image: $item")
            docker image rm $UnicornImage
        }
    }
}

function buildImage {
    param (
        [string]$imageName 
    )
    writeInfo("Build Unicorn image...")
    docker build -t $imageName ..
}