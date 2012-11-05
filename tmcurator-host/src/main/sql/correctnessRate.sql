-- ******************************************************
-- Assess rate of correct classification per verb
--
-- Author: Jochen Weile
-- ******************************************************

SELECT 
	new, 
	AVG(match) AS correctness,
	COUNT(*) AS total
FROM (
	SELECT mentions.actionType AS old, 
	verdicts.action AS new, 
	mentions.actionType=verdicts.action AS match
	FROM mentions, verdicts
	WHERE mentions.ROWID=verdicts.mentionId
)
GROUP BY new
ORDER BY total DESC, correctness DESC;