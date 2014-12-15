#!/bin/sh

echo "====start move apks"
cp out/production/myapp/myapp.apk workspace/plugin/assets/plugintest/
echo "move myapp.apk"
cp out/production/myapp/myapp.unaligned.apk workspace/plugin/assets/plugintest/
echo "move myapp.unaligned.apk"
echo "====end move apks" 
