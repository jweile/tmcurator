-- ******************************************************
-- Delete blacklisted articles and related data
-- Requires arguments: $ 1 = comma-separated list of PMIDs
--
-- Author: Jochen Weile
-- ******************************************************

-- delete all mentions from blacklisted articles
delete from mentions 
where article_id in (
	select id 
	from articles 
	where pmid in ($1)
);

-- delete the blacklisted articles themselves
delete from articles
where pmid in ($1);
