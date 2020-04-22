package com.touchkiss.categoryoflife.translate.mapper;

import com.touchkiss.categoryoflife.translate.bean.Translate;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created on 2020-03-21 17:02:51
 *
 * @author Touchkiss
 */
@Mapper
public interface TranslateDaoM {
	int insert(Translate translate);

	int insertWithoutId(Translate translate);

    int deleteById(Integer id);

    int deleteByMap(@Param("wheres") Map<String, String> wheres);

    int updateById(@Param("translate") Translate translate, @Param("id") Integer id);

    int updateSelectiveById(@Param("translate") Translate translate, @Param("id") Integer id);

    int updateSelectiveByMap(@Param("translate") Translate translate, @Param("wheres") Map<String, String> wheres);

    Translate selectById(Integer id);

    List<Translate> queryTranslateList(@Param("wheres") Map<String, String> wheres);

	Page<Translate> findByPage(@Param("wheres") Map<String, String> wheres);
}
