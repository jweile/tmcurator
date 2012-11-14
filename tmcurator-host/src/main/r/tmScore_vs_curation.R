#!/usr/bin/Rscript

args <- commandArgs(TRUE)

error.bar <- function(x, y, upper, lower=upper, length=0.05, ...){
	if(length(x) != length(y) | length(y) !=length(lower) | length(lower) != length(upper))
		stop("vectors must be same length")
	arrows(x,y+upper, x, y-lower, angle=90, code=3, length=length, ...)
}

sql <- "
SELECT
	CASE invalid
		WHEN 1 THEN 0
		ELSE CASE action_match AND direction_match AND negation_match
			WHEN 1 THEN 1
			ELSE .5
		END
	END AS curation_score, 
	score AS tm_score
FROM (
	SELECT 
		--action match?
		verdicts.action=mentions.actionType AS action_match,

		--direction match?
		CASE actiontypes.updown
			WHEN 1 THEN 
				CASE mentions.upstream=pairs.g1sym 
					WHEN 1 then 1
					ELSE -1
				END
			WHEN 0 THEN 0
			ELSE 
				CASE mentions.upstream=pairs.g2sym 
					WHEN 1 then 1
					ELSE -1
				END
		END = verdicts.updown AS direction_match,

		--negation match?
		CASE verdicts.negative
			WHEN 2 THEN 1
			ELSE verdicts.negative=mentions.negative 
		END AS negation_match,

		--invalid?
		verdicts.negative=2 AS invalid,

		--score
		mentions.score

	FROM 
		verdicts, mentions, pairs, actiontypes
	WHERE 
		verdicts.mentionId=mentions.ROWID 
		AND mentions.pair_id=pairs.id 
		AND actiontypes.name=mentions.actionType
);
"
cmd <- paste("echo \"",sql,"\"|sqlite3 ",args[1],sep="")

data <- read.table(file=pipe(cmd),sep="|",header=FALSE)
colnames(data) <- c("curation.score","tm.score")


#General histogram of scores
#---------------------------
pdf("score_distribution.pdf")
hist(data$tm.score, 
	main="Histogram of textmining scores",
	xlab="Score")
dev.off()


#Separated histograms
#--------------------
breaks <- 0:10 / 5 - 1
hist.invalid <- hist(data[data[,1]==0,2], breaks=breaks, plot=FALSE)$counts
hist.altered <- hist(data[data[,1]==0.5,2], breaks=breaks, plot=FALSE)$counts
hist.correct <- hist(data[data[,1]==1,2], breaks=breaks, plot=FALSE)$counts
score.freqs <- rbind(hist.invalid, hist.altered, hist.correct)

pdf("scores_by_class.pdf")
my.colors <- c("red3", "gold", "green3");
barplot(score.freqs, 
	beside=TRUE, 
	col=my.colors, 
	names.arg=breaks[-1],
	border=NA,
	main="Score frequencies",
	ylab="Frequency",
	xlab="Score"
)
legend("topleft", c("Invalid","Altered","Correct"), fill=my.colors)
dev.off()

#Share diagram
#--------------
pdf("class_shares_by_score.pdf")
plot(0,type="n", 
	xlim=c(-1,1),ylim=c(0,1),
	main="Curation result shares by score",
	xlab="Textmining score",
	ylab="Curation class shares"
)
sh.totals <- hist.invalid + hist.altered + hist.correct
polygon(c(-1,-1,1,1), c(1,0,0,1),col=my.colors[3])
sh.altered <- (hist.invalid + hist.altered) / sh.totals
polygon(c(-1, breaks, 1), c(0,sh.altered[1],sh.altered,0), col=my.colors[2])
sh.invalid <- hist.invalid / sh.totals
polygon(c(-1,breaks,1), c(0,sh.invalid[1],sh.invalid,0), col=my.colors[1])
text(c(-.5,0,.5),c(.2,.57,.8),c("Invalid","Altered","Correct"))
dev.off()

#Counts of curation scores
#-------------------------
score.counts <- vector()
score.counts["invalid"] <- length(data[data[,1]==0,1])
score.counts["altered"] <- length(data[data[,1]==0.5,1])
score.counts["correct"] <- length(data[data[,1]==1,1])

pdf("curation_scores.pdf")
barplot(score.counts, 
	main="Curation result frequencies", 
	xlab="Curation result",
	ylab="Frequency",
	col="steelblue")
dev.off()

# score.stats <- data.frame()
# score.stats["0", "mean"] <- mean(data[data[,1]==0,2])
# score.stats[".5", "mean"] <- mean(data[data[,1]==0.5,2])
# score.stats["1", "mean"] <- mean(data[data[,1]==1,2])
# score.stats["0", "std"] <- sqrt(var(data[data[,1]==0,2]))
# score.stats[".5", "std"] <- sqrt(var(data[data[,1]==0.5,2]))
# score.stats["1", "std"] <- sqrt(var(data[data[,1]==1,2]))

# pdf("score-comparison.pdf")
# plot(c(0,0.5,1), score.stats$mean,
# 	type="b",
# 	ylim=c(-1,1),
# 	main="Score comparison",
# 	xlab="Curation score",
# 	ylab="Textmining score"
# )
# error.bar(c(0,0.5,1), score.stats$mean, score.stats$std)
# dev.off()

cor.test(data[,1],data[,2])


