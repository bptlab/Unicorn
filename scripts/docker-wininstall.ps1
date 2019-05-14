<#
Install and reset script for docker environment for Unicorn
#>

. "$($PWD)/env-commons.ps1"

delExistingVolume($UnicornVolume)
createVolume($UnicornVolume)

buildImage($UnicornImage)

writeInfo("Prebuild environment")
startUnicorn -imageName $UnicornImage -containerName $UnicornContainer -mountDirectory $UnicornMount -volumeName $UnicornVolume
buildSrcCode($UnicornContainer)
deployApp($UnicornContainer)