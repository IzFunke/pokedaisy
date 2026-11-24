#!/usr/bin/env pwsh
<#
.SYNOPSIS
Runs n3ds_dump under an external wall-clock timeout and records its output.

.DESCRIPTION
This supervises the process from outside Azahar, so a synchronous RunLoop call can
be stopped by terminating the child process tree. It does not edit ROMs, saves, or
the user directory. Always pass a disposable, isolated user directory with
-IsolatedUserDir; n3ds_dump can write game saves into its user directory.

.PARAMETER Executable
Path to the n3ds_dump executable (or another executable for smoke testing).

.PARAMETER ArgumentList
Arguments passed to the executable without interpretation by this script.

.PARAMETER ArgumentListJson
JSON array form of ArgumentList for callers that launch this helper as a separate
process. Specify either ArgumentList or ArgumentListJson, not both.

.PARAMETER TimeoutSeconds
Positive hard wall-clock deadline. Timeout exits with code 124.

.PARAMETER LogFile
New log file for merged stdout and stderr. Existing files are never overwritten.

.PARAMETER IsolatedUserDir
Explicit path to the disposable user directory supplied to n3ds_dump. This is
an acknowledgement by the caller; the script does not inspect or alter it.
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [ValidateNotNullOrEmpty()]
    [string] $Executable,

    [Parameter()]
    [string[]] $ArgumentList = @(),

    [Parameter()]
    [string] $ArgumentListJson,

    [Parameter(Mandatory)]
    [ValidateRange(1, 86400)]
    [int] $TimeoutSeconds,

    [Parameter(Mandatory)]
    [ValidateNotNullOrEmpty()]
    [string] $LogFile,

    [Parameter(Mandatory)]
    [ValidateNotNullOrEmpty()]
    [string] $IsolatedUserDir
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath $Executable -PathType Leaf)) {
    throw "Executable does not exist: $Executable"
}
if (-not [string]::IsNullOrWhiteSpace($ArgumentListJson)) {
    if ($ArgumentList.Count -gt 0) { throw 'Specify only one of ArgumentList or ArgumentListJson.' }
    $parsedArguments = ConvertFrom-Json -InputObject $ArgumentListJson -NoEnumerate
    if ($parsedArguments -isnot [array] -or @($parsedArguments | Where-Object { $_ -isnot [string] }).Count -gt 0) {
        throw 'ArgumentListJson must be a JSON array of strings.'
    }
    $ArgumentList = [string[]] $parsedArguments
}
if (-not (Test-Path -LiteralPath $IsolatedUserDir -PathType Container)) {
    throw "Isolated user directory does not exist: $IsolatedUserDir"
}

$logPath = [IO.Path]::GetFullPath($LogFile)
$logParent = [IO.Path]::GetDirectoryName($logPath)
if (-not (Test-Path -LiteralPath $logParent -PathType Container)) {
    throw "Log directory does not exist: $logParent"
}

if (-not ('PokeDaisy.BoundedHeadless.OutputLogger' -as [type])) {
    Add-Type -TypeDefinition @'
using System;
using System.Diagnostics;
using System.IO;

namespace PokeDaisy.BoundedHeadless {
    public sealed class OutputLogger {
        private readonly StreamWriter writer;
        private readonly object gate = new object();
        public OutputLogger(StreamWriter writer) { this.writer = writer; }
        public DataReceivedEventHandler OutputHandler { get { return HandleOutput; } }
        public DataReceivedEventHandler ErrorHandler { get { return HandleError; } }
        public void Append(string line) { lock (gate) writer.WriteLine(line); }
        private void HandleOutput(object sender, DataReceivedEventArgs eventArgs) {
            if (eventArgs.Data == null) return;
            Append("[stdout] " + eventArgs.Data);
        }
        private void HandleError(object sender, DataReceivedEventArgs eventArgs) {
            if (eventArgs.Data == null) return;
            Append("[stderr] " + eventArgs.Data);
        }
    }
}
'@ -ErrorAction Stop
}

# CreateNew prevents accidental truncation of an existing diagnostic log.
$logStream = [IO.File]::Open($logPath, [IO.FileMode]::CreateNew, [IO.FileAccess]::Write, [IO.FileShare]::Read)
$writer = [IO.StreamWriter]::new($logStream, [Text.UTF8Encoding]::new($false))
$writer.AutoFlush = $true
$logger = $null
$process = $null
$timedOut = $false

try {
    $startInfo = [Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = [IO.Path]::GetFullPath($Executable)
    $startInfo.WorkingDirectory = (Get-Location).Path
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    foreach ($argument in $ArgumentList) {
        [void] $startInfo.ArgumentList.Add($argument)
    }

    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    $logger = [PokeDaisy.BoundedHeadless.OutputLogger]::new($writer)
    $process.add_OutputDataReceived($logger.OutputHandler)
    $process.add_ErrorDataReceived($logger.ErrorHandler)

    if (-not $process.Start()) { throw 'The child process did not start.' }
    $process.BeginOutputReadLine()
    $process.BeginErrorReadLine()

    if (-not $process.WaitForExit($TimeoutSeconds * 1000)) {
        $timedOut = $true
        $logger.Append("[supervisor] timed out after $TimeoutSeconds seconds; terminating process tree")

        # Use Windows' process-tree terminator; n3ds_dump may have helper descendants.
        $null = & "$env:SystemRoot\System32\taskkill.exe" /PID $process.Id /T /F 2>$null
        $process.WaitForExit()
    } else {
        # Flush both asynchronous stream readers before closing the log.
        $process.WaitForExit()
    }
} catch {
    if ($null -ne $process -and -not $process.HasExited) {
        try {
            $logger.Append('[supervisor] interrupted by supervisor error; terminating process tree')
            $null = & "$env:SystemRoot\System32\taskkill.exe" /PID $process.Id /T /F 2>$null
            $process.WaitForExit()
        } catch { }
    }
    throw
} finally {
    if ($null -ne $process) { $process.Dispose() }
    $writer.Dispose()
}

if ($timedOut) { exit 124 }
exit $process.ExitCode
