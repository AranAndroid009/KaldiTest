package com.dengjia.lib_share_asr.asr_skill.Entity;

import java.util.ArrayList;
import java.util.List;

public class ActionEntity extends BaseEntity {
    private List<String> actions = new ArrayList<>();

    public ActionEntity(){
        actions = new ArrayList<>();
        actions.add("打开");
        actions.add("关闭");
        actions.add("调亮");
        actions.add("调暗");
        actions.add("调小");
        actions.add("调大");
    }

    @Override
    public List<String> getEntitiess(){
        return actions;
    }
}
