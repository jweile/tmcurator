ATTACH DATABASE "$1" AS source;
ATTACH DATABASE "$2" AS target;
DELETE FROM target.users;
INSERT INTO target.users SELECT * FROM source.users;
--FIXME: Timestamp will not transfer properly
INSERT INTO target.verdicts SELECT * FROM source.verdicts;
DELETE FROM target.articles;
INSERT INTO target.argicles SELECT * FROM source.articles;
DELETE FROM target.mentions;
INSERT INTO target.mentions SELECT * FROM source.mentions;
