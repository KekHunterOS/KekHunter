# Command history tweaks:
# - Append history instead of overwriting
#   when shell exits.
# - When using history substitution, do not
#   exec command immediately.
# - Do not save to history commands starting
#   with space.
# - Do not save duplicated commands.

# Setup PATH so we can use the kekfs utils/bins
PATH=$PATH:/data/data/com.team420.kekhunter/files/etc/root/bin:/data/data/com.team420.kekhunter/files/etc/root/sbin:/data/data/com.team420.kekhunter/files/scripts:/bin:/system/bin:/system/xbin:.

HOME=/sdcard

shopt -s histappend
shopt -s histverify
export HISTCONTROL=ignoreboth

# Default command line prompt.
PROMPT_DIRTRIM=2
PS1='\[\e[0;32m\]\w\[\e[0m\] \[\e[0;97m\]\$\[\e[0m\] '

# Handles nonexistent commands.
# If user has entered command which invokes non-available
# utility, command-not-found will give a package suggestions.
if [ -x @TERMUX_PREFIX@/libexec/termux/command-not-found ]; then
	command_not_found_handle() {
		@TERMUX_PREFIX@/libexec/termux/command-not-found "$1"
	}
fi

# Some useful stuff
alias home='cd ~'
