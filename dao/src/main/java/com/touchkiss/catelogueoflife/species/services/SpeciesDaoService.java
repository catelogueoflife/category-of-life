package com.touchkiss.catelogueoflife.species.services;

import com.github.pagehelper.Page;
import com.touchkiss.catelogueoflife.species.bean.Species;
import java.util.Map;
import java.util.List;

/**
 * Created on 2020-03-20 17:31:57
 *
 * @author Touchkiss
 */
public interface SpeciesDaoService {
	boolean insert(Species species);

	boolean deleteById(String id);

    int deleteByMap(Map<String, String> wheres);

    boolean updateById(Species species, String id);

    boolean updateSelectiveById(Species species, String id);

    int updateSelectiveByMap(Species species, Map<String, String> wheres);

	Species selectById(String id);

	List<Species> querySpeciesList(Map<String, String> map);

	Page<Species> findByPage(int pageNo, int pageSize, Map<String, String> map);
}
