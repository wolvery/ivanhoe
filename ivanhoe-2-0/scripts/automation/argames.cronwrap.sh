#!/bin/bash

E_BADARGS=1

if [ $# -lt 3 ] ; then
    echo "Usage: `basename $0` backupdir logstem emailaddress < serverdir_list"
    echo " e.g.: `basename $0` /var/ivanhoe/archive/ myscript root@localhost < ivanhoegames.txt"
    exit $E_BADARGS
fi

BACKUP_DIR=$1
LOGFILE="$BACKUP_DIR/$2.`date +%d`.log"
EMAIL_ADDRESS=$3

if [ -z `echo $LOGFILE | egrep '^/'` ] ; then
    LOGFILE="`pwd`/$LOGFILE"
fi

argames.sh "$BACKUP_DIR" > "$LOGFILE" 2>&1 || \
    mail -s "Ivanhoe archival ran into problems" $EMAIL_ADDRESS < "$LOGFILE"
