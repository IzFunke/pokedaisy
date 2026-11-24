[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$supervisor = Join-Path $PSScriptRoot 'run_n3ds_dump_bounded.ps1'
$pwsh = (Get-Process -Id $PID).Path
$tempRoot = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd([IO.Path]::DirectorySeparatorChar, [IO.Path]::AltDirectorySeparatorChar)
$scratch = Join-Path $tempRoot ("n3ds-supervisor-smoke-" + [guid]::NewGuid().ToString('N'))
$scratch = [IO.Path]::GetFullPath($scratch)
$tempPrefix = $tempRoot + [IO.Path]::DirectorySeparatorChar
if (-not $scratch.StartsWith($tempPrefix, [StringComparison]::OrdinalIgnoreCase)) {
    throw "Refusing to use a smoke-test path outside the temp directory: $scratch"
}
$userDir = Join-Path $scratch 'isolated-userdir'
$null = New-Item -ItemType Directory -Path $userDir -Force
$grandchildPidFile = Join-Path $scratch 'grandchild.pid'
$childScript = Join-Path $scratch 'fake-child.ps1'
$sleepScript = Join-Path $scratch 'fake-grandchild.ps1'
$normalLog = Join-Path $scratch 'normal.log'
$timeoutLog = Join-Path $scratch 'timeout.log'
$spawnedPid = $null

@'
Start-Sleep -Seconds 120
'@ | Set-Content -LiteralPath $sleepScript -Encoding utf8

@'
param([string] $Mode, [string] $SleepScript, [string] $PidFile, [string] $PwshPath)
[Console]::Out.WriteLine('fake child stdout marker')
[Console]::Error.WriteLine('fake child stderr marker')
if ($Mode -eq 'timeout') {
    $grandchild = Start-Process -FilePath $PwshPath -ArgumentList @('-NoProfile', '-File', $SleepScript) -PassThru
    Set-Content -LiteralPath $PidFile -Value $grandchild.Id -NoNewline
    Start-Sleep -Seconds 120
}
'@ | Set-Content -LiteralPath $childScript -Encoding utf8

function Invoke-SupervisorSmoke([string] $Mode, [int] $TimeoutSeconds, [string] $LogFile) {
    $startInfo = [Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $pwsh
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.RedirectStandardOutput = $true
    foreach ($argument in @(
        '-NoProfile', '-File', $supervisor,
        '-Executable', $pwsh,
        '-ArgumentListJson', (ConvertTo-Json -Compress -InputObject @('-NoProfile', '-File', $childScript, $Mode, $sleepScript, $grandchildPidFile, $pwsh)),
        '-TimeoutSeconds', "$TimeoutSeconds",
        '-LogFile', $LogFile,
        '-IsolatedUserDir', $userDir
    )) { [void] $startInfo.ArgumentList.Add($argument) }

    $runner = [Diagnostics.Process]::new()
    $runner.StartInfo = $startInfo
    if (-not $runner.Start()) { throw 'Could not start the supervisor smoke process.' }
    $stdoutTask = $runner.StandardOutput.ReadToEndAsync()
    $stderrTask = $runner.StandardError.ReadToEndAsync()
    if (-not $runner.WaitForExit(15000)) {
        $runner.Kill($true)
        throw "Supervisor smoke process exceeded 15 seconds ($Mode)."
    }
    $runner.WaitForExit()
    $output = $stdoutTask.Result + $stderrTask.Result
    $code = $runner.ExitCode
    $runner.Dispose()
    return @{ ExitCode = $code; Output = $output }
}

try {
    $normal = Invoke-SupervisorSmoke 'normal' 10 $normalLog
    if ($normal.ExitCode -ne 0) { throw "Normal child exited $($normal.ExitCode): $($normal.Output)" }
    $normalText = Get-Content -Raw -LiteralPath $normalLog
    if ($normalText -notmatch '\[stdout\] fake child stdout marker' -or $normalText -notmatch '\[stderr\] fake child stderr marker') {
        throw 'Normal run log did not contain both stdout and stderr markers.'
    }

    $timed = Invoke-SupervisorSmoke 'timeout' 2 $timeoutLog
    if ($timed.ExitCode -ne 124) { throw "Timed child returned $($timed.ExitCode), expected 124: $($timed.Output)" }
    $timeoutText = Get-Content -Raw -LiteralPath $timeoutLog
    if ($timeoutText -notmatch '\[stdout\] fake child stdout marker' -or $timeoutText -notmatch '\[stderr\] fake child stderr marker' -or
        $timeoutText -notmatch 'timed out after 2 seconds') {
        throw 'Timeout log did not preserve both output streams and the timeout marker.'
    }

    $spawnedPid = [int](Get-Content -Raw -LiteralPath $grandchildPidFile)
    $stopWatch = [Diagnostics.Stopwatch]::StartNew()
    while ($null -ne (Get-Process -Id $spawnedPid -ErrorAction SilentlyContinue) -and $stopWatch.Elapsed.TotalSeconds -lt 5) {
        Start-Sleep -Milliseconds 100
    }
    if ($null -ne (Get-Process -Id $spawnedPid -ErrorAction SilentlyContinue)) {
        throw "Grandchild process $spawnedPid survived the timeout kill."
    }

    'PASS: normal exit code and tagged stdout/stderr log'
    'PASS: wall-clock timeout returns 124 and logs timeout'
    'PASS: timeout terminates the child process tree'
} finally {
    if ($null -ne $spawnedPid) {
        Stop-Process -Id $spawnedPid -Force -ErrorAction SilentlyContinue
    }
    if ($scratch.StartsWith($tempPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        Remove-Item -LiteralPath $scratch -Recurse -Force -ErrorAction SilentlyContinue
    }
}
