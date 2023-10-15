export SCHEDULE_EDITOR_LOG_LEVEL=FINEST
cd /home/openhabian/deconz-config-schedule-editor/
java -jar quarkus-run.jar > /var/lib/openhab/schedule-editor.log 2>&1 &
echo $! > /var/lib/openhab/schedule-editor.pid