#!/bin/sh

if [ $# -eq 0 ] || [ "$1" = "-h" ]
then
	echo "usage: $0 cmd1 cmd2 ... TASK_ID"
	echo "runs a group of commands as a background process and saves the process' pid in 'pid_\$TASK_ID'"
	echo "the script's exit value is the same as the background process'"
	exit 0
fi

POS=0
LAST="$(expr $# - 1)"
for var in "$@"
do
	if [ $POS -lt $LAST ]
	then
		ARGS[$POS]="$var"
	fi
	ALL_ARGS[$POS]="$var"
	POS=`expr $POS + 1`
done


TASK_ID="${ALL_ARGS[$LAST]}"




#################################################################################



cmd="${ARGS[@]}"

#cmd="${cmd//\"}"


temp1="${cmd%\'}"
temp2="${temp1#\'}"


#temp1="${cmd%\"}"
#temp2="${temp1#\"}"


echo "$temp2" > comando.txt

eval "$temp2" &

my_pid=$!

echo "$my_pid" > "pid_$TASK_ID"

wait $my_pid


my_status=$?

exit $my_status
