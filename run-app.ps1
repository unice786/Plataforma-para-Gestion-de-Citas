# Lanzador de la Plataforma de Citas (Spring Boot)
# Ejecuta el jar en segundo plano, desacoplado de la terminal, limitando RAM a 768 MB.
# Uso:  .\run-app.ps1          (ejecuta)

$ErrorActionPreference = "Stop"

$ProjectDir = "C:\Users\unice\proyectos\plataforma-citas"
$JavaHome   = "C:\Users\unice\.jdks\ms-21.0.12.1"
$Jar        = Join-Path $ProjectDir "target\plataforma-citas-0.0.1-SNAPSHOT.jar"
$LogDir     = Join-Path $ProjectDir "target\logs"
$LogStd     = Join-Path $LogDir "app.log"
$LogErr     = Join-Path $LogDir "app.err.log"
$PidFile    = Join-Path $LogDir "app.pid"

if (-not (Test-Path $Jar)) {
    Write-Error "No existe el jar. Ejecuta primero: .\mvnw.cmd -DskipTests package"
}

New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

# Si ya hay una instancia corriendo en el puerto 8080, la detenemos primero.
$listening = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
foreach ($conn in $listening) {
    $proc = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
    if ($proc -and $proc.ProcessName -match "java") {
        Write-Output "Deteniendo instancia anterior (PID $($proc.Id))..."
        Stop-Process -Id $proc.Id -Force
    }
}
Start-Sleep -Seconds 2

$javaExe = Join-Path $JavaHome "bin\javaw.exe"

# Argumentos JVM: limite de heap 768 MB + mem non-heap razonable.
$args = @(
    "-Xmx768m", "-XX:MaxMetaspaceSize=256m",
    "-jar", $Jar
)

$proc = Start-Process -FilePath $javaExe `
    -ArgumentList $args `
    -WorkingDirectory $ProjectDir `
    -RedirectStandardOutput $LogStd `
    -RedirectStandardError $LogErr `
    -WindowStyle Hidden `
    -PassThru

$proc.Id | Set-Content -Path $PidFile
Write-Output "App lanzada. PID: $($proc.Id)"
Write-Output "Heap max: 768 MB (-Xmx768m)"
Write-Output "Log: $LogStd"
Write-Output "URL: http://localhost:8080"
