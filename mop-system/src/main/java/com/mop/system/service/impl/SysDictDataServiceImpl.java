package com.mop.system.service.impl;

import com.mop.common.core.domain.entity.SysDictData;
import com.mop.common.utils.DictUtils;
import com.mop.system.mapper.SysDictDataMapper;
import com.mop.system.service.ISysDictDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典 业务层处理
 *
 * @author weiyiming
 */
@Service
public class SysDictDataServiceImpl implements ISysDictDataService {
    private static final Logger log = LoggerFactory.getLogger(SysDictDataServiceImpl.class);

    @Autowired
    private SysDictDataMapper dictDataMapper;

    /**
     * 根据条件分页查询字典数据
     *
     * @param dictData 字典数据信息
     * @return 字典数据集合信息
     */
    @Override
    public List<SysDictData> selectDictDataList(SysDictData dictData) {
        return dictDataMapper.selectDictDataList(dictData);
    }

    /**
     * 根据字典类型和字典键值查询字典数据信息
     *
     * @param dictType  字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    @Override
    public String selectDictLabel(String dictType, String dictValue) {
        return dictDataMapper.selectDictLabel(dictType, dictValue);
    }

    /**
     * 根据字典数据ID查询信息
     *
     * @param dictCode 字典数据ID
     * @return 字典数据
     */
    @Override
    public SysDictData selectDictDataById(Long dictCode) {
        return dictDataMapper.selectDictDataById(dictCode);
    }

    /**
     * 批量删除字典数据信息
     *
     * @param dictCodes 需要删除的字典数据ID
     */
    @Override
    @Transactional
    public void deleteDictDataByIds(Long[] dictCodes) {
        for (Long dictCode : dictCodes) {
            SysDictData data = selectDictDataById(dictCode);
            dictDataMapper.deleteDictDataById(dictCode);
            try {
                List<SysDictData> dictDatas = dictDataMapper.selectDictDataByType(data.getDictType());
                DictUtils.setDictCache(data.getDictType(), dictDatas);
            } catch (Exception e) {
                log.error("字典缓存更新失败, dictType={}", data.getDictType(), e);
            }
        }
    }

    /**
     * 新增保存字典数据信息
     *
     * @param data 字典数据信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertDictData(SysDictData data) {
        int row = dictDataMapper.insertDictData(data);
        if (row > 0) {
            try {
                List<SysDictData> dictDatas = dictDataMapper.selectDictDataByType(data.getDictType());
                DictUtils.setDictCache(data.getDictType(), dictDatas);
            } catch (Exception e) {
                log.error("字典缓存更新失败, dictType={}", data.getDictType(), e);
            }
        }
        return row;
    }

    /**
     * 修改保存字典数据信息
     *
     * @param data 字典数据信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateDictData(SysDictData data) {
        int row = dictDataMapper.updateDictData(data);
        if (row > 0) {
            try {
                List<SysDictData> dictDatas = dictDataMapper.selectDictDataByType(data.getDictType());
                DictUtils.setDictCache(data.getDictType(), dictDatas);
            } catch (Exception e) {
                log.error("字典缓存更新失败, dictType={}", data.getDictType(), e);
            }
        }
        return row;
    }
}
