param(
  [Parameter(Mandatory=$true)]
  [string]$Workspace
)

$ErrorActionPreference = "Stop"

function Fail([string]$Message) {
  Write-Host "SCHEDULE_VERIFY_FAILED: $Message"
  exit 1
}

function Normalize-JsonValue($Value) {
  if ($null -eq $Value) { return $null }

  if ($Value -is [System.Management.Automation.PSCustomObject]) {
    $ordered = [ordered]@{}
    foreach ($p in ($Value.PSObject.Properties | Sort-Object Name)) {
      $ordered[$p.Name] = Normalize-JsonValue $p.Value
    }
    return [pscustomobject]$ordered
  }

  if (($Value -is [System.Collections.IEnumerable]) -and -not ($Value -is [string])) {
    $items = @()
    foreach ($item in $Value) {
      $items += ,(Normalize-JsonValue $item)
    }
    return $items
  }

  return $Value
}

$productionPath = Join-Path $Workspace "app\src\main\assets\schedule.json"
$expectedPath = Join-Path $PSScriptRoot "expected-schedule.json"

if (-not (Test-Path -LiteralPath $productionPath)) {
  Fail "Missing production schedule: $productionPath"
}
if (-not (Test-Path -LiteralPath $expectedPath)) {
  Fail "Missing package oracle: $expectedPath"
}

try {
  $production = Get-Content -LiteralPath $productionPath -Raw | ConvertFrom-Json
  $expected = Get-Content -LiteralPath $expectedPath -Raw | ConvertFrom-Json
} catch {
  Fail "Invalid JSON: $($_.Exception.Message)"
}

$productionCanonical = (Normalize-JsonValue $production) | ConvertTo-Json -Depth 100 -Compress
$expectedCanonical = (Normalize-JsonValue $expected) | ConvertTo-Json -Depth 100 -Compress

if ($productionCanonical -ne $expectedCanonical) {
  Fail "schedule.json differs from the approved V1 oracle."
}

foreach ($audio in $expected.audioRotation) {
  $path = Join-Path (Join-Path $Workspace "AUDIOS") $audio
  if (-not (Test-Path -LiteralPath $path)) {
    Fail "Missing audio material: AUDIOS/$audio"
  }
}

$visuals = @()
foreach ($dayProperty in $expected.week.PSObject.Properties) {
  foreach ($alert in $dayProperty.Value.alerts) {
    if ($alert.visual) { $visuals += $alert.visual }
  }
}
$visuals = $visuals | Sort-Object -Unique

foreach ($visual in $visuals) {
  $path = Join-Path (Join-Path $Workspace "VISUAL") $visual
  if (-not (Test-Path -LiteralPath $path)) {
    Fail "Missing visual material: VISUAL/$visual"
  }
}

if ($expected.week.SATURDAY.alerts.Count -ne 0 -or $expected.week.SUNDAY.alerts.Count -ne 0) {
  Fail "Oracle itself contains weekend alerts."
}

Write-Host "SCHEDULE_VERIFY_OK"
exit 0
