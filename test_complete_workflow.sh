#!/bin/bash

echo "🧪 Complete System Workflow Test"
echo "=================================="

# Test 1: Check if server is running
echo "📡 Testing server connectivity..."
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/app/index.html | grep -q "200"; then
    echo "✅ Server is running and responsive"
else
    echo "❌ Server is not responding"
    exit 1
fi

# Test 2: Test static file serving
echo "📄 Testing static file serving..."
if curl -s http://localhost:8080/app/form.html | grep -q "Upload Configuration"; then
    echo "✅ Static files are served correctly"
else
    echo "❌ Static file serving failed"
    exit 1
fi

# Test 3: Test configuration upload endpoint
echo "📤 Testing configuration upload..."
if curl -s -X POST \
    -F "configFile=@config_files/working_config.conf" \
    http://localhost:8080/upload | grep -q "Computation Graph Visualization"; then
    echo "✅ Configuration upload works"
else
    echo "❌ Configuration upload failed"
fi

# Test 4: Test topic publishing endpoint
echo "📢 Testing topic publishing..."
if curl -s "http://localhost:8080/publish?topic=test&message=hello" | grep -q "Message Published Successfully"; then
    echo "✅ Topic publishing works"
else
    echo "❌ Topic publishing failed"
fi

# Test 5: Test graph visualization template
echo "🎨 Testing graph visualization..."
if curl -s http://localhost:8080/app/graph.html | grep -q "Graph Visualization"; then
    echo "✅ Graph visualization template loads"
else
    echo "❌ Graph visualization failed"
fi

echo ""
echo "🎉 Workflow test completed!"
echo "🌐 Access the full interface at: http://localhost:8080/app/index.html"
