package cn.iocoder.yudao.adcreater.module.template.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("template")
public class TemplateDO {
    @TableId
    private Long id;
    private String name;
    private String description;
    private String category;
    private String thumbnailUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
