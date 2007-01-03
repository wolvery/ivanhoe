#!/usr/bin/env perl

use strict;
use integer;

if ($ARGV[0] =~ m/--help/) {
    print "Usage: $0 [offset number] [destination directory]\n";
    print "       $0 --help\n";
    exit;
}

my $gameOffset = shift;
my $destinationDir = shift;
if (length($destinationDir) > 0 and !($destinationDir =~ m/\/$/)) {
    $destinationDir .= "/";
}

for (<stdin>) {
    chomp;

    my @TOKENS = split /-/, $_;
    my $gameString = $TOKENS[-2];
    my $gameNumber = $gameString;
    $gameNumber =~ s/game//;
    $gameNumber += $gameOffset;
    
    my $oldName = join('-', @TOKENS);
    my $newName = $destinationDir
           . join('-', $TOKENS[0 .. -2], "game" . $gameNumber, $TOKENS[-1]);

    print "$oldName\t===>\t$newName\n";
    rename($oldName,$newName) or die "Error renaming [$oldName] to [$newName]";
}
