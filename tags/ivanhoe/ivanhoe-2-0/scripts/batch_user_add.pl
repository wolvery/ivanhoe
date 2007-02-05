#!/usr/bin/perl -w

use strict;

while (<>) {
    my @tokens = split(/ /);
    my %vals;

    $vals{"id"} = $tokens[0];
    $vals{"fname"} = $tokens[1];
    $vals{"lname"} = $tokens[2];
    $vals{"password"} = `echo $tokens[3] | md5hashpass`;
    chomp($vals{"password"});
    $vals{"email"} = $tokens[4];
    my $affiliation = "";
    foreach (5 .. $#tokens)
    {
        my $token = $tokens[$_];
        chomp($token);
        $affiliation .= $token . " ";
    }
    chop($affiliation);

    $vals{"affiliation"} = $affiliation;

    printf("INSERT INTO player VALUES (%i, \"%s %s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");\n",
        $vals{id},
        $vals{fname}, $vals{lname},
        $vals{password},
        $vals{fname},
        $vals{lname},
        $vals{email},
        $vals{affiliation});
}
