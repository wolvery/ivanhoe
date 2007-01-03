#!/bin/bash
#
#   mkarchive.sh
#       A tool to automate backups of Ivanhoe data
#       by Ben Cummings
#

get_property() {
    if [ $# -ne 2 ] ; then
        echo "Error: function [$0] needs two arguments"
        exit 255;
    fi

    prop_name=$1
    prop_file=$2

    PROPERTY=`sed 's/#.*//g' $prop_file | grep "$prop_name" | gawk --field-separator = -- '{print $2;}'`
    if [ $PROPERTY ] ; then
        return 0
    else
        echo "Error: could not get property [$prop_name] from [$prop_file]"
        exit 4
    fi
}

#
#   SETUP AND ARGUMENT PROCESSING
#

if [ $# -ne 2 ] ; then
    cat << EOF
Usage: `basename $0` server_directory backup_directory
 e.g.: `basename $0` ./ivanhoe/dist/server ./backup
EOF
    exit 0
fi

IVANHOE_SERVER=$1
IVANHOE_BACKUP=$2

if [ -z `echo $IVANHOE_SERVER | egrep '^/'` ] ; then
    IVANHOE_SERVER="`pwd`/$IVANHOE_SERVER"
fi

if [ -z `echo $IVANHOE_BACKUP | egrep '^/'` ] ; then
    IVANHOE_BACKUP="`pwd`/$IVANHOE_BACKUP"
fi

if [ ! -d $IVANHOE_SERVER ] ; then
    echo "Error: [$IVANHOE_SERVER] must be a directory"
    exit 1
fi

if [ ! -d $IVANHOE_BACKUP ] ; then
    echo "Error: [$IVANHOE_BACKUP] must be a directory"
    exit 1
fi

DATE_STRING=`date +%d`

#
#   DATA PROCESSING
#

cd $IVANHOE_SERVER
if [ -e ./ivanhoe.properties ] ; then
    get_property "gameServerName" "./ivanhoe.properties"
    GAME_SERVER_NAME=$PROPERTY

#
#   PART ONE:
#       SQL dump
#

    get_property "dbName" "./ivanhoe.properties" 
    DB_NAME=$PROPERTY
    get_property "dbHost" "./ivanhoe.properties" 
    DB_HOST=$PROPERTY
    get_property "dbUser" "./ivanhoe.properties" 
    DB_USER=$PROPERTY
    get_property "dbPass" "./ivanhoe.properties" 
    DB_PASS=$PROPERTY

    if [ -z $DB_PASS ] ; then
        echo "Error: could not find required database fields in the properties file"
        exit 4
    fi

    DB_BACKUP_FILE="$IVANHOE_BACKUP/$GAME_SERVER_NAME.$DATE_STRING.sql"

    # This is insecure!  FIXME: use 'expect' instead!
    mysqldump -h$DB_HOST -u$DB_USER -p$DB_PASS --opt --databases $DB_NAME > $DB_BACKUP_FILE && \
    gzip -f $DB_BACKUP_FILE
    DB_BACKUP_FILE="$DB_BACKUP_FILE.gz"

    if [ ! -f $DB_BACKUP_FILE ] ; then
        echo "Error: could not dump database."
        exit 2
    fi

#
#   PART TWO:
#       Discourse field dump
#

    DF_BACKUP_FILE="$IVANHOE_BACKUP/$GAME_SERVER_NAME.$DATE_STRING.discourse_field.tar"
    get_property "discourseFieldRoot" "./ivanhoe.properties"
    DF_ROOT=$PROPERTY

    tar cf $DF_BACKUP_FILE $DF_ROOT # *game[0-9]*-journal.html
    gzip -f $DF_BACKUP_FILE
    rm -f $DF_BACKUP_FILE
    DF_BACKUP_FILE="$DF_BACKUP_FILE.gz"

else
    echo "Error: Could not find the properties file."
    exit 2
fi

#
#   REPORT
#

cat << EOF
Backup completed succesfully.  Generated files:
    SQL database dump: [$DB_BACKUP_FILE]
    Discourse field archive: [$DF_BACKUP_FILE]

EOF
exit 0
