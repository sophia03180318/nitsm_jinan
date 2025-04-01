package com.jcca.web2.enums;

import lombok.Getter;

@Getter
public enum DangerCommand {

    RMRF("rm -rf /"),
    CHMOD("chmod -R 777 /"),
    DDIF("dd if=/dev/random of=/dev/sda"),
    DDSDA("> /dev/sda"),
    CATSDA("cat /dev/null > /dev/sda"),
    MKFS("mkfs.ext4 /dev/sda"),
    REBOOT("reboot"),
    SHUTDOWN("shutdown -h now"),
    FORK(":(){ :|:& };:"),
    USERDEL("userdel -r username"),
    PASSROOT("passwd root"),
    ;

    String command;

    DangerCommand(String command) {
        this.command = command;
    }
}
