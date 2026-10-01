param(
  [Parameter(Mandatory=$true)]
  [string]$Workspace
)

$ErrorActionPreference = "Stop"

function Fail([string]$Message) {
  Write-Host "APK_VERIFY_FAILED: $Message"
  exit 1
}

$apk = Join-Path $Workspace "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path -LiteralPath $apk)) {
  Fail "Missing debug APK: $apk"
}

Add-Type -AssemblyName System.IO.Compression.FileSystem

$required = @(
  "assets/schedule.json",
  "assets/1.mp3",
  "assets/2.mp3",
  "assets/3.mp3",
  "assets/COMER.png",
  "assets/CORRAL.png",
  "assets/DORMIR.png",
  "assets/DUCHA.png",
  "assets/GIMNASIO.png",
  "assets/IRSE.png",
  "assets/PERSONAL.png",
  "assets/TAREA.png",
  "assets/TRASTES.png"
)

$zip = [System.IO.Compression.ZipFile]::OpenRead($apk)
try {
  $names = @{}
  foreach ($entry in $zip.Entries) {
    $names[$entry.FullName] = $true
  }

  foreach ($name in $required) {
    if (-not $names.ContainsKey($name)) {
      Fail "APK does not contain $name"
    }
  }
} finally {
  $zip.Dispose()
}

Write-Host "APK_VERIFY_OK"
exit 0
