package com.dengjia.lib_share_asr.utils;

import com.dengjia.lib_share_asr.asr_skill.BaseSkill;

import java.util.HashMap;
import java.util.List;

public class JudgeAllSkillsUtil {
    public static boolean judgeAllSkillsUtil(String data, List<BaseSkill> skillsList){
        boolean flag = false;
        for (int i = 0; i < skillsList.size(); i++) {
            if (IsContainUtil.isContainUtil(data, skillsList.get(i))){
                flag = true;
                break;
            }
        }

        return flag;
    }
}
