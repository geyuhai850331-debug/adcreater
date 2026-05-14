package com.djb.framework.dict.config;

import com.djb.framework.common.biz.system.dict.DictDataCommonApi;
import com.djb.framework.dict.core.DictFrameworkUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class DjbDictAutoConfiguration {

    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public DictFrameworkUtils dictUtils(DictDataCommonApi dictDataApi) {
        DictFrameworkUtils.init(dictDataApi);
        return new DictFrameworkUtils();
    }

}
