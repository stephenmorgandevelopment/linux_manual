package com.stephenmorgandevelopment.thelinuxmanual.distros;

import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;

import java.util.List;

import io.reactivex.Observable;

public abstract class Distribution implements LinuxDistro {
    static List<SimpleCommand> commandsList;

}
