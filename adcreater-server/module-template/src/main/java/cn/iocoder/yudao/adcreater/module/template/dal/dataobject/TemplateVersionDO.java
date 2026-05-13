package cn.iocoder.yudao.adcreater.module.template.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("template_version")
public class TemplateVersionDO {
    @TableId
    private Long id;
    private Long templateId;
    private Integer version;
    private String fileUrl;
    private String changelog;
    private LocalDateTime createdAt;
}
