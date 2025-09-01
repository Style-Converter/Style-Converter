$ErrorActionPreference = 'Stop'
$root = 'src/main/kotlin/app/parsing/css/styles'
if (-not (Test-Path -LiteralPath $root)) { Write-Error "Root not found: $root" }

function Sanitize([string]$name) { return ($name -replace '[<>:"/\\|?*]','-') }

$dirs = Get-ChildItem -LiteralPath $root -Directory -Recurse
$total = $dirs.Count
$present = 0
$missing = @()

foreach ($d in $dirs) {
	$leaf = Sanitize $d.Name
	$kt = Join-Path -Path $d.FullName -ChildPath ("$leaf.kt")
	if (Test-Path -LiteralPath $kt) { $present++ } else { $missing += $kt }
}

Write-Output ("Directories: " + $total)
Write-Output ("KT present: " + $present)
Write-Output ("KT missing: " + ($total - $present))
if ($missing.Count -gt 0) {
	Write-Output "First missing:"
	$missing | Select-Object -First 20 | ForEach-Object { Write-Output $_ }
}

