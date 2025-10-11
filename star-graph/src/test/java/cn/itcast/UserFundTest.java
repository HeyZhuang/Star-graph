package cn.itcast;

import cn.itcast.star.graph.core.mapper.SgUserFundMapper;
import cn.itcast.star.graph.core.pojo.SgUserFund;
import cn.itcast.star.graph.core.service.UserFundRecordService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserFundTest {

    @Autowired
    SgUserFundMapper sgUserFundMapper;

    @Autowired
    UserFundRecordService userFundRecordService;

    @Test
    public void testCheckAllUserFunds(){
        List<SgUserFund> funds = sgUserFundMapper.selectList(null);
        System.out.println("========================================");
        System.out.println("所有用户积分账户信息");
        System.out.println("========================================");
        for(SgUserFund fund : funds){
            System.out.println("用户ID: " + fund.getUserId() +
                             " | 积分余额: " + fund.getScore() +
                             " | 冻结积分: " + fund.getFreezeScore() +
                             " | 版本: " + fund.getVersion());
        }
        System.out.println("========================================");
        System.out.println("总计: " + funds.size() + " 个积分账户");
    }

    @Test
    public void testInitializeFundsForTestUsers(){
        System.out.println("初始化测试用户积分账户...");

        // 检查用户ID 1976682360185856001 (admin)
        Long adminUserId = 1976682360185856001L;
        SgUserFund adminFund = sgUserFundMapper.selectOne(
            Wrappers.<SgUserFund>lambdaQuery().eq(SgUserFund::getUserId, adminUserId)
        );

        if(adminFund == null){
            adminFund = new SgUserFund();
            adminFund.setUserId(adminUserId);
            adminFund.setScore(1000L);
            adminFund.setFreezeScore(0L);
            adminFund.setVersion(0L);
            sgUserFundMapper.insert(adminFund);
            System.out.println("✅ 管理员积分账户已创建: " + adminUserId + " | 初始积分: 1000");
        } else {
            System.out.println("✅ 管理员积分账户已存在: " + adminUserId + " | 当前积分: " + adminFund.getScore());
        }

        // 检查用户ID 1976682360517206017 (testuser)
        Long testUserId = 1976682360517206017L;
        SgUserFund testFund = sgUserFundMapper.selectOne(
            Wrappers.<SgUserFund>lambdaQuery().eq(SgUserFund::getUserId, testUserId)
        );

        if(testFund == null){
            testFund = new SgUserFund();
            testFund.setUserId(testUserId);
            testFund.setScore(500L);
            testFund.setFreezeScore(0L);
            testFund.setVersion(0L);
            sgUserFundMapper.insert(testFund);
            System.out.println("✅ 测试用户积分账户已创建: " + testUserId + " | 初始积分: 500");
        } else {
            System.out.println("✅ 测试用户积分账户已存在: " + testUserId + " | 当前积分: " + testFund.getScore());
        }
    }

    @Test
    public void testDeductBalance(){
        Long adminUserId = 1976682360185856001L;

        System.out.println("========================================");
        System.out.println("测试积分扣除功能");
        System.out.println("========================================");

        // 查询当前余额
        int balanceBefore = userFundRecordService.checkBalance(adminUserId);
        System.out.println("扣除前余额: " + balanceBefore);

        // 扣除10积分
        try {
            userFundRecordService.deductBalance(adminUserId, 10);
            System.out.println("✅ 成功扣除10积分");
        } catch (Exception e) {
            System.out.println("❌ 扣除失败: " + e.getMessage());
        }

        // 查询扣除后余额
        int balanceAfter = userFundRecordService.checkBalance(adminUserId);
        System.out.println("扣除后余额: " + balanceAfter);
        System.out.println("实际扣除: " + (balanceBefore - balanceAfter) + " 积分");
        System.out.println("========================================");
    }

    @Test
    public void testAddUserFunds(){
        System.out.println("========================================");
        System.out.println("增加用户积分");
        System.out.println("========================================");

        Long adminUserId = 1976682360185856001L;
        int addAmount = 10000; // 增加 10000 积分

        // 查询或创建用户资金账户
        SgUserFund userFund = sgUserFundMapper.selectOne(
            Wrappers.<SgUserFund>lambdaQuery().eq(SgUserFund::getUserId, adminUserId)
        );

        if (userFund == null) {
            userFund = new SgUserFund();
            userFund.setUserId(adminUserId);
            userFund.setScore((long) addAmount);
            userFund.setFreezeScore(0L);
            userFund.setVersion(0L);
            sgUserFundMapper.insert(userFund);
            System.out.println("✅ 创建用户资金账户成功，初始积分: " + addAmount);
        } else {
            long oldScore = userFund.getScore();
            userFund.setScore(oldScore + addAmount);
            sgUserFundMapper.updateById(userFund);
            System.out.println("✅ 用户积分已增加 " + addAmount);
            System.out.println("   原积分: " + oldScore);
            System.out.println("   新积分: " + userFund.getScore());
        }

        // 同时检查并创建系统总账户（ID=0）
        SgUserFund systemFund = sgUserFundMapper.selectOne(
            Wrappers.<SgUserFund>lambdaQuery().eq(SgUserFund::getUserId, 0L)
        );

        if (systemFund == null) {
            systemFund = new SgUserFund();
            systemFund.setUserId(0L);
            systemFund.setScore(0L);
            systemFund.setFreezeScore(0L);
            systemFund.setVersion(0L);
            sgUserFundMapper.insert(systemFund);
            System.out.println("✅ 创建系统总账户成功");
        }

        System.out.println("========================================");
    }
}
