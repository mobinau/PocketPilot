param([Parameter(Mandatory=$true)][string]$Name)
$adbPath = 'D:\Android-SDK\platform-tools\adb.exe'
$outputDirectory = 'C:\Users\ASUS\Desktop\PocketPilot\verification\phase3'
& $adbPath -s emulator-5554 shell uiautomator dump /sdcard/pocketpilot-ui.xml
& $adbPath -s emulator-5554 pull /sdcard/pocketpilot-ui.xml (Join-Path $outputDirectory "$Name.xml")
& $adbPath -s emulator-5554 shell screencap -p /sdcard/pocketpilot-screen.png
& $adbPath -s emulator-5554 pull /sdcard/pocketpilot-screen.png (Join-Path $outputDirectory "$Name.png")
[xml]$hierarchy = Get-Content -LiteralPath (Join-Path $outputDirectory "$Name.xml")
$hierarchy.SelectNodes('//node') | Where-Object { ($_.text -or $_.'content-desc' -or $_.class -eq 'android.widget.EditText') -and $_.bounds -ne '[0,0][0,0]' } | ForEach-Object { Write-Output ($_.class + ' | ' + $_.text + ' | ' + $_.'content-desc' + ' | ' + $_.bounds) }
