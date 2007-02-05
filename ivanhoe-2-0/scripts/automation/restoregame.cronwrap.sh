#!/bin/bash

E_BADARGS=1

ADMIN_COMMAND="java -jar admin.jar"
LOBBY_COMMAND="java -jar ivanhoe-server.jar"
SERVER_COMMAND="java -jar lobby-server.jar"

if [ $# -ne 5 ] ; then
    echo "Usage: `basename $0` server_directory backupdir backupstem logstem emailaddress"
    echo " e.g.: `basename $0` /usr/local/ivanhoe_hosts/ivanhoe/dist/server /var/ivanhoe/archive/ 00 ivanrestore root@localhost"
    exit $E_BADARGS
fi

SERVER_DIR=$1; shift
BACKUP_DIR=$1; shift
BACKUP_STEM=$1; shift
LOG_STEM=$1; shift
EMAIL_ADDRESS=$1; shift

LOG_FILE="$BACKUP_DIR/$LOG_STEM.`date +%d`.log"

if [ -z `echo $LOG_FILE | egrep '^/'` ] ; then
    LOG_FILE="`pwd`/$LOG_FILE"
fi

echo "" > $LOG_FILE

pushd $SERVER_DIR > /dev/null

$ADMIN_COMMAND -servertype l 2> "$LOG_FILE" > /dev/null <<EOF
sleep 1000
shutdown
EOF
$ADMIN_COMMAND -servertype g 2> "$LOG_FILE" > /dev/null <<EOF
sleep 1000
say This server will automatically restart in a moment.
sleep 30000
kickall
sleep 30000
shutdown
quit
EOF

popd > /dev/null

restoregame.sh "$SERVER_DIR" "$BACKUP_DIR" "$BACKUP_STEM" >> "$LOG_FILE" 2>&1 || \
    mail -s "Ivanhoe restore ran into problems" $EMAIL_ADDRESS < "$LOG_FILE"

pushd $SERVER_DIR > /dev/null

nohup $SERVER_COMMAND >> "$LOG_FILE" 2>&1 &
nohup $LOBBY_COMMAND >> "$LOG_FILE" 2>&1 &

popd > /dev/null
