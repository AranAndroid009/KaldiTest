/**
 * Copyright 2020 JiaDeng.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dengjia.lib_share_asr.utils;

import com.dengjia.lib_share_asr.asr_skill.BaseSkill;

import java.util.HashMap;

public class IsContainUtil {
    public static boolean isContainUtil(String data, BaseSkill baseSkill){
        HashMap<Integer, Boolean> flags = new HashMap<>();
        for (int i = 0; i < baseSkill.getEntityList().size(); i++) {
            for (int j = 0; j < baseSkill.getEntityList().get(i).getEntitiess().size(); j++) {
                if (data.contains(baseSkill.getEntityList().get(i).getEntitiess().get(j))){
                    flags.put(i, true);
                }
            }
        }

        boolean flag = true;
        for (int i = 0; i < flags.size(); i++) {
            if (!flags.get(i)) {
                flag = false;
                break;
            }
        }

        return flag;
    }
}
