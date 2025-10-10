package cn.itcast.star.graph.core.service.impl;

import cn.itcast.star.graph.core.exception.CustomException;
import cn.itcast.star.graph.core.mapper.SgUserFundMapper;
import cn.itcast.star.graph.core.mapper.SgUserFundRecordMapper;
import cn.itcast.star.graph.core.pojo.SgUserFund;
import cn.itcast.star.graph.core.pojo.SgUserFundRecord;
import cn.itcast.star.graph.core.service.UserFundRecordService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class UserFundRecordServiceImpl extends ServiceImpl<SgUserFundRecordMapper, SgUserFundRecord> implements UserFundRecordService {

    @Autowired
    SgUserFundMapper sgUserFundMapper;
    @Autowired
    SgUserFundRecordMapper sgUserFundRecordMapper;
    /**
     * 积分冻结：指从用户积分账户拿出积分，增加到用户积分冻结账户上
     * @param userId
     * @param money
     */
    @Override
    public void pointsFreeze(Long userId, Integer money) {
        SgUserFund sgUserFund = getUserSgUserFund(userId);
        long temp = sgUserFund.getScore() - money;
        if(temp>=0){
            sgUserFund.setScore(temp);
            sgUserFund.setFreezeScore(sgUserFund.getFreezeScore()+money);
            sgUserFundMapper.updateById(sgUserFund);

            saveLog(0,-money,sgUserFund.getId());
            saveLog(1,money,sgUserFund.getId());
        }else{
            throw new CustomException("积分账户余额不足");
        }
    }

    /**
     * 冻结归还：指从积分冻结账户上拿出积分，增加到用户积分账户上
     * @param userId
     * @param money
     */
    @Override
    public void freezeReturn(Long userId, Integer money) {
        SgUserFund sgUserFund = getUserSgUserFund(userId);
        long temp = sgUserFund.getFreezeScore() - money;
        if(temp>=0){
            sgUserFund.setScore(sgUserFund.getScore()+money);
            sgUserFund.setFreezeScore(temp);
            sgUserFundMapper.updateById(sgUserFund);

            saveLog(0,money,sgUserFund.getId());
            saveLog(1,-money,sgUserFund.getId());
        }else{
            throw new CustomException("积分冻结账户余额不足");
        }
    }

    /**
     * 积分扣除：指从用户积分冻结账号上拿出积分，增加到总账户的积分账户上
     * @param userId
     * @param money
     */
    @Override
    public void pointsDeduction(Long userId, Integer money) {
        SgUserFund userFund = getUserSgUserFund(userId);
        SgUserFund allFund = getUserSgUserFund(0);

        long temp = userFund.getFreezeScore() - money;
        if(temp>=0){
            // 用户账户修改
            userFund.setFreezeScore(temp);
            saveLog(1,-money,userFund.getId());
            sgUserFundMapper.updateById(userFund);

            // 修改总账户
            allFund.setScore(allFund.getScore()+money);
            saveLog(0,money,allFund.getId());
            sgUserFundMapper.updateById(allFund);
        }else{
            throw new CustomException("积分冻结账户余额不足");
        }

    }

    @Override
    public void directDeduction(Long userId, Integer money) {

    }

    private void saveLog(int fundType,int money,long fundId){
        SgUserFundRecord log = new SgUserFundRecord();
        log.setFundType(fundType);
        log.setMoney(money);
        log.setFundId(fundId);
        sgUserFundRecordMapper.insert(log);
    }

    public SgUserFund getUserSgUserFund(long userId){
        SgUserFund sgUserFund = sgUserFundMapper.selectOne(Wrappers.<SgUserFund>lambdaQuery().eq(SgUserFund::getUserId, userId));
        if(sgUserFund==null){
            sgUserFund = new SgUserFund();
            sgUserFund.setUserId(userId);
            sgUserFund.setScore(0L);
            sgUserFund.setFreezeScore(0L);
            sgUserFund.setVersion(0L);
            sgUserFundMapper.insert(sgUserFund);
        }
        return sgUserFund;
    }
    
    /**
     * 检查用户余额
     * @param userId
     * @return
     */
    @Override
    public int checkBalance(Long userId) {
        SgUserFund sgUserFund = getUserSgUserFund(userId);
        return (int) sgUserFund.getScore();
    }
    
    /**
     * 扣除余额
     * @param userId
     * @param money
     */
    @Override
    public void deductBalance(Long userId, Integer money) {
        directDeduction(userId, money);
    }
}
