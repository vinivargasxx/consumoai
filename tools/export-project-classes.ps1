param(
    [string]$RootPath = "C:\Users\User\AndroidStudioProjects\ConsumoAI",
    [string]$OutputPath = "C:\Users\User\AndroidStudioProjects\ConsumoAI\exports\consumoai-classes.md",
    [switch]$IncludeTests
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $RootPath)) {
    throw "RootPath not found: $RootPath"
}

$outputDir = Split-Path -Path $OutputPath -Parent
if (-not (Test-Path -LiteralPath $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$sourceRoots = @(
    "app\src\main",
    "app\src\debug",
    "app\src\release"
)

if ($IncludeTests) {
    $sourceRoots += @(
        "app\src\test",
        "app\src\androidTest"
    )
}

$existingRoots = $sourceRoots |
    ForEach-Object { Join-Path $RootPath $_ } |
    Where-Object { Test-Path -LiteralPath $_ }

$files = foreach ($sourceRoot in $existingRoots) {
    Get-ChildItem -Path $sourceRoot -Recurse -File -Include *.kt,*.java |
        Where-Object {
            $_.FullName -notmatch "\\build\\" -and
            $_.FullName -notmatch "\\generated\\"
        }
}

$files = $files | Sort-Object FullName -Unique

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

"# ConsumoAI source export`n" | Set-Content -Path $OutputPath -Encoding UTF8
"Generated: $timestamp  `n" | Add-Content -Path $OutputPath -Encoding UTF8
"Include tests: $($IncludeTests.IsPresent)  `n" | Add-Content -Path $OutputPath -Encoding UTF8
"Total files: $($files.Count)`n" | Add-Content -Path $OutputPath -Encoding UTF8

foreach ($file in $files) {
    $relativePath = ($file.FullName.Substring($RootPath.Length + 1) -replace "\\", "/")
    $ext = $file.Extension.ToLowerInvariant()
    $lang = if ($ext -eq ".java") { "java" } else { "kotlin" }

    "## FILE: $relativePath`n" | Add-Content -Path $OutputPath -Encoding UTF8
     ('```' + $lang) | Add-Content -Path $OutputPath -Encoding UTF8
    Get-Content -LiteralPath $file.FullName | Add-Content -Path $OutputPath -Encoding UTF8
    '```' | Add-Content -Path $OutputPath -Encoding UTF8
    "" | Add-Content -Path $OutputPath -Encoding UTF8
}

Write-Output "Export complete: $OutputPath"
Write-Output "Files exported: $($files.Count)"

