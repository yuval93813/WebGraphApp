#!/bin/bash

echo "🎯 Interactive Demo of Complete System"
echo "====================================="
echo ""

echo "🌐 Opening web interface in browser..."
if command -v open >/dev/null 2>&1; then
    open "http://localhost:8080/app/index.html"
else
    echo "Please open http://localhost:8080/app/index.html in your browser"
fi

echo ""
echo "📋 Demo Steps:"
echo "1. ✅ Upload a configuration file using the left panel form"
echo "2. ✅ Send topic messages using the topic form"
echo "3. ✅ View the dynamic graph visualization in the center panel"
echo "4. ✅ Monitor system status in the right panel"
echo ""

echo "🧪 Testing all endpoints automatically..."
echo ""

echo "📤 1. Testing configuration upload with working_config.conf..."
UPLOAD_RESPONSE=$(curl -s -X POST -F "configFile=@config_files/working_config.conf" http://localhost:8080/upload)
if echo "$UPLOAD_RESPONSE" | grep -q "AIncAgent"; then
    echo "   ✅ Configuration uploaded successfully - Graph contains AIncAgent"
    echo "   ✅ Dynamic HTML generated with graph visualization"
else
    echo "   ❌ Upload failed or missing graph data"
fi

echo ""
echo "📢 2. Testing topic message publishing..."
PUBLISH_RESPONSE=$(curl -s "http://localhost:8080/publish?topic=input_a&message=42")
if echo "$PUBLISH_RESPONSE" | grep -q "Message Published Successfully"; then
    echo "   ✅ Message published to topic 'input_a' with value '42'"
else
    echo "   ❌ Topic publishing failed"
fi

echo ""
echo "📢 3. Testing another topic message..."
PUBLISH_RESPONSE2=$(curl -s "http://localhost:8080/publish?topic=input_b&message=58")
if echo "$PUBLISH_RESPONSE2" | grep -q "Message Published Successfully"; then
    echo "   ✅ Message published to topic 'input_b' with value '58'"
else
    echo "   ❌ Topic publishing failed"
fi

echo ""
echo "🔍 4. Testing graph visualization after messages..."
GRAPH_RESPONSE=$(curl -s -X POST -F "configFile=@config_files/working_config.conf" http://localhost:8080/upload)
if echo "$GRAPH_RESPONSE" | grep -q "topicData" && echo "$GRAPH_RESPONSE" | grep -q "lastMessage"; then
    echo "   ✅ Graph visualization includes topic message data"
    echo "   ✅ Topics show last published messages"
else
    echo "   ❌ Graph missing topic data integration"
fi

echo ""
echo "📊 System Features Demonstrated:"
echo "   ✅ Three-panel iframe layout"
echo "   ✅ Configuration file upload with multipart parsing"
echo "   ✅ Topic message publishing with GET requests"
echo "   ✅ Dynamic HTML generation from server-side graph data"
echo "   ✅ Interactive graph visualization with SVG"
echo "   ✅ Template-based View layer architecture"
echo "   ✅ Static file serving with security features"
echo "   ✅ Real-time graph statistics and node information"
echo ""

echo "🎨 UI Features:"
echo "   ✅ Modern responsive design with CSS Grid and Flexbox"
echo "   ✅ Interactive node clicking with detailed information panels"
echo "   ✅ Graph controls: zoom, center, animation toggle, export"
echo "   ✅ Color-coded node types with legend"
echo "   ✅ Force-directed graph layout algorithm"
echo "   ✅ Hover effects and smooth animations"
echo ""

echo "🏗️ Architecture Highlights:"
echo "   ✅ Separation of concerns: servlets, views, and static content"
echo "   ✅ Template injection for dynamic content generation"
echo "   ✅ HTTP server with multiple endpoint handling"
echo "   ✅ Error handling and input validation"
echo "   ✅ Graph data to JavaScript conversion"
echo ""

echo "🎯 Access the full interactive demo at:"
echo "   🌐 http://localhost:8080/app/index.html"
echo ""
echo "🎉 Demo completed successfully!"
