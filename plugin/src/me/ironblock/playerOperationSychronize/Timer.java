package me.ironblock.playerOperationSychronize;

import net.minecraft.server.packs.metadata.pack.ResourcePackInfo;

public class Timer {
    private long time;
    private long current = 0;
    public Timer(long time) {
        setTime(time);
    }

    public boolean update(){
        current++;
        if (current>=time){
            current = 0;
            return true;
        }else{
            return false;
        }
    }

    public void setTime(long time){
        this.time = time;
    }

    public void reset(){
        current = 0;
    }

    public void updateNow(){
        current = time;
    }
}
