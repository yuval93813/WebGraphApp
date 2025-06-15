#!/bin/bash

# Comprehensive Test Script for Computation Graph System
echo "🧪 Testing Computation Graph System"
echo "==================================="

# Check if server is running
echo "🔍 Checking if server is running on port 8080..."
if ! curl -s http://localhost:8080/app/index.html > /dev/null; then
    echo "❌ Server not running. Please start with: cd project_biu && java -cp . Main"
    exit 1
fi

echo "✅ Server is running!"
echo ""

# Test static file serving
echo "📄 Testing static file serving..."
if curl -s "http://localhost:8080/app/index.html" | grep -q "Interactive Web UI"; then
    echo "✅ index.html served successfully"
else
    echo "❌ Failed to serve index.html"
fi

if curl -s "http://localhost:8080/app/form.html" | grep -q "Control Forms"; then
    echo "✅ form.html served successfully"
else
    echo "❌ Failed to serve form.html"
fi

if curl -s "http://localhost:8080/app/graph.html" | grep -q "Computation Graph"; then
    echo "✅ graph.html served successfully"
else
    echo "❌ Failed to serve graph.html"
fi

echo ""

# Test topic publishing
echo "📤 Testing topic messaging..."
if curl -s "http://localhost:8080/publish?topic=test&message=hello" | grep -q "Message Published Successfully"; then
    echo "✅ Topic publishing works"
else
    echo "❌ Topic publishing failed"
fi

echo ""

# Test file upload endpoint (without actual file)
echo "📁 Testing upload endpoint accessibility..."
response=$(curl -s -w "%{http_code}" "http://localhost:8080/upload" -o /dev/null)
if [[ "$response" == "400" ]]; then
    echo "✅ Upload endpoint accessible (returns 400 for GET request as expected)"
else
    echo "❌ Upload endpoint issue (got $response)"
fi

echo ""
echo "🎉 All tests completed!"
echo ""
echo "🌐 Open your browser to: http://localhost:8080/app/index.html"
echo "📱 The system is ready for use!"
