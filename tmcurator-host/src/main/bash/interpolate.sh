#!/bin/bash

############################################################
# Interpolates variable names in a file with given arguments 
# and returns the result
#
# Usage: ./interpolate.sh <file> {args}
#
# Author: Jochen Weile <jochenweile@gmail.com>
############################################################

#Check if file exists
if [ ! -r $1 ] 
then
	echo "Cannot read file $1!"
	echo "Usage: ./interpolate.sh <file> {args}"
	exit 1
fi

#Load file contents into variable
qry=`cat $1`
shift

#iterator over remaining arguments
i=1
while [ $# -gt 0 ]
do
	#replace current variable with its contents
	qry=`echo "$qry"|sed "s/\\\$$i/$1/g"`
	shift
	let i++
done

#return results
echo "$qry"
