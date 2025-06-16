package edu.kit.kastel.vads.compiler.ir.LivenessAnalysis;

import java.util.HashSet;
import java.util.Set;

public class LivenessInfo {
    private Set<String> use = new HashSet<>();
    private Set<String> def = new HashSet<>();
    private Set<String> liveIn = new HashSet<>();
    private Set<String> liveOut = new HashSet<>();

    public Set<String> getUse(){
        return this.use;
    }

    public Set<String> getDef(){
        return this.def;
    }

    public Set<String> getLiveIn(){
        return this.liveIn;
    }

    public Set<String> getLiveOut(){
        return this.liveOut;
    }

    public void setUse(Set<String> use){
        this.use = use;
    }

    public void setDef(Set<String> def){
        this.def = def;
    }

    public void setLiveIn(Set<String> liveIn){
        this.liveIn = liveIn;
    }

    public void setLiveOut(Set<String> liveOut){
        this.liveOut = liveOut;
    }

    public void addUse(String node){
        use.add(node);
    }
}
