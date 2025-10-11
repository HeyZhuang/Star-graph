package cn.itcast.star.graph.core.controller;

import cn.itcast.star.graph.core.dto.Text2ImageReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;
import cn.itcast.star.graph.core.dto.UserLoginRequestDto;
import cn.itcast.star.graph.core.dto.UserLoginResponeDto;
import cn.itcast.star.graph.core.dto.common.PageResult;
import cn.itcast.star.graph.core.dto.common.Result;
import cn.itcast.star.graph.core.pojo.UserResult;
import cn.itcast.star.graph.core.service.Text2ImageService;
import cn.itcast.star.graph.core.service.UserResultService;
import cn.itcast.star.graph.core.service.UserService;
import cn.itcast.star.graph.core.utils.UserUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
* <p>
* 文生图接口
* </p>
*
* @author luoxu
* @since 2024-10-15 14:47:14
*/
@RestController
@RequestMapping("/api/authed/1.0/t2i")
public class Text2ImageController {

    @Autowired
    private Text2ImageService text2ImageService;

    @Autowired
    private UserResultService userResultService;

    @PostMapping("/propmt")
    public Result<Text2ImageResponeDto> propmt(@RequestBody Text2ImageReqDto text2ImageReqDto) throws Exception {
        System.out.println("DEBUG: 收到生图请求 - clientId: " + text2ImageReqDto.getClientId());
        Text2ImageResponeDto text2ImageResponeDto = text2ImageService.textToImage(text2ImageReqDto);
        return Result.ok(text2ImageResponeDto);
    }

    @PostMapping("/list")
    public PageResult<Page<UserResult>> list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserUtils.getUser().getId();
        Page<UserResult> pageInfo = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserResult::getUserId, userId);
        queryWrapper.orderByDesc(UserResult::getCreatedTime);
        Page<UserResult> resultPage = userResultService.page(pageInfo, queryWrapper);
        return PageResult.ok(resultPage.getTotal(), resultPage);
    }

    @PostMapping("/priority")
    public Result<String> priority(@RequestBody Text2ImageReqDto text2ImageReqDto) {
        // 通义万相为云服务，无队列概念，此接口暂不实现
        return Result.ok("云服务无需优先级设置");
    }

    @PostMapping("/cancel")
    public Result<String> cancel(@RequestBody Text2ImageReqDto text2ImageReqDto) {
        // 通义万相为云服务，无法取消已提交的任务
        return Result.ok("云服务任务已提交无法取消");
    }

}
