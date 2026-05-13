package cn.iocoder.yudao.adcreater.module.template.dal.mapper;

import cn.iocoder.yudao.adcreater.module.template.dal.dataobject.TemplateDO;
import cn.iocoder.yudao.adcreater.module.template.dal.dataobject.TemplateVersionDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface TemplateMapper extends BaseMapperX<TemplateDO> {

    @Select("SELECT v.id, v.template_id, v.version, v.file_url, v.changelog, v.created_at " +
            "FROM template_version v " +
            "INNER JOIN (SELECT template_id, MAX(version) AS max_version FROM template_version GROUP BY template_id) latest " +
            "ON v.template_id = latest.template_id AND v.version = latest.max_version " +
            "INNER JOIN template t ON t.id = v.template_id " +
            "WHERE t.status = 'published'")
    List<TemplateVersionDO> selectLatestVersions();
}
