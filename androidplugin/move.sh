#!/bin/sh

echo "====start move apks"

cp out/production/myapp/myapp.apk workspace/plugin/assets/plugintest/
echo "move myapp.apk"

cp out/production/myapp/myapp.unaligned.apk workspace/plugin/assets/plugintest/
echo "move myapp.unaligned.apk"

cp out/production/FragmentDeveloper/FragmentDeveloper.apk workspace/plugin/assets/plugintest/
echo "move FragemntDeveloper.apk"
echo "====end move apks" 
