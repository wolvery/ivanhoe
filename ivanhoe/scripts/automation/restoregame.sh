#!/bin/bash

get_property() {
    if [ $# -ne 2 ] ; then
        echo "Error: function [$0] needs two arguments"
        exit 255;
    fi

    prop_name=$1
    prop_file=$2

    PROPERTY=`sed 's/#.*//g' $prop_file | grep "$prop_name" | gawk --field-separator = -- '{print $2;}'`
    if [ "$PROPERTY" ] ; then
        return 0
    else
        echo "Error: could not get property [$prop_name] from [$prop_file]"
        exit 4
    fi
}

subcommand_fail() {
    echo "Error: subcommand failed."
    exit 255
}

ZCAT="gunzip -c"
GTAR=`which gtar`

if [ -z "$GTAR" ] ; then
    GTAR=`which tar`
fi

if [ $# -ne 3 ] ; then
    cat << EOF
Usage: `basename $0` server_directory backup_dir backup_number
 e.g.: `basename $0` ./ivanhoe/dist/server ./backups 03
EOF
    exit 1;
fi

SERVER_DIR=$1
BACKUP_DIR=$2
BACKUP_NUMBER=$3

if [ -z `echo $BACKUP_DIR | egrep '^/'` ] ; then
    BACKUP_DIR="`pwd`/$BACKUP_DIR"
fi

if [ ! -e $SERVER_DIR/ivanhoe.properties ] ; then
    echo "Error: [$SERVER_DIR] not an ivanhoe server directory.  Cannot find [$SERVER_DIR/ivanhoe.properties]"
    exit 2
fi

if [ ! -d $BACKUP_DIR ] ; then
    echo "Error: [$BACKUP_DIR] cannot be found or is not a directory"
    exit 2
fi

cd $SERVER_DIR

get_property "gameServerName" "ivanhoe.properties"
SERVER_NAME=$PROPERTY
get_property "dbHost" "ivanhoe.properties"
DB_HOST=$PROPERTY
get_property "dbUser" "ivanhoe.properties"
DB_USER=$PROPERTY
get_property "dbPass" "ivanhoe.properties"
DB_PASS=$PROPERTY
get_property "discourseFieldRoot" "ivanhoe.properties"
DF_DIR=$PROPERTY

SQL_AR="$BACKUP_DIR/$SERVER_NAME.$BACKUP_NUMBER.sql.gz"
DF_AR="$BACKUP_DIR/$SERVER_NAME.$BACKUP_NUMBER.discourse_field.tar.gz"

if [ ! -e "$SQL_AR" ] ; then
    echo "Error: could not find [$SQL_AR]"
    exit 3
fi

if [ ! -e "$DF_AR" ] ; then
    echo "Error: could not find [$DF_AR]"
    exit 3
fi

rm -rf $DF_DIR

$ZCAT $DF_AR | $GTAR x || subcommand_fail
$ZCAT $SQL_AR | mysql -h$DB_HOST -u$DB_USER -p$DB_PASS || subcommand_fail

echo "Restore successful."
exit 0
