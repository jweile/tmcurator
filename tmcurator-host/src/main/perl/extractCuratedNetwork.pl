#!/usr/bin/perl

#prepare SQL statement
my $sql = <<SQL;
SELECT g1sym, g2sym, action, verdicts.updown, negative, pl, effect 
FROM pairs, verdicts, actiontypes 
WHERE pairs.ROWID=verdicts.pairId AND verdicts.action=actiontypes.name;
SQL

#hash for column indices
my %c = (
	g1sym => 0,
	g2sym => 1,
	action => 2,
	updown => 3,
	negative => 4,
	pl => 5,
	effect => 6
);

my %set = ();

#read results from sql query
open IN, "echo \"$sql\" | sqlite3 $ARGV[0] |" or die $!;

while (<IN>) {

	#extract columns
	my @cols = split /\|/, $_;

	#skip lines with missing gene names
	if (!defined($cols[$c{g1sym}]) || length($cols[$c{g1sym}])==0
		|| !defined($cols[$c{g2sym}]) || length($cols[$c{g2sym}])==0) {
		next;
	}

	#skip false interactions
	if (int($cols[$c{negative}])) {
		next;
	}

	#determine correct order
	my $g1 = int($cols[$c{updown}]) >= 0 ? $cols[$c{g1sym}] : $cols[$c{g2sym}];
	my $g2 = int($cols[$c{updown}]) >= 0 ? $cols[$c{g2sym}] : $cols[$c{g1sym}];

	my $directed = int($cols[$c{updown}]) != 0;
	my $effect = int($cols[$c{effect}]);
	my $arrow = $directed * ($effect < 0 ? -1 : 1);

	#store results
	my $out = "$g1\t$g2\t$cols[$c{action}]\t$cols[$c{pl}]\t$arrow\t$effect\n";
	$set{$out} = 1;
}

#print results
print "gene1\tgene2\taction\tpl\tarrow\teffect\n";
foreach my $key (keys %set) {
	print $key;
}

close IN;

