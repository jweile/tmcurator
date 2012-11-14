-- ******************************************************
-- calculate penalties for textmining extractions from curation
--
-- Requires no arguments
--
-- Author: Jochen Weile
-- ****************************************

.header on
.mode column

SELECT
	1 - ((1-action_match)*.5 + (1-direction_match)*.1 + (1-negation_match)*.1 + invalid) AS curation_score, 
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
