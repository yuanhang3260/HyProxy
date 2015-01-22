#!/usr/bin/perl
use File::Copy qw(copy);

my $directory = '.';
opendir(DIR, $directory);
while ($file = readdir(DIR)) {
	if ($file eq "mv.perl" or $file eq "." or $file eq "..") {
		next;
	}
	$folderName = substr($file, 0, -4);
	mkdir $folderName;
	$newFileName = "$folderName/$file";
	copy($file, $newFileName);
	print "$file\n";
}
