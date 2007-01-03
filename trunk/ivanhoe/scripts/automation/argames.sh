#!/bin/bash

E_ARCHIVES_FAILED=255

if [ $# -ne 1 ] ; then
    cat << EOF
Usage: `basename $0` backupdir < gamelistfile
 e.g.: `basename $0` ~/gamebackups < ~/games.txt

Input should be a newline delimited list of server directories.

EOF
    exit 1
fi

BACKUP_DIR=$1

if [ ! -x `which mkarchive.sh` ] ; then
    echo "Error: Cannot find executable [mkarchive.sh].  Please make sure it's in the path and try again."
    exit 255
fi

if [ ! -d $BACKUP_DIR  ] ; then
    echo "Error: [$BACKUP_DIR] is not a directory.  Please supply a directory for backup files."
    exit 1
fi

if [ ! -w $BACKUP_DIR ] ; then
    echo "Error: [$BACKUP_DIR] is not writable so backups cannot be written."
    exit 1
fi

COMPLETED_AR=0;
INCOMPLETE_AR=0;

read GAME_DIR
while [ $GAME_DIR ] ; do
    echo " ======== PROCESSING [$GAME_DIR] ======== "
    if mkarchive.sh $GAME_DIR $BACKUP_DIR ; then
        let "COMPLETED_AR = COMPLETED_AR + 1"
    else
        let "INCOMPLETE_AR = INCOMPLETE_AR + 1"
    fi
    echo ""

    read GAME_DIR
done

if [ $INCOMPLETE_AR -ne 0 ] ; then
    let "EXIT_STATUS = $E_ARCHIVES_FAILED + $INCOMPLETE_AR"
else
    let "EXIT_STATUS = 0"
fi

echo "Completed $COMPLETED_AR game archivals with $INCOMPLETE_AR games not fully archived due to problems."
echo "Command completed at [`date`] with exit status [$EXIT_STATUS]"

exit $EXIT_STATUS
