<#
Defining common functions to use the docker environment for unicorn
#>
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