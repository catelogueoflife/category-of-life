package com.touchkiss.categoryoflife.spider.species.bean.convertor;

import com.touchkiss.categoryoflife.species.bean.SpeciesCommonName;
import com.touchkiss.categoryoflife.spider.base.BaseConvertor;
import com.touchkiss.categoryoflife.spider.species.bean.bo.SpeciesCommonNameBO;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Created on 2020/03/21 11:14
 *
 * @author Touchkiss
 */

@Component
public class SpeciesCommonNameConvertor implements BaseConvertor<SpeciesCommonName, SpeciesCommonNameBO> {
    @Override
    public SpeciesCommonName convert(SpeciesCommonNameBO speciesCommonNameBO) {
        if (speciesCommonNameBO == null) {
            return null;
        }
        SpeciesCommonName speciesCommonName = new SpeciesCommonName();
        speciesCommonName.setName(speciesCommonNameBO.getName());
        speciesCommonName.setLanguage(speciesCommonNameBO.getLanguage());
        speciesCommonName.setCountry(speciesCommonNameBO.getCountry());
        return speciesCommonName;
    }
}
