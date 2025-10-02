package cn.itcast.star.graph.core.service.impl;

import cn.itcast.star.graph.core.mapper.UserResultMapper;
import cn.itcast.star.graph.core.pojo.UserResult;
import cn.itcast.star.graph.core.service.UserFundRecordService;
import cn.itcast.star.graph.core.service.UserResultService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
* <p>
* sg_user_result Service 接口实现
* </p>
*
* @author luoxu
* @since 2024-10-18 16:07:29
*/
@Service
@Transactional
@Slf4j
public class UserResultServiceImpl extends ServiceImpl<UserResultMapper, UserResult> implements UserResultService {

    @Autowired
    UserFundRecordService userFundRecordService;

    @Override
    public void saveList(List<String> urls, Long userId) {
        List<UserResult> userResults = urls.stream().map((url) -> {
            UserResult userResult = new UserResult();
            userResult.setUserId(userId);
            userResult.setUrl(url);
            userResult.setCollect(0);
            return userResult;
        }).collect(Collectors.toList());
        this.saveBatch(userResults);
        // 积分扣除
        userFundRecordService.pointsDeduction(userId,userResults.size());
    }
}