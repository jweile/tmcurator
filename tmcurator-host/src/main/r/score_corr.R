#!/usr/bin/Rscript
args <- commandArgs(TRUE)

sql <- "
SELECT 
	score 
FROM 
	mentions, verdicts 
WHERE 
	mentions.ROWID=verdicts.mentionId 
	AND verdicts.negative=2
;"
cmd <- paste("echo \"",sql,"\"|sqlite3 ",args[1],sep="")
# cmd <- paste("echo \"",sql,"\"|sqlite3 ","tmcurator-with-scores.db",sep="")

score.invalid <- read.table(file=pipe(cmd),sep="|",header=FALSE)[,1]

sql <- "
SELECT 
	score 
FROM 
	mentions, verdicts 
WHERE 
	mentions.ROWID=verdicts.mentionId 
	AND verdicts.negative<2
;"
cmd <- paste("echo \"",sql,"\"|sqlite3 ",args[1],sep="")

score.valid <- read.table(file=pipe(cmd),sep="|",header=FALSE)[,1]

breaks <- 0:10 / 5 - 1
hist.valid <- hist(score.valid, breaks=breaks, plot=FALSE)$intensities
hist.invalid <- hist(score.invalid, breaks=breaks, plot=FALSE)$intensities

# plot(1:10 / 5 - 1, hist.valid, type='l')
# lines(1:10 / 5 - 1, hist.invalid, col="red")

pdf("score_corr.pdf")

score.freqs <- rbind(hist.valid,hist.invalid)
barplot(score.freqs, 
	beside=TRUE, 
	col=c("chartreuse4","brown"), 
	names.arg=breaks[-1],
	border=NA,
	main="Score frequencies",
	ylab="Relative frequency",
	xlab="Score"
)
legend("topleft", c("Valid","Invalid"), fill=c("chartreuse4","brown"))

dev.off()