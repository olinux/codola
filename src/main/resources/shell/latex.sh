#!/bin/bash
BASEDIR=$(dirname $0)
cd $2
PDFLATEX="$BASEDIR/$1/bin/x86_64-linux/pdflatex"
FILE="$2/$3"
LOG_FILE="$FILE.out.log"
echo "Executing PDFLatex ($PDFLATEX) for file $FILE"
NO_FILES=0

runPdfLatex(){
	"$PDFLATEX" "$FILE" >> "$LOG_FILE";
	cat "$LOG_FILE"
	NEW_NO_FILES=`cat "$LOG_FILE" | grep "No file" | wc -l`;
	if [ ${NEW_NO_FILES} != ${NO_FILES} ]; then
		NO_FILES=${NEW_NO_FILES}
		runPdfLatex
	else
		echo "Done"
	fi
}

runPdfLatex
