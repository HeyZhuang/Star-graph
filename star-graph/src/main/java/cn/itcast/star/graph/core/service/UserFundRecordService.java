package cn.itcast.star.graph.core.service;

import cn.itcast.star.graph.core.pojo.SgUserFundRecord;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserFundRecordService extends IService<SgUserFundRecord> {
    // 积分冻结
    void pointsFreeze(Long userId, Integer money);
    // 冻结归还
    void freezeReturn(Long userId, Integer money);
    // 积分扣除
    void pointsDeduction(Long userId, Integer money);
    // 直接划扣
    void directDeduction(Long userId, Integer money);
    // 检查余额
    int checkBalance(Long userId);
    // 扣除余额
    void deductBalance(Long userId, Integer money);
}