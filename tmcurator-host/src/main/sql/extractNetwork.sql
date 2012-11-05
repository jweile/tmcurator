-- ******************************************************
-- Extract network data
--
-- Author: Jochen Weile
-- ******************************************************

select g1sym, g2sym, actionType, upstream, downstream, negative, pl, updown, effect 
from mentions, actiontypes, pairs 
where mentions.actionType=actiontypes.name and mentions.pair_id=pairs.id;
