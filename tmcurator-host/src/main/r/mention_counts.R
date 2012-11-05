#!/usr/bin/Rscript

# sqlite3 tmcurator.db "select actionType, count(*) from mentions group by actionType;">mention_counts.csv

mc <- read.table("mention_counts.csv", sep="|")
counts <- mc[,2]
names(counts) <- mc[,1]
counts <- counts[order(counts,decreasing=TRUE)]

pdf("mention_counts.pdf")
op <- par(las=3, omi=c(.5,0,0,0))
barplot(counts, main="Verb frequencies", ylab="Frequency (log-scale)", log="y", cex.names=.5)
par(op)
dev.off()
