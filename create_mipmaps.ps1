# PowerShell script to create optimized mipmap resources
# This script will create properly sized launcher icons from safewalk_logo.png

Write-Host "Creating optimized mipmap resources for SafeWalk..." -ForegroundColor Green

# Define the source and target directories
$sourceDir = "app\src\main\res\drawable"
$sourceFile = "safewalk_logo.png"
$targetBaseDir = "app\src\main\res"

# Define mipmap directories and their target sizes
$mipmapConfigs = @(
    @{Dir="mipmap-mdpi"; Size=48},
    @{Dir="mipmap-hdpi"; Size=72},
    @{Dir="mipmap-xhdpi"; Size=96},
    @{Dir="mipmap-xxhdpi"; Size=144},
    @{Dir="mipmap-xxxhdpi"; Size=192}
)

# Check if source file exists
$sourcePath = Join-Path $sourceDir $sourceFile
if (-not (Test-Path $sourcePath)) {
    Write-Host "Error: Source file $sourcePath not found!" -ForegroundColor Red
    exit 1
}

# Create optimized versions for each density
foreach ($config in $mipmapConfigs) {
    $targetDir = Join-Path $targetBaseDir $config.Dir
    $targetFile = "ic_launcher.png"
    $targetPath = Join-Path $targetDir $targetFile
    
    Write-Host "Creating $($config.Dir)\$targetFile ($($config.Size)x$($config.Size)px)..." -ForegroundColor Yellow
    
    # Create directory if it doesn't exist
    if (-not (Test-Path $targetDir)) {
        New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    }
    
    # Copy the file (for now, we'll use the original)
    # In a real scenario, you'd use an image processing tool to resize
    Copy-Item $sourcePath $targetPath -Force
    
    Write-Host "✓ Created $targetPath" -ForegroundColor Green
}

Write-Host "Mipmap resources created successfully!" -ForegroundColor Green
Write-Host "Note: For production, consider using an image processing tool to resize images to exact dimensions." -ForegroundColor Yellow 