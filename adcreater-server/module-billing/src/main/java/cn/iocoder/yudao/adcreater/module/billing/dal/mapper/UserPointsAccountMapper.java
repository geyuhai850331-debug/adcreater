package cn.iocoder.yudao.adcreater.module.billing.dal.mapper;

import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.UserPointsAccountDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserPointsAccountMapper extends BaseMapperX<UserPointsAccountDO> {
    @Select("SELECT * FROM user_points_account WHERE user_id = #{userId} FOR UPDATE")
    UserPointsAccountDO selectByUserIdForUpdate(@Param("userId") Long userId);

    @Update("UPDATE user_points_account SET balance = balance - #{amount}, " +
            "total_spent = total_spent + #{amount} WHERE user_id = #{userId} AND balance >= #{amount}")
    int deductPoints(@Param("userId") Long userId, @Param("amount") int amount);

    @Update("UPDATE user_points_account SET balance = balance + #{amount}, " +
            "total_earned = total_earned + #{amount} WHERE user_id = #{userId}")
    int addPoints(@Param("userId") Long userId, @Param("amount") int amount);
}
