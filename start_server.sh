#!/bin/bash

# Computation Graph Server Startup Script

echo "🚀 Starting Computation Graph Server..."
echo "======================================="

# Change to the project directory
cd "$(dirname "$0")/project_biu"

# Compile the Java files
echo "📦 Compiling Java files..."
find . -name "*.java" -print0 | xargs -0 javac -cp .

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    echo ""
    echo "🌐 Starting HTTP Server on port 8080..."
    echo "📱 Web Interface: http://localhost:8080/app/index.html"
    echo ""
    echo "Press Ctrl+C to stop the server"
    echo "======================================="
    
    # Run the server
    java -cp . Main
else
    echo "❌ Compilation failed! Please check for errors above."
    exit 1
fi
