#!/usr/bin/Rscript

args <- commandArgs(TRUE)

qry <-
"select type, count(type) from (
	select g1type as type 
	from verdicts 
	where negative!=2
	union all 
	select g2type as type 
	from verdicts 
	where negative!=2
) group by type;"

cmd <- paste("echo \"",qry,"\"|sqlite3 ",args[1],sep="")

data <- read.table(file=pipe(cmd),sep="|",row.names=1)

ramp <- colorRampPalette(c("white","steelblue"))
percentage <- round(100 * data[,1] / sum(data), digits=2);
lab <- paste(tolower(rownames(data)),paste(percentage,"%",sep=""))

pdf("entity_types.pdf")
pie(data[,1], labels=lab, col=ramp(4))
dev.off()