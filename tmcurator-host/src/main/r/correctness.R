#!/usr/bin/Rscript

library(plotrix)

args <- commandArgs(TRUE)

qry <-
"SELECT 
	new, 
	AVG(match) AS correctness,
	COUNT(*) AS total
FROM (
	SELECT 
		mentions.actionType AS old, 
		verdicts.action AS new, 
		mentions.actionType=verdicts.action AS match
	FROM mentions, verdicts
	WHERE mentions.ROWID=verdicts.mentionId
)
GROUP BY new
ORDER BY total DESC, correctness DESC;"

cmd <- paste("echo \"",qry,"\"|sqlite3 ",args[1],sep="")

data <- read.table(file=pipe(cmd),sep="|",row.names=1)
colnames(data) <- c("correctness","total")

pdf("correctness.pdf")
op <- par(las=3, omi=c(.5,0,0,0))

palette.function <- colorRampPalette(c("black","steelblue"))
col.codes <- palette.function(100)[floor(data$correctness * 99)+1]
# col.codes <- findInterval(data$correctness, palette.function(100))
barplot(
	data$total,
	ylab="Frequency",
	names.arg=rownames(data), 
	cex.names=.5, 
	col=col.codes, 
	border=NA
)

color.legend(50,135,75,140,
	c("0%","TM accuracy","100%"),
	palette.function(100),
	cex=.7
)

par(op)
dev.off()
