# Desteklenen tum Minecraft surumleri icin jar uretir.
# Her iki jar da ayni mod surumunu tasir; surum sayaci en sonda bir kez artar.
# Cikti: build/fabric/<mc surumu>/libs/noobs-container-searcher-fabric-mc<mc surumu>.jar
$ErrorActionPreference = "Stop"
$versions = Get-ChildItem "$PSScriptRoot\gradle\mc-*.properties" |
	ForEach-Object { $_.BaseName -replace '^mc-', '' }

foreach ($version in $versions) {
	Write-Host "==> $version derleniyor" -ForegroundColor Cyan
	& "$PSScriptRoot\gradlew.bat" -p $PSScriptRoot build "-Pmc=$version" -PnoBump --console=plain
	if ($LASTEXITCODE -ne 0) {
		throw "$version derlemesi basarisiz (cikis kodu $LASTEXITCODE)"
	}
}

Write-Host "==> mod_version artiriliyor" -ForegroundColor Cyan
& "$PSScriptRoot\gradlew.bat" -p $PSScriptRoot bumpModVersion --console=plain
if ($LASTEXITCODE -ne 0) {
	throw "mod_version artirilamadi (cikis kodu $LASTEXITCODE)"
}

Write-Host "`nUretilen jar dosyalari:" -ForegroundColor Green
Get-ChildItem "$PSScriptRoot\build\fabric\*\libs\*.jar" |
	Where-Object { $_.Name -notmatch 'sources' } |
	Sort-Object LastWriteTime |
	Select-Object Name, Length, LastWriteTime
