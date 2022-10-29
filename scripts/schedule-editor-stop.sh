cd /home/openhabian/deconz-config-schedule-editor/
PID=$(cat /var/lib/openhab/schedule-editor.pid)
kill $PID
rm /var/lib/openhab/schedule-editor.pid