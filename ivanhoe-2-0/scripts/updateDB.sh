#!/bin/sh

jar_file=$1
db_username=$2
db_password=$3
update_dir="./updated_files"

if [ "$NOT_LIVE_DB" != "true" ]
then
    cat << EOF

WARNING:

This update utility will OVERWRITE any databases it finds that match databases
created by the scripts.  In effect, this will destroy the current working
copies of the databases whose backups you're working with.

In other words, don't run this on the live server.

If you're sure you want to run this, set the environment variable
NOT_LIVE_DB="true".

EOF
    exit 1
fi

if [ -z "$IVANHOE_BACKUP_LIST" ]
then
    cat << EOF

You must supply a list of files to be updated in the IVANHOE_BACKUP_LIST
environment variable.

EOF
    exit 1;
else
    filelist=$IVANHOE_BACKUP_LIST
fi

if [ -d "$update_dir" ]
then
    echo "" > /dev/null
else
    if [ -e "$update_dir" ]
    then
        echo "Cannot overwrite file [$update_dir] to create update directory"
        exit 1
    else
        mkdir "$update_dir"
    fi
fi

if [ "$jar_file" -a -f "$jar_file" ]
then
    echo "" > /dev/null
else
    echo "Unable to find file '$jar_file'.  First argument must be jar containing data converter."
    exit 1
fi

if [ -z $db_username ]
then
    echo "Second argument must be SQL username"
    exit 1
fi

if [ -z $db_password ]
then
    echo "Third argument must be SQL password"
    exit 1
fi

for sqlfile in $filelist
do
    cat << EOF
 ========  PROCESSING FILE  ======== 
jar_file = $jar_file
db_username = $db_username
db_password = $db_password
sqlfile = $sqlfile
EOF

    echo ""

    echo "database_name=\`grep \"CREATE DATABASE\" $sqlfile | awk -- '{print \$3;}' | sed -e 's/;//' | tr -d \"\n\r\"\`"
           database_name=`grep  "CREATE DATABASE"  $sqlfile | awk -- '{print  $3;}' | sed -e 's/;//' | tr -d  "\n\r"  `

    if [ -z "$database_name" ]
    then
        echo "Could not find name of database in [$sqlfile].  Not updating."
    else
        echo "mysql -u$db_username -p$db_password < $sqlfile"
              mysql -u$db_username -p$db_password < $sqlfile

        echo "java -jar $jar_file > /dev/null"
              java -jar $jar_file > /dev/null

        echo "mysqldump -u$db_username -p$db_password --opt --databases $database_name > $update_dir/$sqlfile"
              mysqldump -u$db_username -p$db_password --opt --databases $database_name > $update_dir/$sqlfile
    fi

    echo ""
    echo ""
done
exit 0
