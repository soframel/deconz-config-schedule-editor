export SCHEDULE_EDITOR_LOG_LEVEL=FINEST
cd /home/openhabian/deconz-config-schedule-editor/
deconz-config-schedule-editor-0.0.1-SNAPSHOT-runner > /var/lib/openhab/schedule-editor.log 2>&1 &
echo $! > /var/lib/openhab/schedule-editor.pid