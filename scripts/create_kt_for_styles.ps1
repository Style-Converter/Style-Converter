$ErrorActionPreference = 'Stop'

function Ensure-File([string]$path) {
	if (-not (Test-Path -LiteralPath $path)) {
		New-Item -ItemType File -Force -Path $path | Out-Null
	}
}

function Sanitize([string]$name) {
	return ($name -replace '[<>:"/\\|?*]','-')
}

$root = 'src/main/kotlin/app/parsing/css/styles'
if (-not (Test-Path -LiteralPath $root)) {
	Write-Error "Styles root not found: $root"
}

$dirs = Get-ChildItem -Path $root -Directory -Recurse
foreach ($d in $dirs) {
	$leaf = Sanitize $d.Name
	$ktPath = Join-Path -Path $d.FullName -ChildPath ("$leaf.kt")
	Ensure-File -path $ktPath
}

Write-Host "KT files created for $($dirs.Count) directories."

